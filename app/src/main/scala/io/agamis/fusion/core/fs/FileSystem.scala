package io.agamis.fusion.core.fs

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import io.agamis.fusion.core.db.datastores.sql._
import io.agamis.fusion.core.db.datastores.sql.generics.{EmailStore, LanguageStore, TextStore}
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper

object FileSystem {

    sealed trait Command
    case object GracefulShutdown extends Command
    case object InitDb extends Command

    def apply(): Behavior[FileSystem.Command] = {
        Behaviors.setup[FileSystem.Command](context => new FileSystem(context))
    }
}

class FileSystem(context: ActorContext[FileSystem.Command]) extends AbstractBehavior[FileSystem.Command](context) {
    import FileSystem._

    context.setLoggerName("io.agamis.fusion.fs")

    context.log.info("FileSystem Module started")

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

    override def onSignal: PartialFunction[Signal,Behavior[FileSystem.Command]] = {
        case PostStop =>
            context.log.info("FileSystem Module stopped")
            context.system.terminate()
            this
    }
}