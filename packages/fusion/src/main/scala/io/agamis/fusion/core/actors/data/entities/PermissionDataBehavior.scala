package io.agamis.fusion.core.actors.data.entities

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.core.actors.data.entities.common.Identifiable
import io.agamis.fusion.core.actors.data.entities.common.Pageable
import io.agamis.fusion.core.actors.data.entities.common.Timetracked
import io.agamis.fusion.core.db.datastores.sql.PermissionStore
import io.agamis.fusion.core.db.models.sql.Permission
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.datastores.sql.common.Filter
import io.agamis.fusion.core.db.datastores.typed.sql.EntityFilters

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import _root_.io.agamis.fusion.core.db.datastores.sql.PermissionStore

object PermissionDataBehavior {

  sealed trait Command extends DataActor.Command
  sealed trait Response extends DataActor.State

  // responses
  sealed trait Status {
    def msg: String
  }
  final case class Ok(msg: String = "Ok") extends Status
  final case class NotFound(msg: String = "User not found") extends Status
  final case class InternalException(msg: String = "An unhandled exception occured") extends Status

  sealed trait State extends Response {
    def status: Status
  }

  final case class WrappedState(state: State, replyTo: ActorRef[Response]) extends Command

  def apply(state: DataActor.State)(implicit ec: ExecutionContext, wrapper: IgniteClientNodeWrapper): PartialFunction[
    (ActorContext[PermissionDataBehavior.Command], PermissionDataBehavior.Command),
    Behavior[PermissionDataBehavior.Command]
  ] = {
    implicit val store = new PermissionStore
    return {
        case (ctx: ActorContext[Command], wstate: WrappedState) => {
          // Send resulting state to original sender
          wstate.replyTo ! wstate.state
          // Update internal state for caching result
          ctx.log.debug(s"Caching result of entity{${state.entityId}}")
          Behaviors.receivePartial(apply(wstate.state))
        }
    }
  }
}