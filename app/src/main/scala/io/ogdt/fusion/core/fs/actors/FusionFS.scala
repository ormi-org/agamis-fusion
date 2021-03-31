package io.ogdt.fusion.core.fs.actors

import akka.actor.typed.Behavior
import akka.actor.typed.Signal
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors

import io.ogdt.fusion.core.db.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastore.models.UserStore

import org.apache.ignite.IgniteException
import akka.actor.typed.ActorRef
import akka.Done

object FusionFS {

    sealed trait Command
    case object GracefulShutdown extends Command

    def apply(): Behavior[FusionFS.Command] = {
        Behaviors.setup[FusionFS.Command](context => new FusionFS(context))
    }
}

class FusionFS(context: ActorContext[FusionFS.Command]) extends AbstractBehavior[FusionFS.Command](context) {
    import FusionFS._

    val igniteWrapper = IgniteClientNodeWrapper(context.system)

    context.setLoggerName("io.ogdt.fusion.fs")

    context.log.info("FusionFS Application started")

    val userStore = new UserStore(igniteWrapper)
    userStore.getUsers(new Array[String](0))

    override def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case GracefulShutdown =>
                context.log.info("Received graceful shutdown command...")
                Behaviors.stopped
        }
    }

    override def onSignal: PartialFunction[Signal,Behavior[FusionFS.Command]] = {
        case PostStop =>
            context.log.info("FusionFS Application stopped")
            context.system.terminate()
            this
    }
}