package io.agamis.fusion.core.actors.data

import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import scala.concurrent.ExecutionContext

object DataActor {

  final val DataShardName = "Data"
  trait Field
  trait Command
  trait Response
  trait State {
    def entityId: String
  }
  final case class EmptyState(entityId: String) extends State

  def apply(entityId: String): Behavior[Command] = Behaviors.setup { context =>
    implicit val wrapper: IgniteClientNodeWrapper = IgniteClientNodeWrapper(context.system)

    implicit val ec: ExecutionContext = 
      context.system.dispatchers.lookup(
        DispatcherSelector.fromConfig("db-operations-dispatcher")
      )
    
    Behaviors.receivePartial(
      UserDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]]
    )
  }
}