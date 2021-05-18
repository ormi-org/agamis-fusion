package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import io.ogdt.fusion.external.http.entities.User

object UserRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class GetUserByPath(name: String, replyTo: ActorRef[Response]) extends Command
    final case class GetUserById(id: String, replyTo: ActorRef[Response]) extends Command
    final case class UpdateUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class DeleteUser(group: User, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddUser(group, replyTo) =>
            println(group)
            replyTo ! OK     
            Behaviors.same
        case GetUserByPath(name, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case GetUserById(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case UpdateUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same 
        case DeleteUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }
}