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

import io.ogdt.fusion.core.db.models.sql.relations.{
    ProfileEmail,
    ProfileGroup,
    ProfilePermission
}

import java.sql.Timestamp
import java.util.UUID
import io.ogdt.fusion.core.db.common.Utils

import scala.jdk.CollectionConverters._
import org.apache.ignite.cache.CacheMode
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import org.apache.ignite.cache.CacheAtomicityMode

import io.ogdt.fusion.core.db.datastores.sql.exceptions.profiles.{
    ProfileNotPersistedException,
    ProfileQueryExecutionException,
    DuplicateProfileException,
    ProfileNotFoundException
}

import org.apache.ignite.cache.QueryEntity
import scala.util.Try
import io.ogdt.fusion.core.db.datastores.sql.generics.EmailStore
import io.ogdt.fusion.core.db.models.sql.OrganizationType
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException

import io.ogdt.fusion.core.db.models.sql.relations.{
    ProfileEmail,
    ProfileGroup,
    ProfilePermission
}
import io.ogdt.fusion.core.db.datastores.sql.exceptions.users.UserNotFoundException
import java.time.Instant
import io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException
import io.ogdt.fusion.core.db.models.sql.generics.Email
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.OrganizationNotFoundException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.DuplicateOrganizationException
import io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.emails.EmailNotFoundException

class ProfileStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Profile] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_PROFILE"
    override protected var igniteCache: IgniteCache[UUID, Profile] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, Profile](cache)
        case false => {
            wrapper.createCache[UUID, Profile](
                wrapper.makeCacheConfig[UUID, Profile]
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setDataRegionName("Fusion")
                .setQueryEntities(
                    List(
                        new QueryEntity(classOf[String], classOf[ProfileEmail])
                        .setTableName("PROFILE_EMAIL"),
                        new QueryEntity(classOf[String], classOf[ProfileGroup])
                        .setTableName("PROFILE_GROUP"),
                        new QueryEntity(classOf[String], classOf[ProfilePermission])
                        .setTableName("PROFILE_PERMISSION")
                    ).asJava
                )
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
        var baseQueryString = queryString.replace("$schema", schema)
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"profile_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage lastnames search
            if (filter.lastname.length > 0) {
                innerWhereStatement += s"profile_lastname in (${(for (i <- 1 to filter.lastname.length) yield "?").mkString(",")})"
                queryArgs ++= filter.lastname
            }
            // manage lastnames search
            if (filter.firstname.length > 0) {
                innerWhereStatement += s"profile_firstname in (${(for (i <- 1 to filter.firstname.length) yield "?").mkString(",")})"
                queryArgs ++= filter.firstname
            }
            // manage lastLogin date search
            filter.lastLogin match {
                case Some((test, time)) => {
                    innerWhereStatement += s"profile_last_login ${
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
                    innerWhereStatement += s"profile_is_active = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"profile_created_at ${
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
                    innerWhereStatement += s"profile_updated_at ${
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
            baseQueryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (!queryFilters.orderBy.isEmpty) {
            baseQueryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"profile_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        println(queryArgs)
        makeQuery(baseQueryString)
        .setParams(queryArgs.toList)
    }

    def getProfiles(queryFilters: ProfileStore.GetProfilesFilters)(implicit ec: ExecutionContext): Future[List[Profile]] = {
        executeQuery(makeProfilesQuery(queryFilters)).transformWith({
            case Success(rows) => {
                val entityReflections = rows.groupBy(_(0))
                var profiles = rows.map(_(0)).distinct.map(entityReflections.get(_).get).map(entityReflection => {
                    val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 7, 8)
                    groupedRows.get("PROFILE") match {
                        case Some(profileReflections) => {
                            val profileDef = profileReflections.head.head._2
                            (for (
                                //PROFILE.id, lastname, firstname, last_login, is_active, user_id, PROFILE.organization_id , PROFILE.created_at , PROFILE.updated_at
                                profile <- Right(
                                    makeProfile
                                    .setId(profileDef(0).toString)
                                    .setLastname(profileDef(1).toString)
                                    .setFirstname(profileDef(2).toString)
                                    .setLastLogin(Utils.timestampFromString(profileDef(3)) match {
                                        case lastlogin: Timestamp => lastlogin
                                        case _ => null
                                    })
                                    .setCreatedAt(Utils.timestampFromString(profileDef(7)) match {
                                        case createdAt: Timestamp => createdAt
                                        case _ => null
                                    })
                                    .setUpdatedAt(Utils.timestampFromString(profileDef(8)) match {
                                        case updatedAt: Timestamp => updatedAt
                                        case _ => null
                                    })
                                ) flatMap { profile =>
                                    Try(profileDef(4).toBoolean) match {
                                        case Success(active) => {
                                            if (active) Right(profile.setActive)
                                            else Right(profile.setInactive)
                                        }
                                        case Failure(_) => Right(profile)
                                    }
                                } flatMap { profile =>
                                    groupedRows.get("USER") match {
                                        case Some(userReflections) => {
                                            userReflections.foreach({ userReflection =>
                                                val userDef = userReflection.head._2
                                                (for (
                                                    user <- Right(new UserStore()
                                                        .makeUser
                                                        .setId(userDef(0))
                                                        .setUsername(userDef(1))
                                                        .setPasswordHash(userDef(2))
                                                        .setCreatedAt(Utils.timestampFromString(userDef(3)) match {
                                                            case createdAt: Timestamp => createdAt
                                                            case _ => null
                                                        })
                                                        .setUpdatedAt(Utils.timestampFromString(userDef(4)) match {
                                                            case updatedAt: Timestamp => updatedAt
                                                            case _ => null
                                                        })
                                                    )
                                                ) yield profile.setRelatedUser(user))
                                            })
                                        }
                                        case None => Future.failed(new UserNotFoundException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname}"))
                                    }

                                    groupedRows.get("EMAIL") match {
                                        case Some(emailReflections) => {
                                            emailReflections.isEmpty match {
                                                case true => Future.failed(EmailNotFoundException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname} might have no email address"))
                                                case false => {
                                                    emailReflections.foreach({ emailReflection =>
                                                        val emailDef = emailReflection.head._2
                                                        (for (
                                                            email <- Right(Email.apply
                                                            .setId(emailDef(0))
                                                            .setAddress(emailDef(1)))
                                                        ) yield {
                                                            Try(emailDef(2).toBoolean) match {
                                                                case Success(isMain) => {
                                                                    if (isMain) profile.setMainEmail(email)
                                                                    else profile.addEmail(email)
                                                                }
                                                                case Failure(_) => {}
                                                            }
                                                        })
                                                    })
                                                }
                                            }
                                        }
                                        case None => {}
                                    }

                                    groupedRows.get("ORGANIZATION") match {
                                        case Some(organizationReflections) => {
                                            organizationReflections.size match {
                                                case 0 => Future.failed(OrganizationNotFoundException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname} might be orphan"))
                                                case 1 => {
                                                    val organizationReflection = organizationReflections.head
                                                    organizationReflection.partition(_._1 == "ORGANIZATION") match {
                                                        case result => {
                                                            result._1.length match {
                                                                case 0 => Future.failed(OrganizationNotFoundException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname} might be orphan"))
                                                                case 1 => {
                                                                    val orgDef = result._1(0)._2
                                                                    (for (
                                                                        organization <- Right(new OrganizationStore()
                                                                            .makeOrganization
                                                                            .setId(orgDef(0))
                                                                            .setLabel(orgDef(1))
                                                                            .setCreatedAt(Utils.timestampFromString(orgDef(3)) match {
                                                                                case createdAt: Timestamp => createdAt
                                                                                case _ => null
                                                                            })
                                                                            .setUpdatedAt(Utils.timestampFromString(orgDef(4)) match {
                                                                                case updatedAt: Timestamp => updatedAt
                                                                                case _ => null
                                                                            })
                                                                        ) flatMap { organization =>
                                                                            Try(orgDef(2).toBoolean) match {
                                                                                case Success(queryable) => {
                                                                                    if (queryable) Right(organization.setQueryable)
                                                                                    else Right(organization.setUnqueryable)
                                                                                }
                                                                                case Failure(cause) => Right(organization)
                                                                            }
                                                                        } flatMap { organization =>
                                                                            result._2.partition(_._1 == "ORGTYPE") match {
                                                                                case result => {
                                                                                    result._1.length match {
                                                                                        case 0 => 
                                                                                        case 1 => {
                                                                                            val orgTypeDef = result._1(0)._2
                                                                                            val orgType = new OrganizationTypeStore()
                                                                                            .makeOrganizationType
                                                                                            .setId(orgTypeDef(1))
                                                                                            .setLabelTextId(orgTypeDef(2))
                                                                                            .setCreatedAt(Utils.timestampFromString(orgTypeDef(3)) match {
                                                                                                case createdAt: Timestamp => createdAt
                                                                                                case _ => null
                                                                                            })
                                                                                            .setUpdatedAt(Utils.timestampFromString(orgTypeDef(4)) match {
                                                                                                case updatedAt: Timestamp => updatedAt
                                                                                                case _ => null
                                                                                            })
                                                                                            result._2.foreach({ result =>
                                                                                                val orgTypeLangVariantDef = result._2
                                                                                                orgType.setLabel(
                                                                                                    Language.apply
                                                                                                    .setId(orgTypeLangVariantDef(4))
                                                                                                    .setCode(orgTypeLangVariantDef(3))
                                                                                                    .setLabel(orgTypeLangVariantDef(5)),
                                                                                                    orgTypeLangVariantDef(2)
                                                                                                )
                                                                                            })
                                                                                            organization.setType(orgType)
                                                                                        }
                                                                                        case _ => 
                                                                                    }
                                                                                }
                                                                            }
                                                                            Right(organization)
                                                                        }
                                                                    ) yield profile.setRelatedOrganization(organization))
                                                                }
                                                                case _ => Future.failed(DuplicateOrganizationException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname} has duplicate organization relation"))
                                                            }
                                                        }
                                                    }
                                                }
                                                case _ => Future.failed(DuplicateOrganizationException(s"Profile ${profile.id}:${profile.firstname}.${profile.lastname} has duplicate organization relation"))
                                            }
                                        }
                                        case None => {}
                                    }

                                    groupedRows.get("PERMISSION") match {
                                        case Some(permissionReflections) => {
                                            permissionReflections.foreach({ permissionReflection =>
                                                permissionReflection.partition(_._1 == "PERMISSION") match {
                                                    case result => {
                                                        result._1.length match {
                                                            case 0 => 
                                                            case _ => {
                                                                val permissionDef = result._1.head._2
                                                                (for (
                                                                    permission <- Right(new PermissionStore()
                                                                        .makePermission
                                                                        .setId(permissionDef(0))
                                                                        .setKey(permissionDef(1))
                                                                        .setLabelTextId(permissionDef(2))
                                                                        .setDescriptionTextId(permissionDef(3))
                                                                        .setCreatedAt(Utils.timestampFromString(permissionDef(6)) match {
                                                                            case createdAt: Timestamp => createdAt
                                                                            case _ => null
                                                                        })
                                                                        .setUpdatedAt(Utils.timestampFromString(permissionDef(7)) match {
                                                                            case updatedAt: Timestamp => updatedAt
                                                                            case _ => null
                                                                        })
                                                                    ) flatMap { permission =>
                                                                        Try(permissionDef(4).toBoolean) match {
                                                                            case Success(editable) => {
                                                                                if (editable) Right(permission.setEditable)
                                                                                else Right(permission.setReadonly)
                                                                            }
                                                                            case Failure(cause) => Right(permission)
                                                                        }
                                                                    } flatMap { permission =>
                                                                        result._2.partition(_._1 == "PERMISSION_LABEL_LANG_VARIANT") match {
                                                                            case result => {
                                                                                result._1.isEmpty match {
                                                                                    case true => Future.failed(TextNotFoundException(s"Permission ${permission.id} might lack of label in any language"))
                                                                                    case false => {
                                                                                        result._1.foreach({ labelLangVariant =>
                                                                                            val variantDef = labelLangVariant._2
                                                                                            permission.setLabel(
                                                                                                Language.apply
                                                                                                .setId(variantDef(3).toString)
                                                                                                .setCode(variantDef(2).toString)
                                                                                                .setLabel(variantDef(4).toString),
                                                                                                variantDef(1).toString
                                                                                            )
                                                                                        })
                                                                                        result._2.filter(_._1 == "PERMISSION_DESC_LANG_VARIANT") match {
                                                                                            case result => {
                                                                                                result.isEmpty match {
                                                                                                    case true => Future.failed(TextNotFoundException(s"Permission ${permission.id} might lack of description in any language"))
                                                                                                    case false => {
                                                                                                        result.foreach({ descLangVariant =>
                                                                                                            val variantDef = descLangVariant._2
                                                                                                            permission.setDescription(
                                                                                                                Language.apply
                                                                                                                .setId(variantDef(3).toString)
                                                                                                                .setCode(variantDef(2).toString)
                                                                                                                .setLabel(variantDef(4).toString),
                                                                                                                variantDef(1).toString
                                                                                                            )
                                                                                                        })
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        Right(permission)
                                                                    }
                                                                ) yield profile.addPermission(permission))
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                        case None => {}
                                    }

                                    Right(profile)
                                }
                            ) yield profile)
                            .getOrElse(null)
                        }
                        case None => {}
                    }
                })
                Future.successful(profiles.toList.asInstanceOf[List[Profile]])
            }
            case Failure(cause) => Future.failed(ProfileQueryExecutionException(cause))
        })
    }

    def getAllProfiles(implicit ec: ExecutionContext): Future[List[Profile]] = {
        getProfiles(
            ProfileStore.GetProfilesFilters().copy(
                orderBy = List(
                    ("id", 1)
                )
            )
        ).transformWith({
            case Success(profiles) =>
                profiles.length match {
                    case 0 => Future.failed(new NoEntryException("Profile store is empty"))
                    case _ => Future.successful(profiles)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getProfileById(id: String)(implicit ec: ExecutionContext): Future[Profile] = {
        getProfiles(
            ProfileStore.GetProfilesFilters().copy(
                filters = List(
                    ProfileStore.GetProfilesFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(profiles) =>
                profiles.length match {
                    case 0 => Future.failed(new ProfileNotFoundException(s"Profile ${id} couldn't be found"))
                    case 1 => Future.successful(profiles.head)
                    case _ => Future.failed(new DuplicateProfileException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def persistProfile(profile: Profile)(implicit ec: ExecutionContext): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), Some(relatedOrganization)) => {
                val transaction = makeTransaction
                transaction match {
                    case Success(tx) => {
                        val emailRelationCache: IgniteCache[String, ProfileEmail] =
                            wrapper.getCache[String, ProfileEmail](cache)
                        val groupRelationCache: IgniteCache[String, ProfileGroup] =
                            wrapper.getCache[String, ProfileGroup](cache)
                        val permissionRelationCache: IgniteCache[String, ProfilePermission] =
                            wrapper.getCache[String, ProfilePermission](cache)
                        Future.sequence(
                            List(
                                // Save entity
                                Utils.igniteToScalaFuture(igniteCache.putAsync(
                                    profile.id, profile
                                )),
                                // Save emails
                                Utils.igniteToScalaFuture(emailRelationCache.putAllAsync(
                                    (profile.emails
                                    .filter(_._1 == true)
                                    .map({ email =>
                                        (
                                            profile.id +":"+ email._2.id,
                                            ProfileEmail(
                                                profile.id,
                                                email._2.id
                                            )
                                        )
                                    }) ++ List(
                                        (
                                            profile.id +":"+ profile.mainEmail.id,
                                            ProfileEmail(
                                                profile.id,
                                                profile.mainEmail.id,
                                                true
                                            )
                                        )
                                    )).toMap[String, ProfileEmail].asJava
                                )),
                                // Remove emails
                                Utils.igniteToScalaFuture(emailRelationCache.removeAllAsync(
                                    profile.emails
                                    .filter(_._1 == false)
                                    .map({ email => profile.id+":"+email._2.id }).toSet.asJava
                                )),
                                // Save groups
                                Utils.igniteToScalaFuture(groupRelationCache.putAllAsync(
                                    (profile.groups
                                    .filter(_._1 == true)
                                    .map({ group =>
                                        (
                                            profile.id+":"+group._2.id,
                                            ProfileGroup(
                                                profile.id,
                                                group._2.id
                                            )
                                        )
                                    })).toMap.asJava
                                )),
                                // Remove groups
                                Utils.igniteToScalaFuture(groupRelationCache.removeAllAsync(
                                    (profile.groups
                                    .filter(_._1 == false)
                                    .map({ group =>profile.id+":"+group._2.id })).toSet.asJava
                                )),
                                // Save permissions
                                Utils.igniteToScalaFuture(permissionRelationCache.putAllAsync(
                                    profile.permissions
                                    .filter(_._1 == true)
                                    .map({ permission => 
                                        (
                                            profile.id+":"+permission._2.id,
                                            ProfilePermission(
                                                profile.id,
                                                permission._2.id
                                            )
                                        )
                                    }).toMap.asJava
                                )),
                                // Remove permission
                                Utils.igniteToScalaFuture(permissionRelationCache.removeAllAsync(
                                    profile.permissions
                                    .filter(_._1 == false)
                                    .map({ permission => profile.id+":"+permission._2.id }).toSet.asJava
                                ))
                            )
                        ).transformWith({
                            case Success(value) => {
                                commitTransaction(transaction).transformWith({
                                    case Success(value) => Future.unit
                                    case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
                                })
                            }
                            case Failure(cause) => {
                                rollbackTransaction(transaction)
                                Future.failed(ProfileNotPersistedException(cause))
                            }
                        })
                    }
                    case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
                }
                Utils.igniteToScalaFuture(igniteCache.putAsync(
                    profile.id, profile
                )).transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
                })
            }
            case (None, _) => Future.failed(ProfileNotPersistedException("relatedUser not found and can't be set to null"))
            case (_, None) => Future.failed(ProfileNotPersistedException("relatedOrganization not found and can't be set to null"))
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
            case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
        })
    }

    def deleteProfile(profile: Profile)(implicit ec: ExecutionContext): Future[Unit] = {
        (profile.relatedUser, profile.relatedOrganization) match {
            case (Some(relatedUser), _) => Future.failed(new Error("Profile has still attached User"))
            case (_, Some(relatedOrganization)) => Future.failed(new Error("Profile has still attached Organization"))
            case (None, None) => {
                Utils.igniteToScalaFuture(igniteCache.removeAsync(profile.id))
                .transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
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
            case Failure(cause) => Future.failed(ProfileNotPersistedException(cause))
        })
    }
}

object ProfileStore {
    case class GetProfilesFilter(
        id: List[String] = List(),
        lastname: List[String] = List(),
        firstname: List[String] = List(),
        lastLogin: Option[(String, Timestamp)] = None,
        isActive: Option[Boolean] = None,
        createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
    )
    case class GetProfilesFilters(
        filters: List[GetProfilesFilter] = List(),
        orderBy: List[(String, Int)] = List() // (column, direction)
    ) extends GetEntityFilters
}
