package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.ogdt.fusion.external.http.authorization.JwtAuthorization

import io.ogdt.fusion.core.data.security.utils.HashPassword

object AuthenticationRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class Login(username: String, password: String, replyTo: ActorRef[Response]) extends Command
    final case class SendRefreshToken(token: String, replyTo: ActorRef[Response]) extends Command
    final case class AuthenticationWithToken(token: String , replyTo: ActorRef[Response]) extends Command
    final case class AuthenticationWithRefreshToken(token: String, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case Login(username,password, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case SendRefreshToken(token, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case AuthenticationWithToken(token, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case AuthenticationWithRefreshToken(token, replyTo) => 
            Behaviors.same
    }
}