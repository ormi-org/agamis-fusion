package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.models.sql.Profile
import java.util.UUID
import org.apache.ignite.IgniteCache
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Timestamp

class ProfileStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, Profile] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_PROFILE"
    override protected var igniteCache: IgniteCache[UUID, Profile] = null

    super .init()

    def makeProfile: Profile = {
        implicit val profileStore: ProfileStore = this
        new Profile
    }

    def getProfileById(id: String): Future[Profile] = {
        executeQuery(
            makeQuery(
                "SELECT PROFILE.id, lastname, firstname, last_login, USER.id, username, password " +
                s"FROM $schema.PROFILE as PROFILE " +
                s"INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id " +
                "WHERE PROFILE.id = ?")
            .setParams(List(id))
        ).transformWith({
            case Success(profileResults) => {
                var row =profileResults(0)
                Future.successful(
                    (for (
                        profile <- Right(
                            makeProfile
                            .setId(row(0).toString)
                            .setLastname(row(1).toString)
                            .setFirstname(row(2).toString)
                            .setLastLogin(row(3) match {
                                case lastlogin: Timestamp => lastlogin.toInstant
                                case _ => null
                            })
                        ) flatMap { profile =>
                            if (row(4) != null && row(5) != null && row(6) != null)
                                Right(profile.setRelatedUser(
                                    new UserStore().makeUser
                                    .setId(row(4).toString)
                                    .setUsername(row(5).toString)
                                    .setPassword(row(6).toString)
                                ))
                            else Right(profile)
                        }
                    ) yield profile)
                    .getOrElse(null))
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getProfiles(ids: List[String]): Future[List[Profile]] = {
        var queryString: String = 
            "SELECT PROFILE.id, lastname, firstname, last_login, USER.id, username, password " +
            s"FROM $schema.PROFILE as PROFILE " +
            s"INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id" +
            "ORDER BY PROFILE.id WHERE PROFILE.id in "
        var queryArgs: List[String] = ids
        queryString += s"(${(for (i <- 1 to queryArgs.length) yield "?").mkString(",")})"
        executeQuery(
            makeQuery(queryString)
            .setParams(queryArgs)
        ).transformWith({
            case Success(profileResults) => {
                var profiles = profileResults.par map(row => {
                    (for (
                        profile <- Right(
                            makeProfile
                            .setId(row(0).toString)
                            .setLastname(row(1).toString)
                            .setFirstname(row(2).toString)
                            .setLastLogin(row(3) match {
                                case lastlogin: Timestamp => lastlogin.toInstant
                                case _ => null
                            })
                        ) flatMap { profile =>
                            if (row(4) != null && row(5) != null && row(6) != null)
                                Right(profile.setRelatedUser(
                                    new UserStore().makeUser
                                    .setId(row(4).toString)
                                    .setUsername(row(5).toString)
                                    .setPassword(row(6).toString)
                                ))
                            else Right(profile)
                        }
                    ) yield profile)
                    .getOrElse(null)
                })
                Future.successful(profiles.toList)
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def persistProfile(profile: Profile): Future[Boolean] = {
        executeQuery(
            makeQuery(s"MERGE INTO $schema.PROFILE(id, lastname, firstname, last_login, user_id) values (?,?,?,?,?)")
            .setParams(List(profile.id, profile.lastname, profile.firstname, profile.lastLogin, profile.relatedUser.id))
        ).transformWith({
            case Success(result) =>
                if (result(0)(0).toString.toInt == 1)
                    Future.successful(true)
                else
                    Future.successful(false)
            case Failure(cause) => Future.failed(cause)
        })
    }

    def bulkPersistProfiles(profiles: List[Profile]): Future[Int] = {
        var queryString: String = s"MERGE INTO $schema.PROFILE(id, lastname, firstname, last_login, user_id) values "
        val queryArgs: List[List[_]] = profiles.map(profile => {
            List(profile.id, profile.lastname, profile.firstname, profile.lastLogin, profile.relatedUser.id)
        })
        queryString += (for (i <- 1 to queryArgs.length) yield "(?,?,?)").mkString(",")
        executeQuery(
            makeQuery(queryString)
            .setParams(queryArgs.flatten)
        ).transformWith({
            // case Success(result) => wrapper.getLogger().info(s"Updated rows : ${result(0)(0).toString()}")
            // result(0)(0) is the count of updated rows
            case Success(result) => Future.successful(result(0)(0).toString.toInt)
            case Failure(cause) => Future.failed(cause)
        })
    }

    def removeProfile(profile: Profile): Future[Boolean] = {
        executeQuery(
            makeQuery(s"DELETE FROM $schema.PROFILE WHERE id = ?")
            .setParams(List(profile.id))
        ).transformWith({
            case Success(result) =>
                if (result(0)(0).toString.toInt == 1)
                    Future.successful(true)
                else
                    Future.successful(false)
            case Failure(cause) => Future.failed(cause)
        })
    }

    def bulkRemoveProfiles(profiles: List[Profile]): Future[Int] = {
        var queryString: String = s"DELETE FROM $schema.PROFILE WHERE id in "
        var conditionArgs: List[String] = profiles.map(profile => {
            profile.id.toString
        })
        queryString += s"(${(for (i <- 1 to conditionArgs.length) yield "?").mkString(",")})"
        executeQuery(makeQuery(queryString)).transformWith({
            // case Success(result) => wrapper.getLogger().info(s"Deleted rows : ${result(0)(0).toString()}")
            case Success(result) => Future.successful(result(0)(0).toString.toInt)
            case Failure(cause) => Future.failed(cause)
        })
    }
}
