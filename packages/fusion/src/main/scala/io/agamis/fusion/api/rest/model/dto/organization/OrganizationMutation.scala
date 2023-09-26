package io.agamis.fusion.api.rest.model.dto.organization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import spray.json._

import java.util.UUID

/** Organization DTO with JSON
  *
  * @param id
  * @param label
  * @param type
  * @param queryable
  * @param profiles
  * @param groups
  * @param defaultFileSystem
  * @param fileSystems
  * @param applications
  * @param createdAt
  * @param updatedAt
  */
final case class OrganizationMutation(
    label: String,
    queryable: Boolean,
    organizationTypeId: UUID
)

trait OrganizationMutationJsonSupport
    extends SprayJsonSupport
    with DefaultJsonProtocol {

    implicit val mutFormat: RootJsonFormat[OrganizationMutation] =
        jsonFormat3(OrganizationMutation.apply)
}

object OrganizationMutationJsonProtocol extends OrganizationMutationJsonSupport
