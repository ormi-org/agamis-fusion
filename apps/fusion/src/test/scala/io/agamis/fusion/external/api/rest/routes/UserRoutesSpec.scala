package io.agamis.fusion.api.rest.routes

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorSystem
import io.agamis.fusion.api.rest.model.dto.user.UserDto
import io.agamis.fusion.api.rest.model.dto.user.UserJsonSupport
import io.agamis.fusion.api.rest.model.dto.user.UserMutation
import org.scalatest.BeforeAndAfterAll
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.duration._

import org.apache.pekko.http.scaladsl.testkit.RouteTestTimeout
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest

class UserRoutesSpec
    extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers
    with ScalatestRouteTest
    with UserJsonSupport
    with PrivateMethodTester {

    val testKit                             = ActorTestKit()
    implicit val testSystem: ActorSystem[_] = testKit.system
    implicit val timeout                    = RouteTestTimeout(5.seconds)
    implicit val ta                         = TildeArrow.injectIntoRoute
    // implicit val userService: UserService   = mock[UserService]
    // val userRoutes: UserRoutes              = new UserRoutes

    override def afterAll(): Unit = testKit.shutdownTestKit()

    private val BASE_DTO: UserDto = UserDto(
      UUID.fromString("4e6482dc-fafe-4268-8bc3-7fcbb9a7f6a3"),
      "chauncey.vonsnuffles",
      LocalDateTime.parse("2023-02-14T23:31:54.081426541Z"),
      LocalDateTime.parse("2023-02-14T23:31:54.081426541Z")
    )

    private val TEST_USERS: List[UserDto] = List(
      BASE_DTO,
      UserDto(
        UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
        "acat.fromiowa",
        LocalDateTime.parse("2023-02-20T19:07:23.081426541Z"),
        LocalDateTime.parse("2023-02-20T19:07:23.081426541Z")
      )
    )

    private val INPUT_TEST_USER: UserMutation = UserMutation(
      "NotARealChauncey",
      "Chauncey's#Secret#pass123"
    )

    private val OUTPUT_TEST_USER: UserDto = UserDto(
      UUID.fromString("8af7ea95-76df-4749-a7cc-cd41e6a37d65"),
      "NotARealChauncey",
      LocalDateTime.parse("2023-02-21T09:31:54.081426541Z"),
      LocalDateTime.parse("2023-02-21T09:31:54.081426541Z")
    )

    // "excludeFields" should {
    //     "return a user dto with no profiles field WHEN profiles field is None" in {
    //         val excludeFieldsPrivateMethod =
    //             PrivateMethod[UserDto](Symbol("excludeFields"))
    //         val processedDto =
    //             userRoutes invokePrivate excludeFieldsPrivateMethod(
    //               BASE_DTO.copy(profiles = None)
    //             )
    //         processedDto.profiles shouldBe (None)
    //     }

    //     "return a user dto with excluded field WHEN profiles field is Some dto" in {
    //         val excludeFieldsPrivateMethod =
    //             PrivateMethod[UserDto](Symbol("excludeFields"))
    //         val processedDto =
    //             userRoutes invokePrivate excludeFieldsPrivateMethod(BASE_DTO)
    //         val expected =
    //             BASE_DTO.profiles
    //                 .get(0)
    //                 .copy(
    //                   emails = None,
    //                   organization = None,
    //                   permissions = None
    //                 )
    //         processedDto.profiles.get(0) shouldBe (expected)
    //     }
    // }

    // "User routes" should {
    //     "return a user list for GET request ON plural root path with include query params" in {
    //         val query = UserDataBehavior.Query(
    //           List(),
    //           List(),
    //           None,
    //           None,
    //           List(),
    //           List(),
    //           List(),
    //           List("profiles")
    //         )
    //         val expected = Future.successful(
    //           UserDataBehavior.MultiUserState(
    //             "user-query-%d".format(query.hashCode),
    //             TEST_USERS,
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected).when(userService).queryUsers(query)
    //         Get("/users?include=profiles") ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.OK
    //             val result = responseAs[List[UserDto]]
    //             val expected = TEST_USERS.map((u: UserDto) => {
    //                 u.copy(profiles = u.profiles match {
    //                     case Some(profiles) =>
    //                         Some(profiles.map((p) => {
    //                             p.copy(
    //                               emails = None,
    //                               organization = None,
    //                               permissions = None
    //                             )
    //                         }))
    //                     case None => None
    //                 })
    //             })
    //             result should be(expected)
    //         }
    //     }

    //     "return a user list (no relation) for GET request on plural root path with no query params" in {
    //         val query = UserDataBehavior.Query(
    //           List(),
    //           List(),
    //           None,
    //           None,
    //           List(),
    //           List(),
    //           List(),
    //           List()
    //         )
    //         val expected = Future.successful(
    //           UserDataBehavior.MultiUserState(
    //             "user-query-%d".format(query.hashCode),
    //             TEST_USERS.map((u) => {
    //                 u.copy(profiles = None)
    //             }),
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected).when(userService).queryUsers(query)
    //         Get("/users") ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.OK
    //             val result = responseAs[List[UserDto]]
    //             val expected = TEST_USERS.map((u) => {
    //                 u.copy(profiles = None)
    //             })
    //             result should be(expected)
    //         }
    //     }

    //     "return a specific user for GET request on singular root path by id" in {
    //         val expected = Future.successful(
    //           UserDataBehavior.SingleUserState(
    //             "user-%s".format("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
    //             Some(TEST_USERS(1)),
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected)
    //             .when(userService)
    //             .getUserById(
    //               UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
    //               List()
    //             )
    //         Get(
    //           "/user/9ad0d0a3-e621-4a35-aaeb-38533ef090c4"
    //         ) ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.OK
    //             val result = responseAs[UserDto]
    //             val expected = TEST_USERS(1).copy(
    //               profiles = TEST_USERS(1).profiles match {
    //                   case Some(profiles) =>
    //                       Some(profiles.map((p) => {
    //                           p.copy(
    //                             emails = None,
    //                             organization = None,
    //                             permissions = None
    //                           )
    //                       }))
    //                   case None => None
    //               }
    //             )
    //             result should be(expected)
    //         }
    //     }

    //     "return a specific user for GET request on singular root path with username param" in {
    //         val expected = Future.successful(
    //           UserDataBehavior.SingleUserState(
    //             "user-uname-%s".format("ItsmeChauncey"),
    //             Some(TEST_USERS(0)),
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected)
    //             .when(userService)
    //             .getUserByUsername("ItsmeChauncey")
    //         Get(
    //           "/user?username=ItsmeChauncey"
    //         ) ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.OK
    //             val result = responseAs[UserDto]
    //             val expected = TEST_USERS(0).copy(
    //               profiles = TEST_USERS(0).profiles match {
    //                   case Some(profiles) =>
    //                       Some(profiles.map((p) => {
    //                           p.copy(
    //                             emails = None,
    //                             organization = None,
    //                             permissions = None
    //                           )
    //                       }))
    //                   case None => None
    //               }
    //             )
    //             result should be(expected)
    //         }
    //     }

    //     "return ok response with location for POST request on plural root path" in {
    //         val expected = Future.successful(
    //           UserDataBehavior.SingleUserState(
    //             "user-%s".format(OUTPUT_TEST_USER.id.get.toString),
    //             Some(OUTPUT_TEST_USER),
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected)
    //             .when(userService)
    //             .createUser(
    //               UserDataBehavior.UserMutation(
    //                 Some(INPUT_TEST_USER.username),
    //                 Some(INPUT_TEST_USER.password)
    //               )
    //             )
    //         Post(
    //           "/users",
    //           INPUT_TEST_USER
    //         ) ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.Created
    //             header("Location") shouldNot be(None)
    //             header("Location") should be(
    //               Some(
    //                 Location(
    //                   s"/api/v1/user/${OUTPUT_TEST_USER.id.get.toString}"
    //                 )
    //               )
    //             )
    //         }
    //     }

    //     "return output user mutation for PUT request on singular root path" in {
    //         val expected = Future.successful(
    //           UserDataBehavior.SingleUserState(
    //             "user-%s".format(OUTPUT_TEST_USER.id.get.toString),
    //             Some(
    //               TEST_USERS(1).copy(
    //                 username = INPUT_TEST_USER.username
    //               )
    //             ),
    //             UserDataBehavior.Ok()
    //           )
    //         )
    //         doReturn(expected)
    //             .when(userService)
    //             .updateUser(
    //               UUID.fromString("9ad0d0a3-e621-4a35-aaeb-38533ef090c4"),
    //               UserDataBehavior.UserMutation(
    //                 Some(INPUT_TEST_USER.username),
    //                 Some(INPUT_TEST_USER.password)
    //               )
    //             )
    //         Put(
    //           "/user/9ad0d0a3-e621-4a35-aaeb-38533ef090c4",
    //           INPUT_TEST_USER
    //         ) ~> userRoutes.routes ~> check {
    //             status shouldEqual StatusCodes.OK
    //             val result = responseAs[UserDto]
    //             val expected = TEST_USERS(1).copy(
    //               username = INPUT_TEST_USER.username,
    //               profiles = TEST_USERS(1).profiles match {
    //                   case Some(profiles) =>
    //                       Some(profiles.map((p) => {
    //                           p.copy(
    //                             emails = None,
    //                             organization = None,
    //                             permissions = None
    //                           )
    //                       }))
    //                   case None => None
    //               }
    //             )
    //             result should be(expected)
    //         }
    //     }
    // }
}
