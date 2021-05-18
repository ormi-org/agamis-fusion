package io.ogdt.fusion.external.http.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, ActorRef}

object OrganisationRepository {
    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddOrganisation(organisation: String, replyTo: ActorRef[Response]) extends Command
    final case class GetOrganisationById(id: String, replyTo: ActorRef[Response]) extends Command
    final case class ClearOrganisation(replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddOrganisation(profile, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case GetOrganisationById(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case ClearOrganisation(replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}