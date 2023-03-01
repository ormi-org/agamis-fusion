package io.agamis.fusion.external.api.rest.routes

import scala.concurrent.duration._
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import akka.http.scaladsl.testkit.RouteTestTimeout
import org.scalatest.wordspec.AnyWordSpec
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import io.agamis.fusion.core.actors.data.entities.UserDataBehavior
import scala.concurrent.Future
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import io.agamis.fusion.core.services.UserService
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.external.api.rest.dto.user.UserDto
import io.agamis.fusion.external.api.rest.dto.user.UserJsonSupport
import java.util.UUID
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import java.time.Instant
import io.agamis.fusion.external.api.rest.dto.user.UserMutation
import scala.util.Success
import io.agamis.fusion.core.db.models.sql.User
import akka.http.scaladsl.model.headers.Location

class UserRoutesSpec
    extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers
    with ScalatestRouteTest
    with MockitoSugar
    with UserJsonSupport {

    val testKit = ActorTestKit()
    implicit val testSystem: ActorSystem[_] = testKit.system
    implicit val timeout = RouteTestTimeout(5.seconds)
    implicit val ta = TildeArrow.injectIntoRoute
    implicit val userService: UserService = mock[UserService]
    val userRoutes: UserRoutes = new UserRoutes

    override def afterAll(): Unit = testKit.shutdownTestKit()

    private val TEST_USERS: List[UserDto] = List(
      UserDto(
        Some(UUID.fromString("4e6482dc-fafe-4268-8bc3-7fcbb9a7f6a3")),
        "chauncey.vonsnuffles",
        List(
          ProfileDto(
            Some(UUID.fromString("10157d43-eaea-415d-be66-d81312394cc6")),
            Some("ItsmeChauncey"),
            "Von snuffles",
            "Chauncey",
            Some(List()),
            Some(List()),
            None,
            Instant.parse("2023-02-14T23:31:54.081426541Z"),
            Some("4e6482dc-fafe-4268-8bc3-7fcbb9a7f6a3"),
            Some(Instant.parse("2023-02-14T23:31:54.081426541Z")),
            Some(Instant.parse("2023-02-14T23:31:54.081426541Z"))
          )
        ),
        Some(Instant.parse("2023-02-14T23:31:54.081426541Z")),
        Some(Instant.parse("2023-02-14T23:31:54.081426541Z"))
      ),
      UserDto(
        Some(UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4")),
        "acat.fromiowa",
        List(
          ProfileDto(
            Some(UUID.fromString("c94a9314-466d-4b69-bfe0-c8cb83cb3445")),
            Some("Acat"),
            "A cat",
            "from Iowa",
            Some(List()),
            Some(List()),
            None,
            Instant.parse("2023-02-20T19:07:23.081426541Z"),
            Some("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
            Some(Instant.parse("2023-02-20T19:07:23.081426541Z")),
            Some(Instant.parse("2023-02-20T19:07:23.081426541Z"))
          )
        ),
        Some(Instant.parse("2023-02-20T19:07:23.081426541Z")),
        Some(Instant.parse("2023-02-20T19:07:23.081426541Z"))
      )
    )

    private val INPUT_TEST_USER: UserMutation = UserMutation(
      "NotARealChauncey",
      "Chauncey's#Secret#pass123"
    )

    private val OUTPUT_TEST_USER: UserDto = UserDto(
      Some(UUID.fromString("8af7ea95-76df-4749-a7cc-cd41e6a37d65")),
      "NotARealChauncey",
      List(),
      Some(Instant.parse("2023-02-21T09:31:54.081426541Z")),
      Some(Instant.parse("2023-02-21T09:31:54.081426541Z"))
    )

    "User routes" should {
        "return a user list for GET request on plural root path with no query params" in {
            val query = UserDataBehavior.Query(
              List(),
              List(),
              None,
              None,
              List(),
              List(),
              List(),
              List()
            )
            val expected = Future.successful(
              UserDataBehavior.MultiUserState(
                "user-query-%d".format(query.hashCode),
                TEST_USERS,
                UserDataBehavior.Ok()
              )
            )
            doReturn(expected).when(userService).queryUsers(query)
            Get("/users") ~> userRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                val result = responseAs[List[UserDto]]
                val expected = TEST_USERS.map((u: UserDto) => {
                    u.copy(profiles = u.profiles.map((p) => {
                        p.copy(
                          emails = None,
                          organization = None,
                          permissions = None
                        )
                    }))
                })
                result should be(expected)
            }
        }

        "return a specific user for GET request on singular root path by id" in {
            val expected = Future.successful(
              UserDataBehavior.SingleUserState(
                "user-%s".format("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
                Some(TEST_USERS(1)),
                UserDataBehavior.Ok()
              )
            )
            doReturn(expected)
                .when(userService)
                .getUserById(
                  UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
                  List()
                )
            Get(
              "/user/9ad0d0a3-e621-4a35-aaeb-38533ef090c4"
            ) ~> userRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                val result = responseAs[UserDto]
                val expected = TEST_USERS(1).copy(
                  profiles = TEST_USERS(1).profiles.map((p) => {
                      p.copy(
                        emails = None,
                        organization = None,
                        permissions = None
                      )
                  })
                )
                result should be(expected)
            }
        }

        "return a specific user for GET request on singular root path with username param" in {
            val expected = Future.successful(
              UserDataBehavior.SingleUserState(
                "user-uname-%s".format("ItsmeChauncey"),
                Some(TEST_USERS(0)),
                UserDataBehavior.Ok()
              )
            )
            doReturn(expected)
                .when(userService)
                .getUserByUsername("ItsmeChauncey")
            Get(
              "/user?username=ItsmeChauncey"
            ) ~> userRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                val result = responseAs[UserDto]
                val expected = TEST_USERS(0).copy(
                  profiles = TEST_USERS(0).profiles.map((p) => {
                      p.copy(
                        emails = None,
                        organization = None,
                        permissions = None
                      )
                  })
                )
                result should be(expected)
            }
        }

        "return ok response with location for POST request on plural root path" in {
            val expected = Future.successful(
              UserDataBehavior.SingleUserState(
                "user-%s".format(OUTPUT_TEST_USER.id.get.toString),
                Some(OUTPUT_TEST_USER),
                UserDataBehavior.Ok()
              )
            )
            doReturn(expected)
                .when(userService)
                .createUser(
                  UserDataBehavior.UserMutation(
                    Some(INPUT_TEST_USER.username),
                    Some(INPUT_TEST_USER.password)
                  )
                )
            Post(
              "/users",
              INPUT_TEST_USER
            ) ~> userRoutes.routes ~> check {
                status shouldEqual StatusCodes.Created
                header("Location") shouldNot be (None)
                header("Location") should be (Some(Location(s"/api/v1/user/${OUTPUT_TEST_USER.id.get.toString}")))
            }
        }

        "return output user mutation for PUT request on singular root path" in {
            val expected = Future.successful(
              UserDataBehavior.SingleUserState(
                "user-%s".format(OUTPUT_TEST_USER.id.get.toString),
                Some(TEST_USERS(1).copy(
                  username = INPUT_TEST_USER.username
                )),
                UserDataBehavior.Ok()
              )
            )
            doReturn(expected)
                .when(userService)
                .updateUser(
                  UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
                  UserDataBehavior.UserMutation(
                    Some(INPUT_TEST_USER.username),
                    Some(INPUT_TEST_USER.password)
                  )
                )
            Put(
              "/user/9ad0d0a3-e621-4a35-aaeb-38533ef090c4",
              INPUT_TEST_USER
            ) ~> userRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                val result = responseAs[UserDto]
                val expected = TEST_USERS(1).copy(
                  username = INPUT_TEST_USER.username,
                  profiles = TEST_USERS(1).profiles.map((p) => {
                      p.copy(
                        emails = None,
                        organization = None,
                        permissions = None
                      )
                  })
                )
                result should be(expected)
            }
        }
    }
}
