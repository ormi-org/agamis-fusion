package io.agamis.fusion

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import io.agamis.fusion.core.db.datastores.sql._
import io.agamis.fusion.core.db.datastores.sql.generics.{EmailStore, LanguageStore, TextStore}
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.wrappers.mongo.ReactiveMongoWrapper
import akka.NotUsed
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.cluster.typed.Cluster
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import scala.concurrent.ExecutionContextExecutor
import io.agamis.fusion.external.api.rest.Server
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import akka.Done
import akka.actor
import akka.management.scaladsl.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import org.slf4j.Logger

object Core {

    def startNode(behavior: Behavior[NotUsed], clusterName: String, appConfig: Config): Future[Done] = {
        val system = ActorSystem(behavior, clusterName, appConfig)
        system.whenTerminated
    }

    object Behavior {
        object Root {
            def apply(port: Int, defaultPort: Int): Behavior[NotUsed] =
                Behaviors.setup { context =>
                    implicit val classicSystem: actor.ActorSystem = TypedActorSystemOps(context.system).toClassic
                    val cluster = Cluster(context.system)

                    // Logging role of the current starting node
                    context.log.info(s"starting node with roles: [${cluster.selfMember.roles.mkString(",")}]")

                    if (cluster.selfMember.hasRole("k8s")) {
                        // Start http management API
                        AkkaManagement(classicSystem).start()
                        // Auto join k8s based cluster
                        ClusterBootstrap(classicSystem).start()
                    } else
                    if (cluster.selfMember.hasRole("fusion-node-data")) {
                        // TODO
                        // Node type for handling datastore operations, resolving and caching queries results
                    } else
                    if (cluster.selfMember.hasRole("fusion-node-fs")) {
                        // TODO
                        // Node type for handling filesystem operations
                    } else
                    if (cluster.selfMember.hasRole("fusion-node-session")) {
                        // TODO
                        // Node type for handling session save handling and life-cycle
                    } else
                    if (cluster.selfMember.hasRole("fusion-node-app")) {
                        // TODO
                        // Node type for spawning embeded applications backend and serving frontend client
                    } else
                    if (cluster.selfMember.hasRole("fusion-node-rest-v1")) {
                        // Node type for serving v1 rest api endpoint
                        implicit val system = context.system
                        implicit val log = context.log
                        val httpPort = context.system.settings.config.getString("akka.http.server.default-http-port")
                        val interface = if (cluster.selfMember.hasRole("docker") || cluster.selfMember.hasRole("k8s")) {
                            "0.0.0.0"
                        } else {
                            "localhost"
                        }
                        RestEndpoint[RestEndpoint.V1](interface, httpPort.toInt)
                    }
                    Behaviors.empty
                }
        }

        private case object FusionData {
            sealed trait Repo
            sealed trait Broker
        }

        private case object RestEndpoint {
            sealed trait V1
            def apply[V1](interface: String, port: Int)(implicit system: ActorSystem[_], log: Logger) = {
                implicit val ec: ExecutionContextExecutor = system.executionContext
                // Datastore Sharded actors refs HERE

                val binding = Server.V1(interface, port, system)

                binding.foreach { binding =>
                    log.info(s"HTTP REST Server online at ip ${binding.localAddress} port $port")
                }
            }
            
        }
    }

    // private object 
}

// class Core(context: ActorContext[Core.Command]) extends AbstractBehavior[Core.Command](context) {
//     import Core._

//     context.setLoggerName("io.agamis.fusion.fs")

//     context.log.info("Core Module started")

//     override def onMessage(msg: Command): Behavior[Command] = {
//         msg match {
//             case GracefulShutdown =>
//                 context.log.info("Received graceful shutdown command...")
//                 Behaviors.stopped
//             case InitDb => 
//                 context.log.info("Initializing database")
//                 implicit val igniteWrapper: IgniteClientNodeWrapper = IgniteClientNodeWrapper(context.system)
//                 implicit val mongoWrapper: ReactiveMongoWrapper = ReactiveMongoWrapper(context.system)
//                 // Entities stores
//                 var userStore = new UserStore
//                 var profileStore = new ProfileStore
//                 var fsStore = new FileSystemStore
//                 var organizationStore = new OrganizationStore
//                 var organizationTypeStore = new OrganizationTypeStore
//                 var groupStore = new GroupStore
//                 var applicationStore = new ApplicationStore
//                 var permissionStore = new PermissionStore

//                 // Generic stores
//                 var languageStore = new LanguageStore
//                 var textStore = new TextStore
//                 var emailStore = new EmailStore
//                 context.log.info("Database initialized")
//                 Behaviors.same
//         }
//     }

//     override def onSignal: PartialFunction[Signal,Behavior[Core.Command]] = {
//         case PostStop =>
//             context.log.info("Core Module stopped")
//             context.system.terminate()
//             this
//     }
// }