package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.Organization
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import java.sql.Timestamp
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.ogdt.fusion.core.db.common.Utils

class OrganizationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATION"
    override protected var igniteCache: IgniteCache[UUID, Organization] = null

    super .init()

    // Create and get new Organization Object
    def makeOrganization: Organization = {
        implicit val organizationStore: OrganizationStore = this
        new Organization
    }

    // def getOrganizationById(id: String): Future[Organization] = {
    //     executeQuery(
    //         makeQuery(
    //             "SELECT USER.id, username, password, PROFILE.id, lastname, firstname, last_login " +
    //             s"FROM $schema.USER as USER " +
    //             s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
    //             "WHERE USER.id = ?")
    //         .setParams(List(id))
    //     ).transformWith({
    //         case Success(userResults) => {
    //             var row = userResults(0)
    //             Future.successful(
    //                 (for (
    //                     user <- Right(
    //                         makeOrganization
    //                         .setId(row(0).toString)
    //                         .setOrganizationname(row(1).toString)
    //                         .setPassword(row(2).toString)
    //                     ) flatMap { user => 
    //                         if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
    //                             Right(user.addRelatedProfile(
    //                                 new ProfileStore().makeProfile
    //                                 .setId(row(3).toString)
    //                                 .setLastname(row(4).toString)
    //                                 .setFirstname(row(5).toString)
    //                                 .setLastLogin(row(6) match {
    //                                     case lastlogin: Timestamp => lastlogin
    //                                     case _ => null
    //                                 })
    //                             ))
    //                         else Right(user)
    //                     }
    //                 ) yield user)
    //                 .getOrElse(null))
    //         }
    //         case Failure(cause) => Future.failed(cause)
    //     })
    // }

    // Get existing users from database
    // def getOrganizations(ids: List[String]): Future[List[Organization]] = {
    //     var queryString: String = 
    //         "SELECT USER.id, username, password, PROFILE.id, lastname, firstname, last_login " +
    //         s"FROM $schema.USER as USER " +
    //         s"INNER JOIN $schema.PROFILE as PROFILE ON PROFILE.user_id = USER.id " +
    //         "ORDER BY USER.id WHERE USER.id in "
    //     var queryArgs: List[String] = ids
    //     queryString += s"(${(for (i <- 1 to queryArgs.length) yield "?").mkString(",")})"
    //     executeQuery(
    //         makeQuery(queryString)
    //         .setParams(queryArgs)
    //     ).transformWith({
    //         case Success(userResults) => {
    //             var users = userResults.par map(row => {
    //                 (for (
    //                     user <- Right(
    //                         makeOrganization
    //                         .setId(row(0).toString)
    //                         .setOrganizationname(row(1).toString)
    //                         .setPassword(row(2).toString)
    //                     ) flatMap { user => 
    //                         if (row(3) != null && row(4) != null && row(5) != null && row(6) != null)
    //                             Right(user.addRelatedProfile(
    //                                 new ProfileStore().makeProfile
    //                                 .setId(row(3).toString)
    //                                 .setLastname(row(4).toString)
    //                                 .setFirstname(row(5).toString)
    //                                 .setLastLogin(row(6) match {
    //                                     case lastlogin: Timestamp => lastlogin
    //                                     case _ => null
    //                                 })
    //                             ))
    //                         else Right(user)
    //                     }
    //                 ) yield user)
    //                 .getOrElse(null)
    //             })
    //             Future.successful(users.toList)
    //         }
    //         case Failure(cause) => throw cause
    //     })
    // }

    // Save user object's modification to database
    def persistOrganization(organization: Organization): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            organization.id, organization
        )).transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
        })
    }

    /** A result of bulkPersistOrganizations method
      * 
      * @constructor create a new BulkPersistOrganizationsResult with a count of inserted Organizations and a list of errors
      * @param inserts a count of the effectively inserted Organizations
      * @param errors a list of errors catched from a file deletion
      */
    case class BulkPersistOrganizationsResult(inserts: Int, errors: List[String])

    // Save several object's modifications
    def bulkPersistOrganizations(organizations: List[Organization]): Future[BulkPersistOrganizationsResult] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (organizations.map(_.id) zip organizations).toMap[UUID, Organization].asJava
        )).transformWith({
            case Success(value) => {
                Future.sequence(
                    organizations.map(organization => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(organization.id)))
                ).map(lookup => (organizations zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkPersistOrganizationsResult(
                        lookup.get.filter(_._2 == true).length,
                        lookup.get.filter(_._2 == false).map("Insert organization "+_._1.toString+" failed")
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Delete user from database
    def deleteOrganization(organization: Organization): Future[Unit] = {
        organization.relatedProfiles.map(p => p)
        Utils.igniteToScalaFuture(igniteCache.removeAsync(organization.id))
        .transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
        })
    }

    /** A result of bulkDeleteOrganizations method
      * 
      * @constructor create a new BulkDeleteOrganizationsResult with a count of deleted Organizations and a list of errors
      * @param inserts a count of the effectively deleted Organizations
      * @param errors a list of errors catched from an organization deletion
      */
    case class BulkDeleteOrganizationsResult(inserts: Int, errors: List[String])

    // Delete several users from database
    def bulkDeleteOrganizations(organizations: List[Organization]): Future[BulkDeleteOrganizationsResult] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(organizations.map(_.id).toSet.asJava))
        .transformWith({
            case Success(value) => {
                Future.sequence(
                    organizations.map(profile => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(profile.id)))
                ).map(lookup => (organizations zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkDeleteOrganizationsResult(
                        lookup.get.filter(_._2 == false).length,
                        lookup.get.filter(_._2 == true).map("Failed to delete organization "+_._1.toString)
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }
}