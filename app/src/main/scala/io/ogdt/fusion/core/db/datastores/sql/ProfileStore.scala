package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationStore
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import io.ogdt.fusion.core.db.models.sql.Profile
import org.apache.ignite.IgniteCache
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure

import java.sql.Timestamp
import java.util.UUID
import io.ogdt.fusion.core.db.common.Utils

import scala.reflect.classTag

import scala.jdk.CollectionConverters._
import org.apache.ignite.cache.CacheMode
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class ProfileStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Profile] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_PROFILE"
    override protected var igniteCache: IgniteCache[UUID, Profile] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Profile](cache)
        case false => {
            wrapper.createCache[UUID, Profile](
                wrapper.makeCacheConfig[UUID, Profile]
                .setCacheMode(CacheMode.REPLICATED)
                .setDataRegionName("Fusion")
                // .setQueryEntities(
                //     List(
                //         new QueryEntity(classTag[UUID].runtimeClass, classTag[FilesystemOrganization].runtimeClass)
                //     ).asJava
                // )
                .setName(cache)
                .setSqlSchema(schema)
                .setIndexedTypes(classOf[UUID], classOf[Profile])
            )
        }
    }

    def makeProfile: Profile = {
        implicit val profileStore: ProfileStore = this
        new Profile
    }

    def makeProfilesQuery(queryFilters: ProfileStore.GetProfilesFilters): SqlStoreQuery = {
        var queryString: String = 
            "SELECT PROFILE.id, lastname, firstname, last_login, is_active, PROFILE.created_at, PROFILE.updated_at, " +
            "USER.id, username, password, USER.created_at, USER.updated_at, " +
            "ORGANIZATION.id, label, type, queryable, ORGANIZATION.created_at, ORGANIZATION.updated_at " +
            s"FROM $schema.PROFILE as PROFILE " +
            s"INNER JOIN $schema.USER as USER ON USER.id = PROFILE.user_id " +
            s"INNER JOIN $schema.ORGANIZATION as ORGANIZATION ON ORGANIZATION.id = PROFILE.organization_id"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"PROFILE.id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage lastnames search
            if (filter.lastname.length > 0) {
                innerWhereStatement += s"PROFILE.lastname in (${(for (i <- 1 to filter.lastname.length) yield "?").mkString(",")})"
                queryArgs ++= filter.lastname
            }
            // manage lastnames search
            if (filter.firstname.length > 0) {
                innerWhereStatement += s"PROFILE.firstname in (${(for (i <- 1 to filter.firstname.length) yield "?").mkString(",")})"
                queryArgs ++= filter.firstname
            }
            // manage lastLogin date search
            filter.lastLogin match {
                case Some((test, time)) => {
                    innerWhereStatement += s"PROFILE.last_login ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                }
                case None => ()
            }
            // manage shared state search
            filter.isActive match {
                case Some(value) => {
                    innerWhereStatement += s"PROFILE.is_active = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"PROFILE.created_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                }
                case None => ()
            }
            filter.updatedAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"PROFILE.updated_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                }
                case None => ()
            }
            whereStatements += innerWhereStatement.mkString(" AND ")
        })
        // compile whereStatements
        if (!whereStatements.isEmpty) {
            queryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (!queryFilters.orderBy.isEmpty) {
            queryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"PROFILE.${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        println(queryArgs)
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    def getProfiles(queryFilters: ProfileStore.GetProfilesFilters)(implicit ec: ExecutionContext): Future[List[Profile]] = {
        executeQuery(makeProfilesQuery(queryFilters)).transformWith({
            case Success(profileResults) => {
                var profiles = profileResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        profile <- Right(
                            makeProfile
                            .setId(entityReflection._2(0)(0).toString)
                            .setLastname(entityReflection._2(0)(1).toString)
                            .setFirstname(entityReflection._2(0)(2).toString)
                            .setLastLogin(entityReflection._2(0)(3) match {
                                case lastlogin: Timestamp => lastlogin
                                case _ => null
                            })
                            .setCreatedAt(entityReflection._2(0)(5) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(6) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { profile =>
                            entityReflection._2(0)(4) match {
                                case active: Boolean => {
                                    if(active) Right(profile.setActive)
                                    else Right(profile.setInactive)
                                }
                                case _ => Right(profile)
                            }
                        } flatMap { profile =>
                            var row = entityReflection._2(0)
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
                        }
                    ) yield profile)
                    .getOrElse(null)
                })
                Future.successful(profiles.toList)
            }
            case Failure(cause) => Future.failed(cause) // TODO : changer pour une custom
        })
    }

    def getProfileById(id: String)(implicit ec: ExecutionContext): Future[Profile] = {
        getProfiles(
            ProfileStore.GetProfilesFilters(
                List(
                    ProfileStore.GetProfilesFilter(
                        List(id),
                        List(),
                        List(),
                        None,
                        None,
                        None,
                        None
                    )
                ),
                List()
            )
        ).transformWith({
            case Success(profiles) =>
                profiles.length match {
                    case 0 => Future.failed(new Error(s"Profile ${id} couldn't be found")) // TODO : changer pour une custom
                    case 1 => Future.successful(profiles(0))
                    case _ => Future.failed(new Error(s"Duplicate id issue in ProfileStore")) // TODO : changer pour une custom
                }
            case Failure(cause) => Future.failed(new Exception("Failed to get profile by id", cause)) // TODO : changer pour une custom
        })
    }

    def persistProfile(profile: Profile)(implicit ec: ExecutionContext): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), Some(relatedOrganization)) => {
                Utils.igniteToScalaFuture(igniteCache.putAsync(
                    profile.id, profile
                )).transformWith({
                    case Success(value) => Future.successful()
                    case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
                })
            }
            case (None, _) => Future.failed(new Error("relatedUser not found and can't be set to null")) // TODO : changer pour une custom
            case (_, None) => Future.failed(new Error("relatedOrganization not found and can't be set to null")) // TODO : changer pour une custom
        }
    }

    /** A result of bulkPersistProfiles method
      * 
      * @constructor create a new BulkPersistProfilesResult with a count of inserted Profiles and a list of errors
      * @param inserts a count of the effectively inserted Profiles
      * @param errors a list of errors catched from a profile insertion
      */
    case class BulkPersistProfilesResult(inserts: Int, errors: List[String])

    def bulkPersistProfiles(profiles: List[Profile])(implicit ec: ExecutionContext): Future[BulkPersistProfilesResult] = {
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
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def deleteProfile(profile: Profile)(implicit ec: ExecutionContext): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), _) => Future.failed(new Error("Profile has still attached User"))
            case (_, Some(relatedOrganization)) => Future.failed(new Error("Profile has still attached Organization"))
            case (None, None) => {
                Utils.igniteToScalaFuture(igniteCache.removeAsync(profile.id))
                .transformWith({
                    case Success(value) => Future.successful()
                    case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
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

    def bulkDeleteProfiles(profiles: List[Profile])(implicit ec: ExecutionContext): Future[BulkDeleteProfilesResult] = {
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
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }
}

object ProfileStore {
    case class GetProfilesFilter(
        id: List[String],
        lastname: List[String],
        firstname: List[String],
        lastLogin: Option[(String, Timestamp)],
        isActive: Option[Boolean],
        createdAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)], // (date, (eq, lt, gt, ne))
    )
    case class GetProfilesFilters(
        filters: List[GetProfilesFilter],
        orderBy: List[(String, Int)] // (column, direction)
    ) extends GetEntityFilters
}
