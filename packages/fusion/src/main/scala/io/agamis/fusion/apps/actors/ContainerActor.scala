package io.agamis.fusion.apps.actors

import io.agamis.fusion.core.actors.serialization.JsonSerializable
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.DispatcherSelector
import scala.concurrent.ExecutionContext
import io.agamis.fusion.env.EnvContainer
import scala.concurrent.duration._
import io.agamis.fusion.apps.actors.enums.ContainerType._
import io.agamis.fusion.apps.engine.JavetContainer
import java.io.File
import io.agamis.fusion.fs.lib.Tree
import org.slf4j.Logger
import org.apache.ignite.internal.util.scala.impl
import com.caoccao.javet.interop.engine.JavetEngineConfig
import com.caoccao.javet.enums.JSRuntimeType
import io.agamis.fusion.apps.engine.JavetProcessContainer

object ContainerActor {
    final val ShardName = "AppContainer"
    trait Command extends JsonSerializable
    // Command for confirming Idle on timeout
    final case class Idle() extends Command
    final case class Start()(val replyTo: ActorRef[StartResponse]) extends Command
    final case class Recover(sessionId: String)(val replyTo: ActorRef[Start]) extends Command
    final case class Stop()(val replyTo: ActorRef[_]) extends Command
    final case class Request()(val replyTo: ActorRef[_]) extends Command
    trait Response
    // errors
    trait StartResponse extends Response
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
    ): PartialFunction[
      (ActorContext[Command], Command),
      Behavior[Command]
    ] = {
        return {
            case (ctx: ActorContext[_], _: Idle) => {
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            }
            case (ctx: ActorContext[_], start: Start) => {
                state match {
                    case InitState(entityId, path, containerType) =>
                        implicit val logger: Logger = ctx.log
                        implicit val config = new JavetEngineConfig
                        config.setJSRuntimeType(JSRuntimeType.Node)
                        start.replyTo ! StartSuccess()
                        return RunningBehavior(shard, RunnableState(
                            entityId, path, containerType match {
                                case PROCESS => {
                                    config.setAllowEval(true)
                                    // TODO: implement files retrieval
                                    JavetContainer.ofProcessFile(new File(""), Map())
                                }
                                // TODO: implement files retrieval
                                case SEQUENTIAL_SCRIPT => JavetContainer.ofScriptFile(new File(""))
                            }
                        ))
                    case _: State => {
                        ctx.log.error(String.format("<< ContainerActor:IdleBehavior#Start > pre-requisites check failed : actor is in wrong state [%s]", state.toString()))
                        start.replyTo ! StartFailure()
                        Behaviors.same
                    }
                }
            }
        }
    }

    def RunningBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand],
        state: RunnableState
    ): PartialFunction[
      (ActorContext[Command], Command),
      Behavior[Command]
    ] = {
        return {
            case (ctx: ActorContext[_], _: Stop) => {
                // Run shutdown operations
                state.container match {
                    case process: JavetProcessContainer => {
                        process.close()
                    }
                    case script: JavetContainer[_] => ctx.log.warn("<< ContainerActor:RunningBehavior#Stop > pre-requisites check failed : script containers cannot be stopped")
                }
                return IdleBehavior(shard, state)
            }
            case (ctx: ActorContext[_], _: Request) => {
                // Handle request

                Behaviors.same
            }
        }
    }

    def apply(
        shard: ActorRef[ClusterSharding.ShardCommand],
        entityId: String,
        appId: String,
        containerType: ContainerType
    ): Behavior[Command] = Behaviors.setup { context =>
        val ec: ExecutionContext =
            context.system.dispatchers.lookup(
              DispatcherSelector.fromConfig("emby-runtime-dispatcher")
            )

        try {
            val appContainerTimeout: Int =
                EnvContainer.getString("fusion.app.instance.timeout").toInt
            if (appContainerTimeout > 0)
                context.setReceiveTimeout(appContainerTimeout.seconds, Idle())
        } catch {
            case nfe: NumberFormatException => {
                context.log.error(
                  "<< ContainerActor#apply(ActorRef, String) > could not get a valid timeout configuration from `fusion.app.instance.timeout` : must be an integer"
                )
            }
        }

        val state = InitState(
          entityId,
          "",
          containerType
        )

        Behaviors.receivePartial(IdleBehavior(shard, state))
    }
}
