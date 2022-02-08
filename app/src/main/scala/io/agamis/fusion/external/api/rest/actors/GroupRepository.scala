package io.agamis.fusion.external.api.rest.actors

import java.util.UUID
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import io.agamis.fusion.external.api.rest.dto.group.GroupDto


object GroupRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddGroup(group: GroupDto, replyTo: ActorRef[Response]) extends Command
    final case class GetGroupById(id: String, replyTo: ActorRef[Response]) extends Command
    final case class GetGroupByName(id: String, replyTo: ActorRef[Response]) extends Command
    final case class UpdateGroup(group: GroupDto, replyTo: ActorRef[Response]) extends Command
    final case class DeleteGroup(group: GroupDto, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddGroup(group, replyTo) =>
            println(group)
            replyTo ! OK     
            Behaviors.same
        case GetGroupById(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case GetGroupByName(name, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case UpdateGroup(group, replyTo) =>
            replyTo ! OK
            Behaviors.same 
        case DeleteGroup(group, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}