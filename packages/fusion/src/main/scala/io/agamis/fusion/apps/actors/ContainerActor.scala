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

object ContainerActor {
    final val ShardName = "AppContainer"
    trait Command extends JsonSerializable
    // Command for confirming Idle on timeout
    final case class Idle() extends Command
    final case class Start(zipPath: String) extends Command
    final case class Recover(sessionId: String) extends Command
    final case class Stop() extends Command
    trait Response
    trait State extends JsonSerializable {
        def entityId: String
    }
    final case class StartupState(entityId: String) extends State

    def IdleBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand]
    ): PartialFunction[
      (ActorContext[Command], Command),
      Behavior[Command]
    ] = {
        return {
            case (ctx: ActorContext[_], _: Idle) => {
                shard ! ClusterSharding.Passivate(ctx.self)
                Behaviors.same
            }
        }
    }

    def StartupBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand]
    ): PartialFunction[
      (ActorContext[Command], Command),
      Behavior[Command]
    ] = {
        // Run startup operations then release to running state
        return RunningBehavior(shard)
    }

    def RunningBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand]
    ): PartialFunction[
        (ActorContext[Command], Command),
        Behavior[Command]
    ] = {
        return {
            case (ctx: ActorContext[_], _: Stop) => {
                return ShutdownBehavior(shard)
            }
        }
    }

    def ShutdownBehavior(
        shard: ActorRef[ClusterSharding.ShardCommand]
    ): PartialFunction[
        (ActorContext[Command], Command),
        Behavior[Command]
    ] = {
        // Graceful Shutdown operations
        return IdleBehavior(shard)
    }

    def apply(
        shard: ActorRef[ClusterSharding.ShardCommand],
        entityId: String
    ): Behavior[Command] = Behaviors.setup { context =>
        val ec: ExecutionContext =
            context.system.dispatchers.lookup(
                DispatcherSelector.fromConfig("emby-runtime-dispatcher")
            )

        try {
            val appContainerTimeout: Int = EnvContainer.getString("fusion.app.instance.timeout").toInt
        }
        catch {
            case nfe: NumberFormatException => {
                context.log.error("<< ContainerActor#apply(ActorRef, String) > could not get a valid timeout configuration from `fusion.app.instance.timeout` : must be an integer")
            }
        } finally {
            shard ! ClusterSharding.Passivate(context.self)
            context.system.terminate()
            return Behaviors.empty
        }
        
        Behaviors.receivePartial(
            IdleBehavior(shard)
            .orElse(StartupBehavior(shard))
            .orElse(RunningBehavior(shard))
            .orElse(ShutdownBehavior(shard))
        )
    }
}
