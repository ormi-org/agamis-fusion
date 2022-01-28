package io.agamis.fusion.external.api.rest.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.agamis.fusion.external.api.rest.authorization.JwtAuthorization

import io.agamis.fusion.core.data.security.utils.HashPassword
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext

object AuthenticationRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class Login(username: String, password: String, replyTo: ActorRef[Response]) extends Command
    final case class Logout(token: String, refreshToken: String, replyTo: ActorRef[Response]) extends Command
    final case class SendRefreshToken(token: String, replyTo: ActorRef[Response]) extends Command
    final case class AuthenticationWithToken(token: String , replyTo: ActorRef[Response]) extends Command
    final case class AuthenticationWithRefreshToken(token: String, replyTo: ActorRef[Response]) extends Command

    def apply()(implicit system: ActorSystem[_]): Behavior[Command] = 

        Behaviors.setup { context =>

            implicit val ec: ExecutionContext = context.executionContext

            Behaviors.receiveMessage {
                case Login(username,password,replyTo) =>
                    replyTo ! OK
                    Behaviors.same
                case Logout(token,refreshToken,replyTo) =>
                    replyTo ! OK
                    Behaviors.same
                case SendRefreshToken(token,replyTo) =>
                    replyTo ! OK
                    Behaviors.same
                case AuthenticationWithToken(token,replyTo) =>
                    replyTo ! OK
                    Behaviors.same
                case AuthenticationWithRefreshToken(token,replyTo) => 
                    Behaviors.same
            }
    }
}