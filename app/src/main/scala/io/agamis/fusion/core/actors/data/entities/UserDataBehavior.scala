package io.agamis.fusion.core.actors.data.entities

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import io.agamis.fusion.core.actors.data.DataActor
import io.agamis.fusion.core.actors.data.entities.common.Identifiable
import io.agamis.fusion.core.actors.data.entities.common.Pageable
import io.agamis.fusion.core.actors.data.entities.common.Timetracked
import io.agamis.fusion.core.db.datastores.sql.UserStore
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.DuplicateUserException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotFoundException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserNotPersistedException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users.UserQueryExecutionException
import io.agamis.fusion.core.db.models.sql.User
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
// import io.agamis.fusion.external.api.rest.dto.profile.ProfileDto

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

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
    updatedAt: List[(String, Instant)],
    orderBy: List[(String, Int)]
  ) extends Identifiable with Timetracked with Pageable

  // mutations
  final case class UserMutation(
    username: Option[String],
    password: Option[String]
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
    id: UUID,
    userMutation: UserMutation
  ) extends Command

  final case class DeleteUser(
    replyTo: ActorRef[Response],
    id: UUID
  ) extends Command

  // final case class AddProfile(
  //   replyTo: ActorRef[Response],
  //   id: UUID,
  //   profile: ProfileDto
  // ) extends Command

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
          // Update internal state for caching result
          ctx.log.debug(s"Caching result of entity{${state.entityId}}")
          Behaviors.receivePartial(apply(wstate.state))
        }
        case (ctx: ActorContext[Command], eqy: ExecuteQuery) => {
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
            val query = eqy.query
            // Caching
            val filters = UserStore.GetUsersFilters().copy(
              filters = List(UserStore.GetUsersFilter().copy(
                id = if(query.id.nonEmpty) query.id.map { _.toString } else List(),
                username = if(query.username.nonEmpty) query.username else List(),
                createdAt = if(query.createdAt.nonEmpty) query.createdAt.map { c => (c._1, Timestamp.from(c._2)) } else List(),
                updatedAt = if(query.updatedAt.nonEmpty) query.updatedAt.map { c => (c._1, Timestamp.from(c._2)) } else List(),
              )),
              orderBy = if(query.orderBy.nonEmpty) query.orderBy else List(("id", 1))
            )
            ctx.pipeToSelf(store.getUsers(filters, query.limit, query.offset)) {
              case Success(userList) =>
                val newState = MultiUserState(state.entityId, userList, Ok())
                WrappedState(newState, eqy.replyTo)
              case Failure(exception) => 
                exception match {
                  case UserQueryExecutionException(msg, cause@_) =>
                    val newState = MultiUserState(state.entityId, List(), InternalException(msg))
                    ctx.log.error(msg, cause)
                    WrappedState(newState, eqy.replyTo)
                  case default: Throwable =>
                    val newState = MultiUserState(state.entityId, List(), InternalException(default.getMessage()))
                    WrappedState(newState, eqy.replyTo)
                }
            }
          } else {
            ctx.log.debug(s"Providing result using cache on entity{${state.entityId}}")
            eqy.replyTo ! state.asInstanceOf[State]
          }
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
            // Caching
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
          } else {
            ctx.log.debug(s"Providing result using cache on entity{${state.entityId}}")
            qry.replyTo ! state.asInstanceOf[State]
          }
          Behaviors.same
        }
        case (ctx: ActorContext[Command], qry: GetUserByUsername) => {
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
            // Caching
            ctx.pipeToSelf(store.getUserByUsername(qry.username.toString())) {
              case Success(u) => 
                val newState = SingleUserState(state.entityId, Some(u), Ok())
                WrappedState(newState, qry.replyTo)
              case Failure(exception) => {
                exception match {
                  case UserNotFoundException(msg, cause@_) =>
                    val newState = SingleUserState(state.entityId, None, NotFound(msg))
                    WrappedState(newState, qry.replyTo)
                  case DuplicateUserException(msg, cause) =>
                    val newState = SingleUserState(state.entityId, None, InternalException(msg))
                    ctx.log.error(s"$msg; due to: $cause")
                    WrappedState(newState, qry.replyTo)
                  case default: Throwable => 
                    val newState = SingleUserState(state.entityId, None, InternalException(default.getMessage))
                    WrappedState(newState, qry.replyTo)
                }
              }
            }
          } else {
            ctx.log.debug(s"Providing result using cache on entity{${state.entityId}}")
            qry.replyTo ! state.asInstanceOf[State]
          }
          Behaviors.same
        }
        case (ctx: ActorContext[Command], crt: CreateUser) => {
          // Create new user with mutation
          if (crt.userMutation.username.isEmpty || crt.userMutation.password.isEmpty) {
            crt.replyTo ! SingleUserState(state.entityId, None, InternalException("Missing field among: username, password"))
          } else {
            val newUser = store.makeUser
            // Safe retrieval
            .setUsername(crt.userMutation.username.get)
            .setPassword(crt.userMutation.password.get)
            // Persist it
            newUser.persist
            .onComplete({
              case Success((tx, user)) => 
                store.commitTransaction(tx)
                crt.replyTo ! SingleUserState(state.entityId, Some(user), Ok())
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
          }
          Behaviors.same
        }
        case (ctx: ActorContext[Command], upd: UpdateUser) => {
          for {
            newUserState <- store.getUserById(upd.id.toString())
                    .transformWith {
                      case Success(user) => 
                        upd.userMutation.username match {
                          case Some(value) => user.setUsername(value)
                          case None =>
                        }
                        upd.userMutation.password match {
                          case Some(value) => user.setPassword(value)
                          case None =>
                        }
                        Future.successful(user)
                      case Failure(exception) => {
                        exception match {
                          case UserNotFoundException(msg, cause) =>
                            upd.replyTo ! SingleUserState(state.entityId, None, NotFound(msg))
                            Future.failed(cause)
                          case DuplicateUserException(msg, cause) =>
                            upd.replyTo ! SingleUserState(state.entityId, None, InternalException(msg))
                            ctx.log.error(s"$msg; due to: $cause")
                            Future.failed(cause)
                          case default: Throwable => 
                            upd.replyTo ! SingleUserState(state.entityId, None, InternalException(default.getMessage))
                            Future.failed(default)
                        }
                      }
                    }
            mutation <- newUserState.persist
                        .andThen {
                          case Success((tx, user)) =>
                            store.commitTransaction(tx)
                            upd.replyTo ! SingleUserState(state.entityId, Some(user), Ok())
                          case Failure(exception) =>
                            exception match {
                              case UserNotPersistedException(msg, cause) =>
                                upd.replyTo ! SingleUserState(state.entityId, None, InternalException(msg))
                                ctx.log.error(s"$msg; due to: $cause")
                              case default: Throwable =>
                                upd.replyTo ! SingleUserState(state.entityId, None, InternalException(default.getMessage))
                            }
                        }
          } yield mutation
          Behaviors.same
        }
    }
  }
}