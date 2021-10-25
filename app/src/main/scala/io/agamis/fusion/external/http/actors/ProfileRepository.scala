package io.agamis.fusion.external.http.actors

import java.util.UUID

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Signal, PostStop, ActorRef}

import io.agamis.fusion.external.http.entities.Profile
import io.agamis.fusion.external.http.entities.User


object ProfileRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddProfile(profile: Profile, replyTo: ActorRef[Response]) extends Command
    final case class GetProfileById(profileId: String, replyTo: ActorRef[Response]) extends Command
    final case class UpdateProfile(profileId: String, replyTo: ActorRef[Response]) extends Command
    final case class DeleteProfile(profileId: String, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddProfile(profile, replyTo) =>
            println(profile)
            replyTo ! OK
            Behaviors.same
        case GetProfileById(profileId, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case UpdateProfile(profileId, replyTo) => 
            replyTo ! OK 
            Behaviors.same
        case DeleteProfile(profileId, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}