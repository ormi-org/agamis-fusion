package io.agamis.fusion.core.actors.data.entities

import io.agamis.fusion.core.db.models.sql.User
import java.util.UUID
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto
import akka.actor.typed.ActorRef
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.agamis.fusion.core.db.datastores.sql.UserStore
import scala.concurrent.ExecutionContext
import io.agamis.fusion.core.actors.data.DataActor
import akka.actor.typed.scaladsl.ActorContext
import scala.util.Success
import scala.util.Failure
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotFoundException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.DuplicateUserException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotPersistedException
import java.time.Instant

class UserDataBehavior(
  final val store: UserStore
)

object UserDataBehavior {

  sealed trait Command extends DataActor.Command
  sealed trait Response extends DataActor.State

  // queries
  final case class Query(
    id: List[UUID],
    username: List[String],
    limit: Long,
    offset: Long,
    createdAt: List[(String, Instant)],
    updatedAt: List[(String, Instant)]
  )

  // mutations
  final case class UserMutation(
    username: String,
    password: String
  )

  // commands
  final case class ExecuteQuery(
    replyTo: ActorRef[Response],
    query: Query
  ) extends Command

  final case class GetUserById(
    replyTo: ActorRef[Response],
    id: UUID
  ) extends Command

  final case class GetUserByUsername(
    replyTo: ActorRef[Response],
    username: String
  ) extends Command

  final case class CreateUser(
    replyTo: ActorRef[Response],
    userMutation: UserMutation
  ) extends Command

  final case class UpdateUser(
    replyTo: ActorRef[Response],
    dto: UserMutation
  ) extends Command

  final case class AddProfile(
    replyTo: ActorRef[Response],
    id: UUID,
    profile: ProfileDto
  ) extends Command

  // responses
  sealed trait Status {
    def msg: String
  }
  final case class Ok(msg: String = "Ok") extends Status
  final case class NotFound(msg: String = "User not found") extends Status
  final case class InternalException(msg: String = "An unhandled exception occured") extends Status

  sealed trait State extends Response {
    def status: Status
  }
  final case class SingleUserState(entityId: String, result: Option[User], status: Status) extends State
  final case class MultiUserState(entityId: String, result: List[User] = List(), status: Status) extends State
  final case class WrappedState(state: State, replyTo: ActorRef[Response]) extends Command

  def apply(state: DataActor.State)(implicit ec: ExecutionContext, wrapper: IgniteClientNodeWrapper): PartialFunction[
    (ActorContext[UserDataBehavior.Command], UserDataBehavior.Command),
    Behavior[UserDataBehavior.Command]
  ] = {
    implicit val store = new UserStore
    return {
        case (ctx: ActorContext[Command], wstate: WrappedState) => {
          // Send resulting state to original sender
          wstate.replyTo ! wstate.state
          ctx.log.debug(s"Providing result using cache on entity{${wstate.state.entityId}}")
          // Update internal state for caching result
          Behaviors.receivePartial(apply(wstate.state))
        }
        case (ctx: ActorContext[Command], eqy: ExecuteQuery) => {
          Behaviors.same
        }
        case (ctx: ActorContext[Command], qry: GetUserById) => {
          if (
            state match {
              case _: DataActor.EmptyState => true
              case s: State => {
                s.status match {
                  case _: Ok => false
                  case _ => true
                }
              }
            }
          ) {
            ctx.pipeToSelf(store.getUserById(qry.id.toString())) {
              case Success(u) => 
                val newState = SingleUserState(state.entityId, Some(u), Ok()) 
                WrappedState(newState, qry.replyTo)
              case Failure(exception) => {
                exception match {
                  case UserNotFoundException(msg, cause@_) =>
                    val newState = SingleUserState(state.entityId, None, NotFound(msg))
                    WrappedState(newState, qry.replyTo)
                  case DuplicateUserException(msg, cause@_) =>
                    val newState = SingleUserState(state.entityId, None, InternalException(msg))
                    ctx.log.error(msg, cause)
                    WrappedState(newState, qry.replyTo)
                  case default: Throwable => 
                    val newState = SingleUserState(state.entityId, None, InternalException(default.getMessage())) 
                    WrappedState(newState, qry.replyTo)
                }
              }
            }
            Behaviors.same
          } else {
            qry.replyTo ! state.asInstanceOf[State]
            Behaviors.same
          }
        }
        case (ctx: ActorContext[Command], qry: GetUserByUsername) => {
          store.getUserByUsername(qry.username.toString()).onComplete({
            case Success(u) => 
              qry.replyTo ! SingleUserState(state.entityId, Some(u), Ok())
            case Failure(exception) => {
              exception match {
                case UserNotFoundException(msg, cause@_) =>
                  qry.replyTo ! SingleUserState(state.entityId, None, NotFound(msg))
                case DuplicateUserException(msg, cause) =>
                  qry.replyTo ! SingleUserState(state.entityId, None, InternalException(msg))
                  ctx.log.error(s"$msg; due to: $cause")
                case default: Throwable => 
                  qry.replyTo ! SingleUserState(state.entityId, None, InternalException(default.getMessage))
              }
            }
          })
          Behaviors.same
        }
        case (ctx: ActorContext[Command], crt: CreateUser) => {
          // Create new user with mutation
          val newUser = store.makeUser
          .setUsername(crt.userMutation.username)
          .setPassword(crt.userMutation.password)
          // Persist it
          newUser.persist
          .onComplete({
            case Success(statement) => 
              store.commitTransaction(statement._1)
              crt.replyTo ! SingleUserState(state.entityId, Some(statement._2), Ok())
            case Failure(exception) => {
              exception match {
                case UserNotPersistedException(msg, cause) =>
                  crt.replyTo ! SingleUserState(state.entityId, None, InternalException(msg))
                  ctx.log.error(s"$msg; due to: $cause")
                case default: Throwable =>
                  crt.replyTo ! SingleUserState(state.entityId, None, InternalException(default.getMessage))
              }
            }
          })
          Behaviors.same
        }
        case (ctx: ActorContext[Command], addpf: AddProfile) => {
          
          Behaviors.same
        }
        case (ctx: ActorContext[Command], upd: UpdateUser) => {
          
          Behaviors.same
        }
    }
  }
}