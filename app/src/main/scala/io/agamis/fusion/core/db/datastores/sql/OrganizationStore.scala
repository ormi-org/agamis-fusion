package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.models.sql.Organization
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.GetEntityFilters
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure

import scala.concurrent.Future
import java.sql.Timestamp
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.agamis.fusion.core.db.common.Utils
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode
import io.agamis.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.Try
import io.agamis.fusion.core.db.models.sql.generics.Language
import io.agamis.fusion.core.db.models.sql.relations.OrganizationApplication

import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.{
    OrganizationNotPersistedException,
    DuplicateOrganizationException,
    OrganizationNotFoundException
}

import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizationtypes.{
    OrganizationtypeNotFoundException,
    DuplicateOrganizationtypeException
}

import org.apache.ignite.cache.QueryEntity
import io.agamis.fusion.core.db.models.sql.generics.Email
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.generics.exceptions.texts.TextNotFoundException

class OrganizationStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Organization] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_ORGANIZATION"
    override protected val igniteCache: IgniteCache[UUID, Organization] = if (wrapper.cacheExists(cache)) {
        wrapper.getCache[UUID, Organization](cache)
    } else {
        wrapper.createCache[UUID, Organization](
            wrapper.makeCacheConfig[UUID, Organization]
              .setCacheMode(CacheMode.REPLICATED)
              .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
              .setDataRegionName("Fusion")
              .setQueryEntities(
                  List(
                      new QueryEntity(classOf[String], classOf[OrganizationApplication])
                        .setTableName("ORGANIZATION_APPLICATION")
                  ).asJava
              )
              .setName(cache)
              .setSqlSchema(schema)
              .setIndexedTypes(classOf[UUID], classOf[Organization])
        )
    }

    // Create and get new Organization Object
    def makeOrganization: Organization = {
        implicit val organizationStore: OrganizationStore = this
        new Organization
    }

    def makeOrganizationsQuery(queryFilters: OrganizationStore.GetOrganizationsFilters): SqlStoreQuery = {
        var baseQueryString = queryString.replace("$schema", schema)
        val queryArgs: ListBuffer[String] = ListBuffer()
        val whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            val innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.nonEmpty) {
                innerWhereStatement += s"org_id in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage labels search
            if (filter.label.nonEmpty) {
                innerWhereStatement += s"org_label in (${(for (_ <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage types search
            if (filter.oType.nonEmpty) {
                innerWhereStatement += s"org_type in (${(for (_ <- 1 to filter.oType.length) yield "?").mkString(",")})"
                queryArgs ++= filter.oType
            }
            // manage shared state search
            filter.queryable match {
                case Some(value) =>
                    innerWhereStatement += s"queryable = ?"
                    queryArgs += value.toString
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) =>
                    innerWhereStatement += s"org_created_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                case None => ()
            }
            filter.updatedAt match {
                case Some((test, time)) =>
                    innerWhereStatement += s"org_updated_at ${
                        test match {
                            case "eq" => "="
                            case "gt" => ">"
                            case "lt" => "<"
                            case "neq" => "<>"
                        }
                    } ?"
                    queryArgs += time.toString
                case None => ()
            }
            whereStatements += innerWhereStatement.mkString(" AND ")
        })
        // compile whereStatements
        if (whereStatements.nonEmpty) {
            baseQueryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (queryFilters.orderBy.nonEmpty) {
            baseQueryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"org_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(baseQueryString)
        .setParams(queryArgs.toList)
    }

    // Get existing organizations from database
    def getOrganizations(queryFilters: OrganizationStore.GetOrganizationsFilters)(implicit ec: ExecutionContext): Future[List[Organization]] = {
        executeQuery(
            makeOrganizationsQuery(queryFilters)
        ).transformWith({
            case Success(rows) =>
                val entityReflections = rows.groupBy(_.head)
                val organizations = rows.map(_.head).distinct.map(entityReflections(_)).map(entityReflection => {
                    val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 5, 6)
                    groupedRows.get("ORGANIZATION") match {
                        case Some(organizationReflections) =>
                            val orgDef = organizationReflections.head.head._2
                            (for (
                                organization <- Right(
                                    makeOrganization
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
                                        case Success(queryable) =>
                                            if (queryable) Right(organization.setQueryable())
                                            else Right(organization.setUnqueryable())
                                        case Failure(_) => Right(organization)
                                    }
                                } flatMap { organization =>
                                    organizationReflections.head.partition(_._1 == "ORGTYPE") match {
                                        case result =>
                                            result._1.length match {
                                                case 0 => Future.failed(OrganizationtypeNotFoundException(s"Organization type of organization ${organization.id}:${organization.label} might be undefined"))
                                                case 1 =>
                                                    val orgTypeDef = result._1(0)._2
                                                    for (
                                                        organizationType <- Right(new OrganizationTypeStore()
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
                                                        ) flatMap { organizationType =>
                                                            result._2.filter(_._1 == "ORGTYPE_LABEL_LANG_VARIANT") match {
                                                                case result =>
                                                                    if (result.isEmpty) {
                                                                        Future.failed(TextNotFoundException(s"Organization type ${organizationType.id} might lack of label in any language"))
                                                                    } else {
                                                                        result.foreach({ orgTypeLangVariant =>
                                                                            val orgTypeLangVariantDef = orgTypeLangVariant._2
                                                                            organizationType.setLabel(
                                                                                Language.apply
                                                                                  .setId(orgTypeLangVariantDef(4))
                                                                                  .setCode(orgTypeLangVariantDef(3))
                                                                                  .setLabel(orgTypeLangVariantDef(5)),
                                                                                orgTypeLangVariantDef(2)
                                                                            )
                                                                        })
                                                                    }
                                                            }
                                                            Right(organizationType)
                                                        }
                                                    ) yield organization.setType(organizationType)
                                                case _ => Future.failed(DuplicateOrganizationtypeException(s"Organization ${organization.id}:${organization.label} has duplicate organization types"))
                                            }
                                    }
                                    Right(organization)
                                } flatMap { organization => // pass created organization to relation mapping step
                                    // profile mapping
                                    groupedRows.get("PROFILE") match {
                                        case Some(profileReflections) =>
                                            profileReflections.foreach({ profileReflection =>
                                                val profileDef = profileReflection(0)._2
                                                for (
                                                    profile <- Right(new ProfileStore()
                                                      .makeProfile
                                                      .setId(profileDef(0))
                                                      .setLastname(profileDef(1))
                                                      .setFirstname(profileDef(2))
                                                      .setLastLogin(Utils.timestampFromString(profileDef(4)) match {
                                                          case lastLogin: Timestamp => lastLogin
                                                          case _ => null
                                                      })
                                                      .setRelatedUser(new UserStore()
                                                        .makeUser
                                                        .setId(profileDef(6))
                                                      )
                                                      .setRelatedOrganization(new OrganizationStore()
                                                        .makeOrganization
                                                        .setId(profileDef(7))
                                                      )
                                                      .setCreatedAt(Utils.timestampFromString(profileDef(8)) match {
                                                          case createdAt: Timestamp => createdAt
                                                          case _ => null
                                                      })
                                                      .setUpdatedAt(Utils.timestampFromString(profileDef(9)) match {
                                                          case updatedAt: Timestamp => updatedAt
                                                          case _ => null
                                                      })
                                                    ) flatMap { profile =>
                                                        Try(profileDef(5).toBoolean) match {
                                                            case Success(isActive) =>
                                                                if (isActive) Right(profile.setActive())
                                                                else Right(profile.setInactive())
                                                            case Failure(_) => Right(profile)
                                                        }
                                                    } flatMap { profile =>
                                                        val profileMainEmailDef = profileDef(3).split(";")
                                                        profileMainEmailDef.length match {
                                                            case 2 =>
                                                                Right(profile.setMainEmail(Email.apply
                                                                  .setId(profileMainEmailDef(0))
                                                                  .setAddress(profileMainEmailDef(1))
                                                                ))
                                                            case _ => Right(profile)
                                                        }
                                                    }
                                                ) yield organization.addRelatedProfile(profile)
                                            })
                                        case None =>
                                    }

                                    groupedRows.get("FS") match {
                                        case Some(fsReflections) =>
                                            fsReflections.foreach({ fsReflection =>
                                                val fsDef = fsReflection(0)._2
                                                for (
                                                    fileSystem <- Right(new FileSystemStore()
                                                      .makeFileSystem
                                                      .setId(fsDef(0))
                                                      .setRootdirId(fsDef(1))
                                                      .setLabel(fsDef(2))
                                                      .setCreatedAt(Utils.timestampFromString(fsDef(5)) match {
                                                          case createdAt: Timestamp => createdAt
                                                          case _ => null
                                                      })
                                                      .setUpdatedAt(Utils.timestampFromString(fsDef(6)) match {
                                                          case updatedAt: Timestamp => updatedAt
                                                          case _ => null
                                                      })
                                                    ) flatMap { fileSystem =>
                                                        Try(fsDef(3).toBoolean) match {
                                                            case Success(shared) =>
                                                                if (shared) Right(fileSystem.setShared())
                                                                else Right(fileSystem.setUnshared())
                                                            case Failure(_) => Right(fileSystem)
                                                        }
                                                    }
                                                ) yield {
                                                    Try(fsDef(4).toBoolean) match {
                                                        case Success(isDefault) =>
                                                            organization.addFileSystem(fileSystem)
                                                            if (isDefault) organization.setDefaultFileSystem(fileSystem)
                                                            Right(organization)
                                                        case Failure(_) => Right(organization)
                                                    }
                                                }
                                            })
                                        case None =>
                                    }
                                    Right(organization)
                                }
                            ) yield organization).getOrElse(null)
                        case None =>
                    }
                })
                Future.successful(organizations.toList.asInstanceOf[List[Organization]])
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    def getAllOrganizations(implicit ec: ExecutionContext): Future[List[Organization]] = {
        getOrganizations(OrganizationStore.GetOrganizationsFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(organizations) => 
                if (organizations.isEmpty) {
                    Future.failed(NoEntryException("Organization store is empty"))
                } else {
                    Future.successful(organizations)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Get a single existing organization from database by its id
    def getOrganizationById(id: String)(implicit ec: ExecutionContext): Future[Organization] = {
        getOrganizations(
            OrganizationStore.GetOrganizationsFilters().copy(
                filters = List(
                    OrganizationStore.GetOrganizationsFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(organizations) => 
                organizations.length match {
                    case 0 => Future.failed(OrganizationNotFoundException(s"Organization $id couldn't be found"))
                    case 1 => Future.successful(organizations.head)
                    case _ => Future.failed(new DuplicateOrganizationException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Save organization object's modification to database
    def persistOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            organization.id, organization
        )).transformWith({
            case Success(_) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Save several object's modifications
    def bulkPersistOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[Unit] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (organizations.map(_.id) zip organizations).toMap[UUID, Organization].asJava
        )).transformWith({
            case Success(_) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Delete organization from database
    def deleteOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[Unit] = {
        if (organization.relatedProfiles.nonEmpty) return Future.failed(OrganizationNotPersistedException("Organization still contains profiles and can't be deleted"))
        Utils.igniteToScalaFuture(igniteCache.removeAsync(organization.id))
        .transformWith({
            case Success(_) => Future.unit
            case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
        })
    }

    // Delete several organizations from database
    def bulkDeleteOrganizations(organizations: List[Organization])(implicit ec: ExecutionContext): Future[Unit] = {
        organizations.find({ org => org.relatedProfiles.nonEmpty }) match {
            case Some(found) => Future.failed(OrganizationNotPersistedException(s"Organization ${found.id}:${found.label} still contains profiles and can't be deleted"))
            case None =>
                Utils.igniteToScalaFuture(igniteCache.removeAllAsync(organizations.map(_.id).toSet.asJava))
                .transformWith({
                    case Success(_) => Future.unit
                    case Failure(cause) => Future.failed(OrganizationNotPersistedException(cause))
                })
        }
    }
}

object OrganizationStore {
    case class GetOrganizationsFilter(
        id: List[String] = List(),
        label: List[String] = List(),
        oType: List[String] = List(),
        queryable: Option[Boolean] = None,
        createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
        updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
    )
    case class GetOrganizationsFilters(
        filters: List[GetOrganizationsFilter] = List(),
        orderBy: List[(String, Int)] = List()
    ) extends GetEntityFilters
}