package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlStore
import io.ogdt.fusion.core.db.models.sql.Profile
import org.apache.ignite.IgniteCache
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Timestamp
import java.util.UUID
import io.ogdt.fusion.core.db.common.Utils

import scala.jdk.CollectionConverters._

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
                "SELECT PROFILE.id, lastname, firstname, last_login, is_active, PROFILE.created_at, PROFILE.updated_at " +
                "USER.id, username, password, USER.created_at, USER.updated_at " +
                "ORGANIZATION.id, label, type, queryable, ORGANIZATION.created_at, ORGANIZATION.updated_at " +
                s"FROM $schema.PROFILE as PROFILE " +
                s"INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id " +
                s"INNER JOIN $schema.ORGANIZATION as ORGANIZATION ON ORGANIZATION.id = PROFILE.organization_id " +
                "WHERE PROFILE.id = ?")
            .setParams(List(id))
        ).transformWith({
            case Success(profileResults) => {
                var row = profileResults(0)
                Future.successful(
                    (for (
                        profile <- Right(
                            makeProfile
                            .setId(row(0).toString)
                            .setLastname(row(1).toString)
                            .setFirstname(row(2).toString)
                            .setLastLogin(row(3) match {
                                case lastlogin: Timestamp => lastlogin
                                case _ => null
                            })
                            .setCreatedAt(row(5) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(row(6) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { profile =>
                            if (row(7) != null && row(8) != null && row(9) != null && row(10) != null && row(11) != null)
                                Right(profile.setRelatedUser(
                                    new UserStore().makeUser
                                    .setId(row(7).toString)
                                    .setUsername(row(8).toString)
                                    .setPassword(row(9).toString)
                                    .setCreatedAt(row(10) match {
                                        case createdAt: Timestamp => createdAt
                                        case _ => null
                                    })
                                    .setUpdatedAt(row(11) match {
                                        case updatedAt: Timestamp => updatedAt
                                        case _ => null
                                    })
                                ))
                            else Right(profile)
                        } flatMap { profile =>
                            if (row(12) != null && row(13) != null && row(14) != null && row(15) != null && row(16) != null && row(17) != null)
                                Right(profile.setRelatedOrganization(
                                    (for (
                                        organization <- Right(
                                            new OrganizationStore().makeOrganization
                                            .setId(row(12).toString)
                                            .setLabel(row(13).toString)
                                            .setType(row(14).toString)
                                            .setCreatedAt(row(16) match {
                                                case createdAt: Timestamp => createdAt
                                                case _ => null
                                            })
                                            .setUpdatedAt(row(17) match {
                                                case updatedAt: Timestamp => updatedAt
                                                case _ => null
                                            })
                                        ) flatMap { organization =>
                                            row(15) match {
                                                case queryable: Boolean => {
                                                    if(queryable) Right(organization.setQueryable)
                                                    else Right(organization.setUnqueryable)
                                                }
                                                case _ => Right(organization.setUnqueryable)
                                            }
                                        }
                                    ) yield organization)
                                    .getOrElse(null)
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
            "USER.id, username, password, USER.created_at, USER.updated_at " +
            "ORGANIZATION.id, label, type, queryable, ORGANIZATION.created_at, ORGANIZATION.updated_at " +
            s"FROM $schema.PROFILE as PROFILE " +
            s"INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id " +
            s"INNER JOIN $schema.ORGANIZATION as ORGANIZATION ON ORGANIZATION.id = PROFILE.organization_id " +
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
                                case lastlogin: Timestamp => lastlogin
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

    def persistProfile(profile: Profile): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), Some(relatedOrganization)) => {
                Utils.igniteToScalaFuture(igniteCache.putAsync(
                    profile.id, profile
                )).transformWith({
                    case Success(value) => Future.successful()
                    case Failure(cause) => Future.failed(cause)
                })
            }
            case (None, _) => Future.failed(new Error("relatedUser not found and can't be set to null"))
            case (_, None) => Future.failed(new Error("relatedOrganization not found and can't be set to null"))
        }
    }

    /** A result of bulkPersistProfiles method
      * 
      * @constructor create a new BulkPersistProfilesResult with a count of inserted Profiles and a list of errors
      * @param inserts a count of the effectively inserted Profiles
      * @param errors a list of errors catched from a profile insertion
      */
    case class BulkPersistProfilesResult(inserts: Int, errors: List[String])

    def bulkPersistProfiles(profiles: List[Profile]): Future[BulkPersistProfilesResult] = {
        Utils.igniteToScalaFuture(
            igniteCache.putAllAsync(
                (profiles.filter(profile => {
                    (profile.relatedUser, profile.relatedOrganization) match {
                        case (Some(relatedUser), Some(relatedOrganization)) => true
                        case (None, _) => false
                        case (_, None) => false
                    }
                }).map(_.id) zip profiles).toMap[UUID, Profile].asJava
            )
        ).transformWith({
            case Success(value) => {
                Future.sequence(
                    profiles.map(profile => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(profile.id)))
                ).map(lookup => (profiles zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkPersistProfilesResult(
                        lookup.get.filter(_._2 == true).length,
                        lookup.get.filter(_._2 == false).map("Insert profile "+_._1.toString+" failed")
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def deleteProfile(profile: Profile): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), _) => Future.failed(new Error("Profile has still attached User"))
            case (_, Some(relatedOrganization)) => Future.failed(new Error("Profile has still attached Organization"))
            case (None, None) => {
                Utils.igniteToScalaFuture(igniteCache.removeAsync(profile.id))
                .transformWith({
                    case Success(value) => Future.successful()
                    case Failure(cause) => Future.failed(cause)
                })
            }
        }
    }

    /** A result of bulkDeleteProfiles method
      * 
      * @constructor create a new BulkDeleteProfilesResult with a count of deleted Profiles and a list of errors
      * @param inserts a count of the effectively deleted Profiles
      * @param errors a list of errors catched from a profile deletion
      */
    case class BulkDeleteProfilesResult(inserts: Int, errors: List[String])

    def bulkDeleteProfiles(profiles: List[Profile]): Future[BulkDeleteProfilesResult] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(
            (profiles.filter(profile => {
                (profile.relatedUser, profile.relatedOrganization) match {
                        case (Some(relatedUser), _) => false
                        case (_, Some(relatedOrganization)) => false
                        case (None, None) => true
                    }
            }).map(_.id).toSet.asJava))
        ).transformWith({
            case Success(value) => {
                Future.sequence(
                    profiles.map(profile => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(profile.id)))
                ).map(lookup => (profiles zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkDeleteProfilesResult(
                        lookup.get.filter(_._2 == false).length,
                        lookup.get.filter(_._2 == true).map("Failed to delete profile "+_._1.toString)
                    ))
                })
            }
            case Failure(cause) => Future.failed(cause)
        })
    }
}
