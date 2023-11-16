package io.agamis.fusion.apps.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.engine.JavetEngineConfig
import io.agamis.fusion.apps.actors.enums.ContainerType
import io.agamis.fusion.apps.actors.enums.PROCESS
import io.agamis.fusion.apps.actors.enums.SEQUENTIAL_SCRIPT
import io.agamis.fusion.apps.engine.JavetContainer
import io.agamis.fusion.apps.engine.JavetProcessContainer
import io.agamis.fusion.core.actor.serialization.JsonSerializable
import io.agamis.fusion.env.EnvContainer
import org.slf4j.Logger

import java.io.File
import scala.concurrent.duration._

object ContainerActor {
    final val ShardName = "AppContainer"
    trait Command extends JsonSerializable
    // Command for confirming Idle on timeout
    final case class Idle() extends Command
    final case class Start()(val replyTo: ActorRef[StartResponse])
        extends Command
    final case class Recover(sessionId: String)(val replyTo: ActorRef[Start])
        extends Command
    final case class Stop()(val replyTo: ActorRef[_])    extends Command
    final case class Request()(val replyTo: ActorRef[_]) extends Command
    trait Response
    // errors
    trait StartResponse             extends Response
    final case class StartSuccess() extends StartResponse
    final case class StartFailure() extends StartResponse

    trait State extends JsonSerializable {
        def entityId: String
    }
    final case class InitState(
        entityId: String,
        path: String,
        containerType: ContainerType
    ) extends State

    final case class RunnableState(
        entityId: String,
        path: String,
        container: JavetContainer[_]
    ) extends State

    def IdleBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand],
        state: State
    ): Behavior[Command] = {
        Behaviors.receive {
            case (ctx: ActorContext[_], _: Idle) => {
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            }
            case (ctx: ActorContext[_], start: Start) => {
                state match {
                    case InitState(entityId, path, containerType) =>
                        implicit val logger: Logger = ctx.log
                        implicit val config         = new JavetEngineConfig
                        config.setJSRuntimeType(JSRuntimeType.Node)
                        start.replyTo ! StartSuccess()
                        RunningBehavior(
                          shard,
                          RunnableState(
                            entityId,
                            path,
                            containerType match {
                                case PROCESS => {
                                    config.setAllowEval(true)
                                    // TODO: implement files retrieval
                                    JavetContainer
                                        .ofProcessFile(new File(""), Map())
                                }
                                // TODO: implement files retrieval
                                case SEQUENTIAL_SCRIPT =>
                                    JavetContainer
                                        .ofScriptFile(new File(""))
                            }
                          )
                        )
                    case _: State => {
                        ctx.log.error(
                          String.format(
                            "<< ContainerActor:IdleBehavior#Start > pre-requisites check failed : actor is in wrong state [%s]",
                            state.toString()
                          )
                        )
                        start.replyTo ! StartFailure()
                        Behaviors.same
                    }
                }
            }
            case (_, _) => Behaviors.same
        }
    }

    def RunningBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand],
        state: RunnableState
    ): Behavior[Command] = {
        Behaviors.receive {
            case (ctx: ActorContext[_], _: Stop) => {
                // Run shutdown operations
                state.container match {
                    case process: JavetProcessContainer => {
                        process.close()
                    }
                    case _: JavetContainer[_] =>
                        ctx.log.warn(
                          "<< ContainerActor:RunningBehavior#Stop > pre-requisites check failed : script containers cannot be stopped"
                        )
                }
                IdleBehavior(shard, state)
            }
            case (_: ActorContext[_], _: Request) => {
                // Handle request

                Behaviors.same
            }
            case (_, _) => Behaviors.same
        }
    }

    def apply(
        shard: ActorRef[ClusterSharding.ShardCommand],
        entityId: String,
        appId: String,
        containerType: ContainerType
    ): Behavior[Command] = Behaviors.setup { context =>
        context.system.dispatchers.lookup(
          DispatcherSelector.fromConfig("emby-runtime-dispatcher")
        )

        try {
            val appContainerTimeout: Int =
                EnvContainer.getString("fusion.app.instance.timeout").toInt
            if (appContainerTimeout > 0)
                context.setReceiveTimeout(appContainerTimeout.seconds, Idle())
        } catch {
            case e: NumberFormatException => {
                val msg =
                    "<< ContainerActor#apply(ActorRef, String) > could not get a valid timeout configuration from `fusion.app.instance.timeout` : must be an integer"
                context.log.error(msg)
                throw new IllegalStateException(msg, e)
            }
        }

        val state = InitState(
          entityId,
          "",
          containerType
        )

        IdleBehavior(shard, state)
    }
}
