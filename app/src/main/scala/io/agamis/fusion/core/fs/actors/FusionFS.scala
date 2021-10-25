package io.agamis.fusion.core.fs.actors

import akka.Done
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}
import akka.actor.typed.scaladsl.{ActorContext, AbstractBehavior, Behaviors}

import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

import io.agamis.fusion.core.db.datastores.sql.ProfileStore
import io.agamis.fusion.core.db.datastores.sql.UserStore
import io.agamis.fusion.core.db.datastores.sql.FileSystemStore
import io.agamis.fusion.core.db.datastores.sql.OrganizationStore
import io.agamis.fusion.core.db.datastores.sql.OrganizationTypeStore
import io.agamis.fusion.core.db.datastores.sql.generics.LanguageStore
import io.agamis.fusion.core.db.datastores.sql.generics.TextStore
import io.agamis.fusion.core.db.datastores.sql.generics.EmailStore
import io.agamis.fusion.core.db.datastores.sql.GroupStore
import io.agamis.fusion.core.db.datastores.sql.ApplicationStore
import io.agamis.fusion.core.db.datastores.sql.PermissionStore

object FusionFileSystem {

    sealed trait Command
    case object GracefulShutdown extends Command
    case object InitDb extends Command

    def apply(): Behavior[FusionFileSystem.Command] = {
        Behaviors.setup[FusionFileSystem.Command](context => new FusionFS(context))
    }
}

class FusionFS(context: ActorContext[FusionFileSystem.Command]) extends AbstractBehavior[FusionFileSystem.Command](context) {
    import FusionFileSystem._

    context.setLoggerName("io.agamis.fusion.fs")

    context.log.info("FusionFileSystem Module started")

    override def onMessage(msg: Command): Behavior[Command] = {
        msg match {
            case GracefulShutdown =>
                context.log.info("Received graceful shutdown command...")
                Behaviors.stopped
            case InitDb => 
                context.log.info("Initializing database")
                implicit val igniteWrapper: IgniteClientNodeWrapper = IgniteClientNodeWrapper(context.system)
                implicit val mongoWrapper: ReactiveMongoWrapper = ReactiveMongoWrapper(context.system)
                // Entities stores
                var userStore = new UserStore
                var profileStore = new ProfileStore
                var fsStore = new FileSystemStore
                var organizationStore = new OrganizationStore
                var organizationTypeStore = new OrganizationTypeStore
                var groupStore = new GroupStore
                var applicationStore = new ApplicationStore
                var permissionStore = new PermissionStore

                // Generic stores
                var languageStore = new LanguageStore
                var textStore = new TextStore
                var emailStore = new EmailStore
                context.log.info("Database initialized")
                Behaviors.same
        }
    }

    override def onSignal: PartialFunction[Signal,Behavior[FusionFileSystem.Command]] = {
        case PostStop =>
            context.log.info("FusionFileSystem Module stopped")
            context.system.terminate()
            this
    }
}