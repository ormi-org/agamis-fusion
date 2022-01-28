package io.agamis.fusion.external.api.rest.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.agamis.fusion.core.db.models.sql.Profile
import spray.json.DefaultJsonProtocol

final case class ProfileDto(
 id: Option[String],
 lastName: String,
 firstName: String,
 mainEmail: String,
 emails: List[String],
 groups: Option[List[GroupDto]],
 permissions: Option[List[PermissionDto]],
 organization: Option[OrganizationDto],
 lastLogin: String,
 userId: Option[String],
 createdAt: String,
 updatedAt: String,
)

object ProfileDto {

  implicit def apply(dto: ProfileDto): Profile = {
    Profile(

    )
  }

  implicit def apply(doc: Profile): ProfileDto = {
    ProfileDto(
      
    )
  }
}

trait ProfileJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val profileFormat = jsonFormat6(ProfileDto)
}