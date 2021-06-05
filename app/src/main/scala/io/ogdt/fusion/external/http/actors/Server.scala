package io.ogdt.fusion.external.http.actors

import akka.actor.typed.PostStop
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http

import scala.util.{ Success, Failure }
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.Future

import io.ogdt.fusion.external.http.routes.{
    FileRoutes, UserRoutes, GroupRoutes, ProfileRoutes, FileSystemRoutes, PermissionRoutes, OrganizationRoutes, AuthenticationRoutes
}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import java.util.UUID
import akka.http.scaladsl.model.StatusCodes

object Server {

    sealed trait Message
    private final case class StartFailed(cause: Throwable) extends Message
    private final case class Started(binding: ServerBinding) extends Message
    case object Stop extends Message

    def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>

    implicit val system = ctx.system

    val buildFileSystemRepository = ctx.spawn(FileSystemRepository(), "FileSystemRepository")
    val fileSystemRoutes = new FileSystemRoutes(buildFileSystemRepository)

    val buildFileRepository = ctx.spawn(FileRepository(), "FileRepository")
    val fileRoutes = new FileRoutes(buildFileRepository)
	
    val buildUserRepository = ctx.spawn(UserRepository(), "UserRepository")
    val userRoutes = new UserRoutes(buildUserRepository)

    val buildGroupRepository = ctx.spawn(GroupRepository(), "GroupRepository")
    val groupRoutes = new GroupRoutes(buildGroupRepository)

    val buildOrganizationRepository = ctx.spawn(OrganizationRepository(), "OrganizationRepository")
    val organizationRoutes = new OrganizationRoutes(buildOrganizationRepository)

    val buildProfileRepository = ctx.spawn(ProfileRepository(), "ProfileRepository")
    val profileRoutes = new ProfileRoutes(buildProfileRepository)

    val buildPermissionRepository = ctx.spawn(PermissionRepository(), "PermissionRepository")
    val permissionRoutes = new PermissionRoutes(buildPermissionRepository)

    val buildAuthenticationRepository = ctx.spawn(AuthenticationRepository(), "AuthenticationRepository")
    val authenticationRoutes = new AuthenticationRoutes(buildAuthenticationRepository)

    val topLevel: Route = 
        concat(
            pathPrefix("api")(
                concat(
                    pathPrefix("v1")(
                        concat(
                            pathPrefix("fs")(
                                concat(
                                    fileSystemRoutes.routes,
                                    userRoutes.routes,
                                    concat(
                                        fileRoutes.routes,
                                    )
                                )
                            ),
                            pathPrefix("settings")(
                                concat(
                                    permissionRoutes.routes,
                                    profileRoutes.routes,
                                    groupRoutes.routes,
                                    organizationRoutes.routes
                                )
                            ), 
                            pathPrefix("authentication")(
                                concat(
                                    authenticationRoutes.routes
                                )
                            )
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
        Behaviors.receiveMessagePartial[Message] {
        case Stop =>
            ctx.log.info(
            "Stopping server http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
            Behaviors.stopped
        }.receiveSignal {
        case (_, PostStop) =>
            binding.unbind()
            Behaviors.same
        }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
        Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
            throw new RuntimeException("Server failed to start", cause)
        case Started(binding) =>
            ctx.log.info(
            "Server online at http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
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