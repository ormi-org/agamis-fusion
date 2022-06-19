package io.agamis.fusion.external.api.rest.dto.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

final case class UserMutation (
  username: String,
  password: String
)

object UserMutation {
  def apply(
    username: String,
    password: String
  ): UserMutation = {
    UserMutation(username, password)
  }
}

trait UserMutationJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userMutationFormat: RootJsonFormat[UserMutation] = jsonFormat2(UserMutation.apply)
}

object UserMutationJsonProtocol extends UserMutationJsonSupport