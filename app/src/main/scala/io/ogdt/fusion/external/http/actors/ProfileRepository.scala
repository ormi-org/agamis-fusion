package io.ogdt.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import io.ogdt.fusion.external.http.entities.Profile
import io.ogdt.fusion.external.http.entities.User


object ProfileRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddProfile(profile: Profile, replyTo: ActorRef[Response]) extends Command
    final case class GetProfileById(id: UUID, replyTo: ActorRef[Option[Profile]]) extends Command
    final case class ClearProfiles(replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddProfile(profile, replyTo) =>
            println(profile)
            replyTo ! OK
            Behaviors.same
        case GetProfileById(id, replyTo) =>
            replyTo ! Some(Profile(
                id =  Some(UUID.randomUUID()), 
                lastName = "test", 
                firstName = "test", 
                email = "test@gmail.com", 
                lastLogin = "test", 
                userId = Some(UUID.randomUUID())
            ))
            Behaviors.same
        case ClearProfiles(replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}