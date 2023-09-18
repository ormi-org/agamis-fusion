package io.agamis.fusion.core.actors.data.entities

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.core.db.datastores.sql.GroupStore
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import scala.concurrent.ExecutionContext

object GroupDataBehavior {

  sealed trait Command extends DataActor.Command
  sealed trait Response extends DataActor.State

  // responses
  sealed trait Status {
    def msg: String
  }

  sealed trait State extends Response {
    def status: Status
  }

  final case class WrappedState(state: State, replyTo: ActorRef[Response]) extends Command

  def apply(state: DataActor.State)(implicit ec: ExecutionContext, wrapper: IgniteClientNodeWrapper): PartialFunction[
    (ActorContext[GroupDataBehavior.Command], GroupDataBehavior.Command),
    Behavior[GroupDataBehavior.Command]
  ] = {
    implicit val store = new GroupStore
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