package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.ogdt.fusion.external.http.entities.User
import io.ogdt.fusion.external.http.authorization.JwtAuthorization

object UserRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class GetUserById(token: String, id: String, replyTo: ActorRef[User]) extends Command
    final case class GetUserByName(name: String, replyTo: ActorRef[User]) extends Command
    final case class UpdateUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class DeleteUser(user: User, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddUser(user, replyTo) =>
            if (user.checkPassword(user.username, user.password)) {
                replyTo ! OK
            } else {
                replyTo ! KO("Problem with the creation of user")
            }
            Behaviors.same
        case GetUserById(token,id, replyTo) =>
            if (user.isTokenExpired(token)) {
                throw new Exception("Token expired")
            } else if (user.isTokenValid(token)) {
                UserStore.getUserbyID(id).transformWith({
                    case Success(user) => replyTo ! user
                    case Failure(cause) => throw new Exception(cause)
                })
            } else {
                throw new Exception("Token invalid") 
            }
            Behaviors.same
        case GetUserByName(name, replyTo) => 
            replyTo ! User(UUID.randomUUID(),"","")
            Behaviors.same
        case UpdateUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same 
        case DeleteUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }
}