package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import io.ogdt.fusion.external.http.entities.Group


object GroupRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddGroup(group: Group, replyTo: ActorRef[Response]) extends Command
    final case class GetGroupById(id: String, replyTo: ActorRef[Response]) extends Command
    final case class GetGroupByName(id: String, replyTo: ActorRef[Response]) extends Command
    final case class UpdateGroup(group: Group, replyTo: ActorRef[Response]) extends Command
    final case class DeleteGroup(group: Group, replyTo: ActorRef[Response]) extends Command

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