// package io.agamis.fusion.core.services

// import akka.cluster.sharding.typed.scaladsl.ClusterSharding
// import java.util.UUID
// import scala.concurrent.Future
// import io.agamis.fusion.core.actor.data.entities.UserDataBehavior
// import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
// import io.agamis.fusion.core.actor.data.DataActor
// import scala.concurrent.duration._
// import akka.util.Timeout
// import akka.actor.typed.ActorRef

// class UserService()(implicit sharding: ClusterSharding) {

//     private val TypeKey =
//         EntityTypeKey[DataActor.Command](DataActor.DataShardName)
//     // asking someone requires a timeout and a scheduler, if the timeout hits without response
//     // the ask is failed with a TimeoutException
//     implicit val timeout = Timeout(10.seconds)

//     def getUserById(
//         id: UUID,
//         include: List[String]
//     ): Future[UserDataBehavior.Response] = {
//         sharding.entityRefFor(TypeKey, "user-%s".format(id.toString())).ask {
//             ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.GetUserById(ref, id, include)
//         }
//     }

//     def getUserByUsername(
//         username: String
//     ): Future[UserDataBehavior.Response] = {
//         sharding.entityRefFor(TypeKey, "user-uname-%s".format(username)).ask {
//             ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.GetUserByUsername(ref, username)
//         }
//     }

//     def queryUsers(
//         query: UserDataBehavior.Query
//     ): Future[UserDataBehavior.Response] = {
//         sharding
//             .entityRefFor(TypeKey, "user-query-%d".format(query.hashCode))
//             .ask { ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.ExecuteQuery(ref, query)
//             }
//     }

//     def createUser(
//         uMut: UserDataBehavior.UserMutation
//     ): Future[UserDataBehavior.Response] = {
//         sharding.entityRefFor(TypeKey, "user-%s".format(UUID.randomUUID)).ask {
//             ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.CreateUser(ref, uMut)
//         }
//     }

//     def updateUser(
//         id: UUID,
//         uMut: UserDataBehavior.UserMutation
//     ): Future[UserDataBehavior.Response] = {
//         sharding.entityRefFor(TypeKey, "user-%s".format(id.toString)).ask {
//             ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.UpdateUser(ref, id, uMut)
//         }
//     }

//     def deleteUser(id: UUID): Future[UserDataBehavior.Response] = {
//         sharding.entityRefFor(TypeKey, "user-%s".format(id.toString)).ask {
//             ref: ActorRef[UserDataBehavior.Response] =>
//                 UserDataBehavior.DeleteUser(ref, id)
//         }
//     }
// }
