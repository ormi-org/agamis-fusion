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

      val fileSystemRepository =
        ctx.spawn(FileSystemRepository(), "FileSystemRepository")
      val fileSystemRoutes = new FileSystemRoutes(fileSystemRepository)

      val fileRepository = ctx.spawn(FileRepository(), "FileRepository")
      val fileRoutes = new FileRoutes(fileRepository)

      val userRepository = ctx.spawn(UserRepository(), "UserRepository")
      val userRoutes = new UserRoutes(userRepository)

      val groupRepository = ctx.spawn(GroupRepository(), "GroupRepository")
      val groupRoutes = new GroupRoutes(groupRepository)

      val organizationRepository =
        ctx.spawn(OrganizationRepository(), "OrganizationRepository")
      val organizationRoutes = new OrganizationRoutes(organizationRepository)

      val organizationTypeRepository =
        ctx.spawn(OrganizationTypeRepository(), "OrganizationTypeRepository")
      val organizationTypeRoutes =
        new OrganizationTypeRoutes(organizationTypeRepository)

      val profileRepository =
        ctx.spawn(ProfileRepository(), "ProfileRepository")
      val profileRoutes = new ProfileRoutes(profileRepository)

      val permissionRepository =
        ctx.spawn(PermissionRepository(), "PermissionRepository")
      val permissionRoutes = new PermissionRoutes(permissionRepository)

      val authenticationRepository =
        ctx.spawn(AuthenticationRepository(), "AuthenticationRepository")
      val authenticationRoutes =
        new AuthenticationRoutes(authenticationRepository)

      val topLevel: Route =
        concat(
          pathPrefix("api")(
            concat(
              pathPrefix("v1")(
                concat(
                  pathPrefix("auth")(
                    authenticationRoutes.routes
                  ),
                  fileSystemRoutes.routes,
                  fileRoutes.routes,
                  groupRoutes.routes,
                  organizationRoutes.routes,
                  organizationTypeRoutes.routes,
                  permissionRoutes.routes,
                  profileRoutes.routes,
                  userRoutes.routes
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
