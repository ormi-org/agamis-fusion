package io.agamis.fusion

import com.typesafe.config.Config
import io.agamis.fusion.api.rest.Server
import org.apache.pekko.Done
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.event.slf4j.Logger
import org.apache.pekko.management.cluster.bootstrap.ClusterBootstrap
import org.apache.pekko.management.scaladsl.PekkoManagement
import org.slf4j

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object Core {

    val logger = Logger("Core")

    def startNode(
        behavior: Behavior[NotUsed],
        clusterName: String,
        appConfig: Config
    ): Future[Done] = {
        val logoIconStream = getClass.getResourceAsStream(
          "/io/agamis/fusion/media/fusion-logo.ans"
        )
        scala.io.Source
            .fromInputStream(logoIconStream)
            .getLines()
            .foreach(logger.info(_))
        logger.info(
          "Starting Agamis Fusion v0.1.0 - Copyright 2021-2023 The Open Rich Media Initiative"
        )
        val system = ActorSystem(behavior, clusterName, appConfig)
        logger.info(s"Started Agamis Fusion !")
        system.whenTerminated
    }

    object Behavior {
        object Root {
            def apply(): Behavior[NotUsed] =
                Behaviors.setup { context =>
                    val cluster = Cluster(context.system)

                    // Logging role of the current starting node
                    logger.info(
                      s"Starting node with roles: [${cluster.selfMember.roles.mkString(",")}]"
                    )

                    if (cluster.selfMember.hasRole("bootstrap")) {
                        // Start http management API
                        logger.info(s"Bootstrapping cluster...")
                        implicit lazy val ec: ExecutionContext =
                            context.system.executionContext;
                        PekkoManagement(context.system)
                            .start()
                            .onComplete({
                                case Success(uri) => {
                                    logger.info(
                                      s"Successfuly started pekko-management on URI { ${uri} }"
                                    )
                                }
                                case Failure(exception) => {
                                    logger.info(
                                      s"Failed to start pekko-management due to : ${exception.toString}"
                                    )
                                }
                            })
                        // Auto join cluster
                        ClusterBootstrap(context.system).start()
                    }
                    if (
                      cluster.selfMember.hasRole(
                        "fusion-node-data"
                      ) || cluster.selfMember.hasRole("fusion-node-data-proxy")
                    ) {
                        // Node type for handling datastore operations, resolving and caching queries results
                        // Start database connection at startup (only on data node; excluding proxies)
                        // implicit var wrapper: IgniteClientNodeWrapper = null
                        // if (cluster.selfMember.hasRole("fusion-node-data")) {
                        //     wrapper = IgniteClientNodeWrapper(context.system)
                        // }
                        // // Check role
                        // val TypeKey = EntityTypeKey[DataActor.Command](
                        //   DataActor.DataShardName
                        // )
                        // ClusterSharding(context.system).init(
                        //   Entity(TypeKey)(createBehavior =
                        //       ctx => DataActor(ctx.shard, ctx.entityId)
                        //   )
                        //       .withSettings(
                        //         ClusterShardingSettings(context.system)
                        //             .withRole("fusion-node-data")
                        //       )
                        // )
                        // implicit val system = context.system
                        // OrganizationShard.init
                    }
                    if (cluster.selfMember.hasRole("fusion-node-fs")) {
                        // TODO
                        // Node type for handling filesystem operations
                    }
                    if (cluster.selfMember.hasRole("fusion-node-session")) {
                        // TODO
                        // Node type for handling session save handling and life-cycle
                    }
                    if (cluster.selfMember.hasRole("fusion-node-app")) {
                        // TODO
                        // Node type for spawning embeded applications backend and serving frontend client
                        // val TypeKey = EntityTypeKey[]
                    }
                    if (cluster.selfMember.hasRole("fusion-node-rest-v1")) {
                        // Node type for serving v1 rest api endpoint
                        implicit val system = context.system
                        implicit val log    = context.log
                        val httpPort = context.system.settings.config
                            .getString(
                              "org.apache.pekko.http.server.default-http-port"
                            )
                        val interface =
                            if (cluster.selfMember.hasRole("cluster")) {
                                "0.0.0.0"
                            } else {
                                "localhost"
                            }
                        RestEndpoint[RestEndpoint.V1](interface, httpPort.toInt)
                    }
                    logger.info(s"Started Node from config !")
                    Behaviors.empty
                }
        }

        private case object RestEndpoint {
            sealed trait V1
            @nowarn
            def apply[V1](interface: String, port: Int)(implicit
                system: ActorSystem[_],
                log: slf4j.Logger
            ) = {
                implicit val ec: ExecutionContextExecutor =
                    system.executionContext

                val binding = Server.V1(interface, port, system)

                binding.onComplete {
                    case Success(binding) =>
                        log.info(
                          s"HTTP REST Server online at ip ${binding.localAddress} port $port"
                        )
                    case Failure(exception) => {
                        log.error(
                          s"HTTP REST Server failed to start due to : $exception"
                        )
                        system.terminate()
                    }
                }
            }
        }

        private case object DataNode {
            def apply()(implicit system: ActorSystem[_], log: slf4j.Logger) = {
                // implicit
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
