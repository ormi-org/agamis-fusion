package io.agamis.fusion.api.rest.routes

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.server.Directives._
import io.agamis.fusion.api.rest.common.marshalling.StringArrayUnmarshaller
import io.agamis.fusion.api.rest.controller.UserController
import io.agamis.fusion.api.rest.model.dto.user.UserApiJsonSupport
import io.agamis.fusion.api.rest.model.dto.user.UserMutation
import io.agamis.fusion.api.rest.model.dto.user.UserQuery
import io.agamis.fusion.core.services.UserService

/** Class User Routes
  *
  * @param system
  * @param data
  */
class UserRoutes()(implicit system: ActorSystem[_], userService: UserService)
    extends UserApiJsonSupport {

    import io.agamis.fusion.core.actors.data.entities.UserDataBehavior.Field

    implicit val sharding = ClusterSharding(system)

    val controller = new UserController(new UserService())

    val routes = concat(
      path("users") {
          concat(
            // query on all users
            get {
                parameters(
                  Field.ID.as[List[String]].optional,
                  Field.USERNAME.as[List[String]].optional,
                  Field.OFFSET.as[Int].optional,
                  Field.LIMIT.as[Int].optional,
                  Field.CREATED_AT.as[List[(String, String)]].optional,
                  Field.UPDATED_AT.as[List[(String, String)]].optional,
                  Field.ORDER_BY.as[List[(String, Int)]].optional,
                  Field.INCLUDE
                      .as(StringArrayUnmarshaller.commaSeparatedUnmarshaller)
                      .optional
                ).as(UserQuery.apply _) {
                    controller.getManyUsers(_)
                }
            },
            // create user
            post {
                entity(as[UserMutation]) {
                    controller.createUser(_)
                }
            }
          )
      },
      pathPrefix("users")(
        concat(
          // get by username
          get {
              parameters(Field.USERNAME.as[String]) {
                  controller.getUserByUsername(_)
              }
          },
          // user id segment
          path(Segment) { id: String =>
              concat(
                // get by id
                get {
                    parameters(Field.INCLUDE.as[List[String]].optional) {
                        include =>
                            controller
                                .getSingleUser(id, include.getOrElse(List()))
                    }
                },
                // update user
                put {
                    entity(as[UserMutation]) { uMut =>
                        controller.updateUser(id, uMut)
                    }
                },
                // delete user
                delete {
                    controller.deleteUser(id)
                }
              )
          }
        )
      )
    )
}
