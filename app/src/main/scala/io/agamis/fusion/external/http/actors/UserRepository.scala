package io.agamis.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.agamis.fusion.external.http.entities.User

import io.agamis.fusion.external.http.authorization.JwtAuthorization

import io.agamis.fusion.core.data.security.utils.HashPassword

object UserRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class GetUserById(id: String, token: String , replyTo: ActorRef[Response]) extends Command
    final case class GetUserByName(name: String, token: String, replyTo: ActorRef[User]) extends Command
    final case class UpdateUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class DeleteUser(user: User, replyTo: ActorRef[Response]) extends Command
    final case class Authenfication(token: String, username: String, password: String, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddUser(user, replyTo) =>
            //hashPassword(user.password)
            //insert user in database
            replyTo ! OK
            Behaviors.same
        case GetUserById(id, token, replyTo) =>
            replyTo ! OK
            Behaviors.same
            
            // UserStore.getUserbyID(id).transformWith({
            //     case Success(user) => 
            //         if (user.isTokenValid(token)) {
            //             if (user.isTokenExpired(token)) { throw new Exception("Token expired") } 
            //             else { throw new Exception("User authorized") }
            //         } else { throw new Exception("Token invalid") }
            //     case Failure(cause) => throw new Exception(cause)
            // })

            Behaviors.same
        case GetUserByName(name, token, replyTo) => 
            replyTo ! User(UUID.randomUUID(),"","")
            Behaviors.same
        case UpdateUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same 
        case DeleteUser(user, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case Authenfication(token, username, password, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }
}