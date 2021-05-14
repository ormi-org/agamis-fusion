package io.ogdt.fusion.core.fs.actors

import akka.Done
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

import io.ogdt.fusion.core.db.datastores.sql.ProfileStore
import io.ogdt.fusion.core.db.datastores.sql.UserStore
import io.ogdt.fusion.core.db.datastores.sql.FileSystemStore
import io.ogdt.fusion.core.db.datastores.sql.OrganizationStore
import io.ogdt.fusion.core.db.datastores.sql.OrganizationTypeStore
import io.ogdt.fusion.core.db.datastores.sql.generics.LanguageStore
import io.ogdt.fusion.core.db.datastores.sql.generics.TextStore

object FusionFS {

    sealed trait Command
    case object GracefulShutdown extends Command
    case object InitDb extends Command

    def apply(): Behavior[FusionFS.Command] = {
        Behaviors.setup[FusionFS.Command](context => new FusionFS(context))
    }
}

class FusionFS(context: ActorContext[FusionFS.Command]) extends AbstractBehavior[FusionFS.Command](context) {
    import FusionFS._

    context.setLoggerName("io.ogdt.fusion.fs")

    context.log.info("FusionFS Application started")

    override def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case GracefulShutdown =>
                context.log.info("Received graceful shutdown command...")
                Behaviors.stopped
            case InitDb => 
                context.log.info("Initializing database")
                implicit val igniteWrapper = IgniteClientNodeWrapper(context.system)
                implicit val mongoWrapper = ReactiveMongoWrapper(context.system)
                // Entities stores
                var userStore = new UserStore
                var profileStore = new ProfileStore
                var fsStore = new FileSystemStore
                var organizationStore = new OrganizationStore
                var organizationTypeStore = new OrganizationTypeStore

                // Generic stores
                var languageStore = new LanguageStore
                var textStore = new TextStore
                context.log.info("Initializing database")
                Behaviors.same
        }
    }

    override def onSignal: PartialFunction[Signal,Behavior[FusionFS.Command]] = {
        case PostStop =>
            context.log.info("FusionFS Application stopped")
            context.system.terminate()
            this
    }
}