package io.agamis.fusion.core.actors.data

import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.core.actors.serialization.JsonSerializable
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import scala.concurrent.ExecutionContext
import io.agamis.fusion.core.actors.data.entities.ProfileDataBehavior
import io.agamis.fusion.core.actors.data.entities.PermissionDataBehavior
import io.agamis.fusion.core.actors.data.entities.OrganizationTypeDataBehavior
import io.agamis.fusion.core.actors.data.entities.OrganizationDataBehavior
import io.agamis.fusion.core.actors.data.entities.GroupDataBehavior
import io.agamis.fusion.core.actors.data.entities.FileSystemDataBehavior
import io.agamis.fusion.core.actors.data.entities.ApplicationDataBehavior
import io.agamis.fusion.core.actors.data.entities.TextDataBehavior
import io.agamis.fusion.core.actors.data.entities.LanguageDataBehavior
import io.agamis.fusion.core.actors.data.entities.EmailDataBehavior
import io.agamis.fusion.env.EnvContainer
import io.agamis.fusion.core.actors.common.CachePolicy
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import scala.concurrent.duration._

object DataActor {

  final val DataShardName = "Data"
  trait Field
  trait Command extends JsonSerializable
  final case class Idle() extends Command
  trait Response
  trait State extends JsonSerializable {
    def entityId: String
  }
  final case class EmptyState(entityId: String) extends State

  def IdleBehavior(shard: ActorRef[ClusterSharding.ShardCommand]): PartialFunction[(ActorContext[Command], Command), Behavior[Command]] = {
    return {
      case (ctx: ActorContext[_], _: Idle) => {
        shard ! ClusterSharding.Passivate(ctx.self)
        Behaviors.same
      }
    }
  }

  def apply(shard: ActorRef[ClusterSharding.ShardCommand], entityId: String)(implicit wrapper: IgniteClientNodeWrapper): Behavior[Command] = Behaviors.setup { context =>

    implicit val ec: ExecutionContext = 
      context.system.dispatchers.lookup(
        DispatcherSelector.fromConfig("db-operations-dispatcher")
      )
    
    implicit val cachingPolicy: String = EnvContainer.getString("fusion.cache.sql.when")
    implicit val cachingTimeToLive: Int = cachingPolicy match {
      case CachePolicy.ALWAYS => EnvContainer.getString("fusion.cache.sql.ttl").toInt
      case CachePolicy.ON_READ => EnvContainer.getString("fusion.cache.sql.ttl").toInt
      case CachePolicy.NEVER  => 0
    }

    if (cachingTimeToLive > 0) context.setReceiveTimeout(cachingTimeToLive.seconds, Idle())
    
    Behaviors.receivePartial(
      IdleBehavior(shard)
      .orElse(UserDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(ProfileDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(PermissionDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(OrganizationTypeDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(OrganizationDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(GroupDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(FileSystemDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(ApplicationDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(TextDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(LanguageDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
      .orElse(EmailDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]])
    )
  }
}