package io.agamis.fusion.external.api.rest.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, ActorRef}

object OrganizationTypeRepository {
    import io.agamis.fusion.external.api.rest.dto.organizationtype.OrganizationTypeDto

    sealed trait Status
    object Successful extends Status
    object Failed extends Status 

    sealed trait Response 
    case object OK extends Response
    final case class KO(reason: String) extends Response

    sealed trait Command
    final case class AddOrganizationType(organization: OrganizationTypeDto, replyTo: ActorRef[Response]) extends Command
    final case class GetOrganizationTypeById(id: String, replyTo: ActorRef[Response]) extends Command
    final case class UpdateOrganizationType(id: String, replyTo: ActorRef[Response]) extends Command
    final case class DeleteOrganizationType(id: String, replyTo: ActorRef[Response]) extends Command

    def apply(): Behavior[Command] = Behaviors.receiveMessage {
        case AddOrganizationType(organization, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case GetOrganizationTypeById(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case UpdateOrganizationType(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
        case DeleteOrganizationType(id, replyTo) =>
            replyTo ! OK
            Behaviors.same
    }

}