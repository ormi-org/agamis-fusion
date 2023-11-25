package io.agamis.fusion.api.rest.model.dto.organization

import io.agamis.fusion.api.rest.model.dto.common.JsonFormatters._
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import java.util.UUID

/** Organization DTO with JSON
  *
  * @param label
  * @param queryable
  * @param organizationTypeId
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
