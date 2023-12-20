package io.agamis.fusion.core.actor.entity

import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.core.actor.common.enum.Dispatcher
import io.agamis.fusion.core.actor.serialization.JsonSerializable
import io.agamis.fusion.core.db.datastore.cache.OrganizationStore
import io.agamis.fusion.core.db.datastore.cache.exceptions.NotFoundException
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.model
import io.agamis.fusion.core.model.OrganizationFK
import io.agamis.fusion.env.EnvContainer
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.DispatcherSelector
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.StashBuffer
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey
import org.apache.pekko.pattern.StatusReply

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object Organization {
    trait Command extends JsonSerializable
    final case class Get(replyTo: ActorRef[StatusReply[State]]) extends Command
    final case class GetProfiles(replyTo: ActorRef[_])          extends Command
    final case class GetGroups(replyTo: ActorRef[_])            extends Command
    final case class GetApplications(replyTo: ActorRef[_])      extends Command
    final case class GetFilesystems(replyTo: ActorRef[_])       extends Command

    private case object Passivate extends Command

    final case class Update(
        replyTo: ActorRef[UpdateResult],
        org: OrganizationMutation
    ) extends Command
    sealed trait UpdateResult extends JsonSerializable
    final case class UpdateSuccess(o: model.Organization)  extends UpdateResult
    final case class UpdateFailure(cause: Some[Throwable]) extends UpdateResult
    final case class WrappedUpdateResult(
        replyTo: ActorRef[UpdateResult],
        result: UpdateResult
    ) extends Command

    final case class Delete(replyTo: ActorRef[StatusReply[DeleteResult]])
        extends Command
    sealed trait DeleteResult        extends JsonSerializable
    final case class DeleteSuccess() extends DeleteResult
    final case class DeleteFailure(cause: Some[Throwable]) extends DeleteResult
    final case class WrappedDeleteResult(
        replyTo: ActorRef[StatusReply[DeleteResult]],
        result: DeleteResult
    ) extends Command

    private final case class Init(initialValue: model.Organization)
        extends Command
    private final case class StoreError(cause: Throwable) extends Command

    sealed trait Response                               extends JsonSerializable
    final case class Error(cause: Throwable)            extends Response
    sealed trait State                                  extends Response
    final case class Shadow()                           extends State
    final case class Queryable(org: model.Organization) extends State
    final case class ProfilesAggregate(profiles: Vector[model.Profile])
        extends Response
    final case class GroupsAggregate(groups: Vector[model.Group])
        extends Response
    final case class ApplicationsAggregate(
        applications: Vector[model.Application]
    ) extends Response
    final case class FilesystemsAggregate(filesystems: Vector[model.Filesystem])
        extends Response

    val TypeKey: EntityTypeKey[Command] =
        EntityTypeKey[Command](getClass().getSimpleName())

    /** @param entityId
      * @param wrapper
      * @return
      *   behavior of existing organization to
      */
    def apply(
        shard: ActorRef[ClusterSharding.ShardCommand],
        entityId: String
    )(implicit wrapper: IgniteClientNodeWrapper): Behavior[Command] = {

        Behaviors.withStash(
          Try(
            EnvContainer.getString("fusion.actor.stash.organization").toInt
          ).getOrElse(1000)
        ) { buffer =>
            Behaviors.setup[Command] { ctx =>
                implicit val ec: ExecutionContext =
                    ctx.system.dispatchers.lookup(
                      DispatcherSelector.fromConfig(Dispatcher.DB_OP)
                    )
                new Organization(
                  shard,
                  ctx,
                  buffer,
                  entityId,
                  new OrganizationStore
                ).bootstrap()
            }
        }
    }
}

class Organization(
    shard: ActorRef[ClusterSharding.ShardCommand],
    ctx: ActorContext[Organization.Command],
    buffer: StashBuffer[Organization.Command],
    entityId: String,
    store: OrganizationStore
)(implicit _ec: ExecutionContext) {
    import Organization._

    val log = ctx.log

    ctx.setReceiveTimeout(5.minutes, Passivate)

    /** A bootstrap method. Tries to get an existing persisted organization or
      * wait for an initial update, then wait in a queryable or shadow behavior
      *
      * @return
      *   a behavior for bootstrapping shard
      */
    private def bootstrap(): Behavior[Command] = {

        log.debug(
          ">> Organization#bootstrap > bootstraping organization with id{{}}",
          entityId
        )

        ctx.pipeToSelf(store.getById(UUID.fromString(entityId))) {
            case Success(org) => Init(org)
            case Failure(e)   => StoreError(e)
        }

        Behaviors.receiveMessage {
            case Init(o) =>
                buffer.unstashAll(
                  if (o.queryable) queryable(o) else shadow(o)
                )
            case StoreError(cause) =>
                cause match {
                    case NotFoundException(msg, cause) =>
                        log.debug(
                          "-- Organization#bootstrap > No organization found in persistence storage"
                        )
                        buffer.unstashAll(idle())
                    case other =>
                        log.error(
                          "<< Organization#bootstrap > Could not retrieve organization from database",
                          cause
                        )
                        throw cause
                }
            case other =>
                buffer.stash(other)
                Behaviors.same
        }
    }

    /** An idle method. Waits for an initial update.
      *
      * @return
      *   an idle behavior for initial update only
      */
    private def idle(): Behavior[Command] = {

        log.debug(
          ">> Organization#idle > shard{{}} going idle",
          entityId
        )

        Behaviors.receiveMessage {
            case Update(replyTo, mut) =>
                val newState = model.Organization(
                  id = UUID.fromString(entityId),
                  label = mut.label,
                  queryable = mut.queryable,
                  OrganizationFK(
                    organizationTypeId = mut.organizationTypeId
                  ),
                  createdAt = LocalDateTime.now(),
                  updatedAt = LocalDateTime.now()
                )
                updated(replyTo, newState)
            case Get(replyTo) =>
                replyTo ! StatusReply.Error(new NotFoundException())
                Behaviors.same
            case Delete(replyTo) =>
                replyTo ! StatusReply.Error(new NotFoundException())
                Behaviors.same
            case Passivate =>
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            case other =>
                // stash other msg while waiting for initial update
                buffer.stash(other)
                Behaviors.same
        }
    }

    /** A state used for shadow organizations, i.e. organizations that cannot be
      * seen by common mortals
      *
      * @param o
      *   inject new organization state
      * @return
      */
    private def shadow(o: model.Organization): Behavior[Command] = {
        Behaviors.receiveMessage {
            case Get(replyTo) =>
                replyTo ! StatusReply.Error(new NotFoundException())
                Behaviors.same
            case Passivate =>
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            case other =>
                Behaviors.same
        }
    }

    /** A state used for clear organizations, i.e. organizations that can be
      * seen and hit by anyone with the right attributions
      *
      * @param o
      *   inject new organization state
      * @return
      */
    private def queryable(o: model.Organization): Behavior[Command] = {
        Behaviors.receiveMessage {
            case Get(replyTo) =>
                replyTo ! StatusReply.Success(Queryable(o))
                Behaviors.same
            case Update(replyTo, mut) =>
                updated(
                  replyTo,
                  model.Organization(
                    id = o.id,
                    label = mut.label,
                    queryable = mut.queryable,
                    OrganizationFK(
                      organizationTypeId = o.fk.organizationTypeId
                    ),
                    createdAt = o.createdAt,
                    updatedAt = LocalDateTime.now()
                  )
                )
            case Delete(replyTo) =>
                deleted(replyTo, o)
            case Passivate =>
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            case other =>
                log.warn(
                  "-- Organization#queryable > Received an unhandled message while being in Queryable state"
                )
                throw new UnsupportedOperationException(
                  s"<< Organization#queryable > could not treat or understand ${other.getClass.getSimpleName}"
                )
        }
    }

    private def updated(
        replyTo: ActorRef[UpdateResult],
        o: model.Organization
    ): Behavior[Command] = {
        ctx.pipeToSelf(store.put(o)) {
            case Success(value) =>
                WrappedUpdateResult(replyTo, UpdateSuccess(o))
            case Failure(cause) =>
                WrappedUpdateResult(replyTo, UpdateFailure(Some(cause)))
        }
        Behaviors.receiveMessage {
            case WrappedUpdateResult(replyTo, result) =>
                // return final result
                replyTo ! result
                result match {
                    case UpdateSuccess(_) | UpdateFailure(_) =>
                        // transition to a stable state and unstash
                        buffer.unstashAll(
                          if (o.queryable) queryable(o) else shadow(o)
                        )
                }
            case other =>
                // stash other msg while updating
                buffer.stash(other)
                Behaviors.same
        }
    }

    private def deleted(
        replyTo: ActorRef[StatusReply[DeleteResult]],
        o: model.Organization
    ): Behavior[Command] = {
        ctx.pipeToSelf(store.delete(o)) {
            case Failure(cause) =>
                WrappedDeleteResult(replyTo, DeleteFailure(Some(cause)))
            case Success(_) =>
                WrappedDeleteResult(replyTo, DeleteSuccess())
        }
        Behaviors.receiveMessage {
            case WrappedDeleteResult(replyTo, result) =>
                replyTo ! StatusReply.Success(result)
                result match {
                    case DeleteSuccess() =>
                        shard ! ClusterSharding.Passivate(ctx.self)
                        Behaviors.same
                    case DeleteFailure(_) =>
                        buffer.unstashAll(
                          if (o.queryable) queryable(o) else shadow(o)
                        )
                }
            case other =>
                buffer.stash(other)
                Behaviors.same
        }
    }
}
