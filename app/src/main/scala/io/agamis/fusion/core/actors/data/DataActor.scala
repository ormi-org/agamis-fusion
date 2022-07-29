package io.agamis.fusion.core.actors.data

import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
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

object DataActor {

  final val DataShardName = "Data"
  trait Field
  trait Command
  trait Response
  trait State {
    def entityId: String
  }
  final case class EmptyState(entityId: String) extends State

  def apply(entityId: String): Behavior[Command] = Behaviors.setup { context =>
    implicit val wrapper: IgniteClientNodeWrapper = IgniteClientNodeWrapper(context.system)

    implicit val ec: ExecutionContext = 
      context.system.dispatchers.lookup(
        DispatcherSelector.fromConfig("db-operations-dispatcher")
      )
    
    Behaviors.receivePartial(
      UserDataBehavior(EmptyState(entityId)).asInstanceOf[PartialFunction[(ActorContext[Command], Command), Behavior[Command]]]
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