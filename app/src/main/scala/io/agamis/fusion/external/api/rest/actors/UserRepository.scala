package io.agamis.fusion.external.api.rest.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.agamis.fusion.external.api.rest.dto.user.UserDto

import io.agamis.fusion.external.api.rest.authorization.JwtAuthorization

import io.agamis.fusion.core.data.security.utils.HashPassword
import java.time.Instant
import io.agamis.fusion.core.db.datastores.sql.UserStore
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext

object UserRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddUser(user: UserDto, replyTo: ActorRef[Response]) extends Command
    final case class GetUserById(id: String, token: String , replyTo: ActorRef[Response]) extends Command
    final case class GetUserByName(name: String, token: String, replyTo: ActorRef[UserDto]) extends Command
    final case class UpdateUser(user: UserDto, replyTo: ActorRef[Response]) extends Command
    final case class DeleteUser(user: UserDto, replyTo: ActorRef[Response]) extends Command
    final case class Authenfication(token: String, username: String, password: String, replyTo: ActorRef[Response]) extends Command

    def apply()(implicit system: ActorSystem[_]): Behavior[Command] = Behaviors.receiveMessage {
        case AddUser(user, replyTo) =>
            //hashPassword(user.password)
            //insert user in database
            replyTo ! OK
            Behaviors.same
        // case GetUserById(id, token, replyTo) =>
        //     implicit val ec: ExecutionContext = system.executionContext
        //     implicit val igniteWrapper: IgniteClientNodeWrapper = IgniteClientNodeWrapper(system)
        //     replyTo ! new UserStore().getUserById(id).transformWith({
        //         case Success(user) => Future.successful(user)
        //         case Failure(cause) => throw new Exception(cause)
        //     })

            Behaviors.same
        case GetUserByName(name, token, replyTo) => 
            replyTo ! UserDto(Some(UUID.randomUUID()),"","",List(),Some(Instant.now()),Some(Instant.now()))
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