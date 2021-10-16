package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import scala.util.{Success, Failure}

import io.ogdt.fusion.external.http.entities.Permission

import io.ogdt.fusion.external.http.authorization.JwtAuthorization

import io.ogdt.fusion.core.data.security.utils.HashPassword

object PermissionRepository {

    sealed trait Status
    object Successful extends Status
    object Failed extends Status

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddPermission(permission: Permission, token: String, replyTo: ActorRef[Response]) extends Command
    final case class GetPermissionById(id: String, token: String , replyTo: ActorRef[Response]) extends Command
    final case class GetPermissionByName(name: String, token: String, replyTo: ActorRef[Permission]) extends Command
    final case class UpdatePermission(permission: Permission, replyTo: ActorRef[Response]) extends Command
    final case class DeletePermission(permission: Permission, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddPermission(permission, token, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case GetPermissionById(id, token, replyTo) =>
            replyTo ! OK
            Behaviors.same

            Behaviors.same
        case GetPermissionByName(name, token, replyTo) => 
            Behaviors.same
        case UpdatePermission(permission, replyTo) =>
            replyTo ! OK
            Behaviors.same 
        case DeletePermission(permission, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }
}