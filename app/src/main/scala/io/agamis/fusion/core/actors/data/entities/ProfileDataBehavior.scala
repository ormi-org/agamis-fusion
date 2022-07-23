package io.agamis.fusion.core.actors.data.entities

import io.agamis.fusion.core.db.datastores.sql.ProfileStore
import io.agamis.fusion.core.actors.data.DataActor
import java.util.UUID
import io.agamis.fusion.core.actors.data.entities.common.Identifiable
import io.agamis.fusion.core.actors.data.entities.common.Timetracked
import io.agamis.fusion.core.actors.data.entities.common.Pageable
import java.time.Instant
import akka.actor.typed.ActorRef
import io.agamis.fusion.core.db.models.sql.Profile
import scala.concurrent.ExecutionContext
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import java.sql.Timestamp

object ProfileDataBehavior {
  
  sealed trait Command extends DataActor.Command
  sealed trait Response extends DataActor.State

  // queries
  final case class Query(
    id: List[UUID],
    lastname: List[String],
    firstname: List[String],
    lastLogin: List[(String, Instant)],
    isActive: Option[Boolean],
    createdAt: List[(String, Instant)],
    updatedAt: List[(String, Instant)],
    limit: Long,
    offset: Long,
    orderBy: List[(String, Int)]
  ) extends Identifiable with Timetracked with Pageable

  // mutations
  final case class ProfileMutation(
    lastname: Option[String],
    firstname: Option[String],
    lastLogin: Option[Instant],
    isActive: Option[Boolean]
  )

  // commands
  final case class ExecuteQuery(
    replyTo: ActorRef[Response],
    query: Query
  ) extends Command

  final case class GetProfileById(
    replyTo: ActorRef[Response],
    id: UUID
  ) extends Command

  final case class CreateProfile(
    replyTo: ActorRef[Response],
    profileMutation: ProfileMutation
  ) extends Command

  final case class UpdateProfile(
    replyTo: ActorRef[Response],
    id: UUID,
    profileMutation: ProfileMutation
  ) extends Command

  final case class DeleteProfile(
    replyTo: ActorRef[Response],
    id: UUID
  ) extends Command

  // responses
  sealed trait Status {
    def msg: String
  }
  final case class Ok(msg: String = "Ok") extends Status
  final case class NotFound(msg: String = "Profile not found") extends Status
  final case class InternalException(msg: String = "An unhandled exception occured") extends Status

  sealed trait State extends Response {
    def status: Status
  }
  final case class SingleProfileState(entityId: String, result: Option[Profile], status: Status) extends State
  final case class MultiProfileState(entityId: String, result: List[Profile] = List(), status: Status) extends State
  final case class WrappedState(state: State, replyTo: ActorRef[Response]) extends Command

  def apply(state: DataActor.State)(implicit ec: ExecutionContext, wrapper: IgniteClientNodeWrapper): PartialFunction[
    (ActorContext[ProfileDataBehavior.Command], ProfileDataBehavior.Command),
    Behavior[ProfileDataBehavior.Command]
  ] = {
    implicit val store = new ProfileStore
    return {
      case (ctx: ActorContext[Command], wstate: WrappedState) => {
        // Send resulting state to original sender
        wstate.replyTo ! wstate.state
        // Update internal state for caching result
        ctx.log.debug(s"Caching result of entity{${state.entityId}}")
        Behaviors.receivePartial(apply(wstate.state))
      }
      // case (ctx: ActorContext[Command], eqy: ExecuteQuery) => {
      //   if (
      //     state match {
      //       case _: DataActor.EmptyState => true
      //       case s: State => {
      //         s.status match {
      //           case _: Ok => false
      //           case _ => true
      //         }
      //       }
      //     }
      //   ) {
      //     val query = eqy.query
      //     val filters = ProfileStore.GetProfilesFilters().copy(
      //       filters = List(ProfileStore.GetProfilesFilter().copy(
      //         id = if (query.id.nonEmpty) query.id.map { _.toString } else List(),
      //         lastname = if (query.lastname.nonEmpty) query.lastname else List(),
      //         firstname = if (query.firstname.nonEmpty) query.firstname else List(),
      //         lastLogin = if (query.lastLogin.nonEmpty) query.lastLogin.map { l => (l._1, Timestamp.from(l._2)) } else List(),
      //         // isActive = if (query.isActive.)
      //       ))
      //     )
      //   }
      // }
    }
  }
}
