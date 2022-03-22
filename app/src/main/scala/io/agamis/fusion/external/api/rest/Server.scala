package io.agamis.fusion.external.api.rest.actors

import akka.actor.typed.PostStop
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http

import scala.util.{Success, Failure}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Future

import io.agamis.fusion.external.api.rest.routes.{
  FileRoutes,
  UserRoutes,
  GroupRoutes,
  ProfileRoutes,
  FileSystemRoutes,
  PermissionRoutes,
  OrganizationRoutes,
  AuthenticationRoutes
}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import java.util.UUID
import akka.http.scaladsl.model.StatusCodes
import io.agamis.fusion.external.api.rest.routes.OrganizationTypeRoutes

object Server {

  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup {
    ctx =>
      implicit val system = ctx.system

      val topLevel: Route =
        concat(
          pathPrefix("api")(
            concat(
              pathPrefix("v1")(
                concat(
                  pathPrefix("auth")(
                    new AuthenticationRoutes().routes
                  ),
                  new FileSystemRoutes().routes,
                  new FileRoutes().routes,
                  new GroupRoutes().routes,
                  new OrganizationRoutes().routes,
                  new OrganizationTypeRoutes().routes,
                  new PermissionRoutes().routes,
                  new ProfileRoutes().routes,
                  new UserRoutes().routes
                )
              )
            )
          )
        )

      val serverBinding: Future[Http.ServerBinding] =
        Http().newServerAt(host, port).bind(topLevel)
      ctx.pipeToSelf(serverBinding) {
        case Success(binding) => Started(binding)
        case Failure(ex)      => StartFailed(ex)
      }
      def running(binding: ServerBinding): Behavior[Message] =
        Behaviors
          .receiveMessagePartial[Message] { case Stop =>
            ctx.log.info(
              "Stopping REST server {}:{}",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            Behaviors.stopped
          }
          .receiveSignal { case (_, PostStop) =>
            binding.unbind()
            Behaviors.same
          }

      def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
        Behaviors.receiveMessage[Message] {
          case StartFailed(cause) =>
            throw new RuntimeException("Server failed to start", cause)
          case Started(binding) =>
            ctx.log.info(
              "REST Server online at {}:{}",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            if (wasStopped) ctx.self ! Stop
            running(binding)
          case Stop =>
            // we got a stop message but haven't completed starting yet,
            // we cannot stop until starting has completed
            starting(wasStopped = true)
        }

      starting(wasStopped = false)
  }
}
