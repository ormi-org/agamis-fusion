package io.agamis.fusion.api.rest.controller

import io.agamis.fusion.api.rest.model.dto.common.ApiStatus
import io.agamis.fusion.api.rest.model.dto.common.ApiStatusJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationDto
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationJsonSupport
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutation
import io.agamis.fusion.api.rest.model.dto.organization.OrganizationMutationJsonSupport
import io.agamis.fusion.api.rest.routes.OrganizationRoutes
import io.agamis.fusion.core.model
import io.agamis.fusion.core.shard.OrganizationShard
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.testkit.scaladsl.TestEntityRef
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.model.headers.Location
import org.apache.pekko.http.scaladsl.testkit.RouteTestTimeout
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime
import java.util.UUID

import concurrent.duration._

class OrganizationControllerSpec
    extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterAll
    with MockFactory
    with OrganizationJsonSupport
    with OrganizationMutationJsonSupport
    with ApiStatusJsonSupport {

    import io.agamis.fusion.core.actor.entity.Organization._

    val testKit = ActorTestKit()

    implicit val sys: ActorSystem[_] = testKit.system

    val shard: OrganizationShard = mock[OrganizationShard]

    // Create an instance of the OrganizationController
    val controller =
        new OrganizationController(shard)(sys, sys.executionContext)

    // Define the routes for testing
    val routes = new OrganizationRoutes(controller).routes

    // override default route testing timeout
    implicit val rtTimeout = RouteTestTimeout(2.seconds)

    // Cleanup the actor system after all tests have run
    override def afterAll(): Unit = {
        super.afterAll()
        testKit.shutdownTestKit()
    }

    "OrganizationController" should {
        "return organization by id" in {
            val id = UUID.randomUUID
            val organization = model.Organization(
              id,
              "Test Organization",
              true,
              model.OrganizationFK(UUID.randomUUID),
              LocalDateTime.now,
              LocalDateTime.now
            )
            val expectedResult = OrganizationDto.from(organization)

            val probe = testKit.createTestProbe[Command]()
            val ref   = TestEntityRef.apply(TypeKey, id.toString, probe.ref)
            val getResponse = Queryable(organization)

            // Mock the OrganizationShard.ref() response
            (shard
                .ref(_: String)(
                  _: ActorSystem[_]
                ))
                .expects(id.toString, sys)
                .returning(ref)

            // Send GET request to /api/organization/{id}
            val test = Get(s"/organizations/$id") ~> routes
            // Mock the OrganizationShard.Get response
            probe.expectMessageType[Get].replyTo ! getResponse
            // Assert test results
            test ~> check {
                status shouldBe StatusCodes.OK
                responseAs[OrganizationDto] shouldBe expectedResult
            }
        }

        "return bad request for invalid id" in {
            val id = "invalid-id"

            // Send GET request to /api/organization/{id}
            Get(s"/organizations/$id") ~> routes ~> check {
                status shouldBe StatusCodes.BadRequest
                responseAs[
                  ApiStatus
                ].message shouldBe Some("{id} argument is not a valid UUID")
            }
        }

        "create organization" in {
            // mock generated id
            val id = UUID.randomUUID
            val mutation =
                OrganizationMutation("Test Organization", true, UUID.randomUUID)
            val probe = testKit.createTestProbe[Command]()
            val ref   = TestEntityRef.apply(TypeKey, id.toString, probe.ref)
            val createResponse = UpdateSuccess(
              model.Organization(
                id,
                "Test Organization",
                true,
                model.OrganizationFK(UUID.randomUUID),
                LocalDateTime.now,
                LocalDateTime.now
              )
            )

            // Mock the OrganizationShard.ref() response
            (shard
                .ref(_: String)(
                  _: ActorSystem[_]
                ))
                .expects(*, sys)
                .returning(ref)

            // Send POST request to /api/organization
            val test = Post("/organizations", mutation) ~> routes
            // Mock the OrganizationShard.Update response
            probe
                .expectMessageType[Update]
                .replyTo ! createResponse
            // Assert test results
            test ~> check {
                status shouldBe StatusCodes.Created
                header[
                  Location
                ].get.value shouldBe s"http://example.com/api/organization/$id"
            }
        }

        "update organization" in {
            val id        = UUID.randomUUID
            val newTypeId = UUID.randomUUID
            val mutation = OrganizationMutation(
              "Updated Organization",
              true,
              newTypeId
            )
            val creationDate = LocalDateTime.now

            val probe = testKit.createTestProbe[Command]()
            val ref   = TestEntityRef.apply(TypeKey, id.toString, probe.ref)

            val getResponse = Queryable(
              model.Organization(
                id,
                "Test Organization",
                true,
                model.OrganizationFK(UUID.randomUUID),
                creationDate,
                creationDate
              )
            )
            val updateResponse = UpdateSuccess(
              model.Organization(
                id,
                "Updated Organization",
                true,
                model.OrganizationFK(newTypeId),
                creationDate,
                LocalDateTime.now
              )
            )

            // Mock the OrganizationShard.ref() response
            (shard
                .ref(_: String)(
                  _: ActorSystem[_]
                ))
                .expects(id.toString, sys)
                .returning(ref)

            // Send PUT request to /api/organization/{id}
            val test = Put(s"/organizations/$id", mutation) ~> routes
            // Mock the OrganizationShard.Get response
            probe.expectMessageType[Get].replyTo ! getResponse

            // Mock the OrganizationShard.Update response
            probe
                .expectMessageType[Update]
                .replyTo ! updateResponse
            // Asserts test results
            test ~> check {
                status shouldBe StatusCodes.NoContent
            }
        }

        "delete organization" in {
            val id    = UUID.randomUUID
            val probe = testKit.createTestProbe[Command]()
            val ref   = TestEntityRef.apply(TypeKey, id.toString, probe.ref)
            val getResponse = Queryable(
              model.Organization(
                id,
                "Test Organization",
                true,
                model.OrganizationFK(UUID.randomUUID),
                LocalDateTime.now,
                LocalDateTime.now
              )
            )
            val deleteResponse = DeleteSuccess()

            // Mock the OrganizationShard.ref() response
            (shard
                .ref(_: String)(
                  _: ActorSystem[_]
                ))
                .expects(id.toString, sys)
                .returning(ref)

            // Send DELETE request to /api/organization/{id}
            val test = Delete(s"/organizations/$id") ~> routes
            // Mock the OrganizationShard.Get response
            probe.expectMessageType[Get].replyTo ! getResponse
            // Mock the OrganizationShard.Delete response
            probe
                .expectMessageType[Delete]
                .replyTo ! deleteResponse
            // Asserts test results
            test ~> check {
                status shouldBe StatusCodes.NoContent
            }
        }
    }
}
