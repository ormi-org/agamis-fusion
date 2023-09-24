package io.agamis.fusion.core.actor.entity

import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.StashBuffer
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import io.agamis.fusion.core.actor.common.enum.Dispatcher
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.env.EnvContainer
import io.agamis.fusion.core.model
import io.agamis.fusion.core.db.datastore.cache.OrganizationStore

import scala.concurrent.ExecutionContext
import scala.util.Try

object Organization {
    trait Command
    private final case class InitialState(initialValue: model.Organization)
        extends Command
    private final case class StoreError(cause: Throwable) extends Command

    sealed trait Response
    final case class State(org: model.Organization) extends Response

    val TypeKey: EntityTypeKey[Command] =
        EntityTypeKey[Command](getClass().getSimpleName())

    def apply(
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
    ctx: ActorContext[Organization.Command],
    buffer: StashBuffer[Organization.Command],
    entityId: String,
    store: OrganizationStore
)(implicit _ec: ExecutionContext) {
    import Organization._

    val log = ctx.log

    private def bootstrap(): Behavior[Command] = {
        // ctx.pipeToSelf(store.getOrganizationById(entityId)) {
        //     case Success(org) => InitialState(org)
        //     case Failure(e)   => StoreError(e)
        // }

        Behaviors.receiveMessage {
            case InitialState(o) =>
                buffer.unstashAll(
                  if (o.queryable) queryable(o) else shadow(o)
                )
            case StoreError(cause) =>
                log.error(
                  "<< Organization#bootstrap > Could not retrieve organization from database",
                  cause
                )
                throw cause
            case other =>
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
    private def shadow(o: model.Organization): Behavior[Command] = ???

    /** A state used for clear organizations, i.e. organizations that can be
      * seen and hit by anyone with the right attributions
      *
      * @param o
      *   inject new organization state
      * @return
      */
    private def queryable(o: model.Organization): Behavior[Command] = ???
}
