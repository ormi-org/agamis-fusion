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
    final case class GetUserById(id: UUID, replyTo: ActorRef[Option[User]]) extends Command
    final case class ClearUsers(replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddUser(user, replyTo) =>
            println(user)
            replyTo ! OK
            Behaviors.same
        case GetUserById(id, replyTo) =>
            replyTo ! Some(User(
                id = UUID.randomUUID(),
                username = "John",
                password = "password"
            ))
            Behaviors.same
        case ClearUsers(replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}