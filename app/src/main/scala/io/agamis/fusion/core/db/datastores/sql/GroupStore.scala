package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.ProfileStore.{GetProfilesFilter, GetProfilesFilters}
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.groups.{DuplicateGroupException, GroupNotFoundException, GroupNotPersistedException, GroupQueryExecutionException}
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.{DuplicateOrganizationException, OrganizationNotFoundException}
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.{GetEntityFilters, SqlStoreQuery}
import io.agamis.fusion.core.db.models.sql.{Group, Organization}
import io.agamis.fusion.core.db.models.sql.generics.{Email, Language}
import io.agamis.fusion.core.db.models.sql.relations.GroupPermission
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.{CacheAtomicityMode, CacheMode, QueryEntity}

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}
import org.apache.ignite.transactions.Transaction

class GroupStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, Group] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_GROUP"
  override protected val igniteCache: IgniteCache[UUID, Group] = if (wrapper.cacheExists(cache)) {
    wrapper.getCache[UUID, Group](cache)
  } else {
    wrapper.createCache[UUID, Group](
      wrapper.makeCacheConfig[UUID, Group]
        .setCacheMode(CacheMode.REPLICATED)
        .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
        .setDataRegionName("Fusion")
        .setQueryEntities(
          List(
            new QueryEntity(classOf[String], classOf[GroupPermission])
              .setTableName("GROUP_PERMISSION")
          ).asJava
        )
        .setName(cache)
        .setSqlSchema(schema)
        .setIndexedTypes(classOf[UUID], classOf[Group])
    )
  }

  /** A factory method that generates a new group object
    *
    * [[Group Group]] is generated along with its '''implicit''' [[GroupStore GroupStore]]
    *
    * @return a simple [[Group Group]]
    */
  def makeGroup: Group = {
    implicit val groupStore: GroupStore = this
    new Group
  }

  /** A factory method that generates an SQL query based on provided filters
    *
    * @param queryFilters the filters used to populate the query
    * @return a simple [[SqlStoreQuery SqlStoreQuery]]
    */
  def makeGroupsQuery(queryFilters: GroupStore.GetGroupsFilters): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$schema", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"group_id in (${(for (_ <- 1 to filter.id.length) yield "?").mkString(",")})"
        queryArgs ++= filter.id
      }
      // manage names search
      if (filter.name.nonEmpty) {
        innerWhereStatement += s"group_name in (${(for (_ <- 1 to filter.name.length) yield "?").mkString(",")})"
        queryArgs ++= filter.name
      }
      // manage metadate search
      filter.createdAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"group_created_at ${
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
          innerWhereStatement += s"group_updated_at ${
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
      baseQueryString += s" ORDER BY ${
        queryFilters.orderBy.map(o =>
          s"group_${o._1} ${
            o._2 match {
              case 1 => "ASC"
              case -1 => "DESC"
            }
          }"
        ).mkString(", ")
      }"
    }
    makeQuery(baseQueryString)
      .setParams(queryArgs.toList)
  }

  /** A method that gets several existing groups from database based on provided filters
    *
    * @note used as a generic methods wich parse result in Object sets to process it; it is used in regular SELECT based methods
    * @param queryFilters the filters used to populate the query
    * @param ec           the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future [[List List]] of [[Group Group]]
    */
  def getGroups(queryFilters: GroupStore.GetGroupsFilters)(implicit ec: ExecutionContext): Future[List[Group]] = {
    executeQuery(makeGroupsQuery(queryFilters)).transformWith({
      case Success(rows) =>
        // map each group from queryResult by grouping results by GROUP.id and mapping to group objects creation
        val entityReflections = rows.groupBy(_.head)
        val groups = rows.map(_.head).distinct.map(entityReflections(_)).map(entityReflection => {
          val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 4, 5)
          groupedRows.get("GROUP") match {
            case Some(groupReflections) =>
              val groupDef = groupReflections.head.head._2
              (for (
                // Start a for comprehension
                group <- Right(makeGroup
                  .setId(groupDef(0))
                  .setName(groupDef(1))
                  .setCreatedAt(Utils.timestampFromString(groupDef(2)) match {
                    case createdAt: Timestamp => createdAt
                    case _ => null
                  })
                  .setUpdatedAt(Utils.timestampFromString(groupDef(3)) match {
                    case updatedAt: Timestamp => updatedAt
                    case _ => null
                  })
                ) flatMap { group =>
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
                            .setCreatedAt(Utils.timestampFromString(profileDef(6)) match {
                              case createdAt: Timestamp => createdAt
                              case _ => null
                            })
                            .setUpdatedAt(Utils.timestampFromString(profileDef(7)) match {
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
                        ) yield group.addMember(profile)
                      })
                    case None =>
                  }
                  groupedRows.get("ORGANIZATION") match {
                    case Some(organizationReflections) =>
                      organizationReflections.size match {
                        case 0 => Future.failed(OrganizationNotFoundException(s"Group ${group.id}:${group.name} might be orphan"))
                        case 1 =>
                          val organizationReflection = organizationReflections.last
                          organizationReflection.partition(_._1 == "ORGANIZATION") match {
                            case result =>
                              result._1.length match {
                                case 0 => Future.failed(OrganizationNotFoundException(s"Group ${group.id}:${group.name} might be orphan"))
                                case 1 =>
                                  val orgDef = result._1(0)._2
                                  for (
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
                                        case Success(queryable) =>
                                          if (queryable) Right(organization.setQueryable())
                                          else Right(organization.setUnqueryable())
                                        case Failure(_) => Right(organization)
                                      }
                                    } flatMap { organization =>
                                      result._2.partition(_._1 == "ORGTYPE") match {
                                        case result =>
                                          result._1.length match {
                                            case 0 =>
                                            case 1 =>
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
                                            case _ =>
                                          }
                                      }
                                      Right(organization)
                                    }
                                  ) yield group.setRelatedOrganization(organization)
                                case _ => Future.failed(DuplicateOrganizationException(s"Group ${group.id}:${group.name} has duplicate organization relation"))
                              }
                          }
                        case _ => Future.failed(DuplicateOrganizationException(s"Group ${group.id}:${group.name} has duplicate organization relation"))
                      }
                    case None =>
                  }
                  groupedRows.get("PERMISSION") match {
                    case Some(permissionReflections) =>
                      permissionReflections.foreach({ permissionReflection =>
                        permissionReflection.partition(_._1 == "PERMISSION") match {
                          case result =>
                            result._1.length match {
                              case 0 =>
                              case _ =>
                                val permissionDef = result._1(0)._2
                                for (
                                  permission <- Right(new PermissionStore()
                                    .makePermission
                                    .setId(permissionDef(0))
                                    .setKey(permissionDef(1))
                                    .setLabelTextId(permissionDef(2))
                                    .setDescriptionTextId(permissionDef(3))
                                    .setRelatedApplication(new ApplicationStore()
                                      .makeApplication
                                      .setId(permissionDef(5))
                                    )
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
                                      case Success(editable) =>
                                        if (editable) Right(permission.setEditable)
                                        else Right(permission.setReadonly)
                                      case Failure(_) => Right(permission)
                                    }
                                  } flatMap { permission =>
                                    result._2.partition(_._1 == "PERMISSION_LABEL_LANG_VARIANT") match {
                                      case result =>
                                        result._1.length match {
                                          case 0 =>
                                          case _ =>
                                            result._1.foreach({ labelLangVariant =>
                                              val variantDef = labelLangVariant._2
                                              permission.setLabel(
                                                Language.apply
                                                  .setId(variantDef(3))
                                                  .setCode(variantDef(2))
                                                  .setLabel(variantDef(4)),
                                                variantDef(1)
                                              )
                                            })
                                            result._2.filter(_._1 == "PERMISSION_DESC_LANG_VARIANT").foreach({ labelLangVariant =>
                                              val variantDef = labelLangVariant._2
                                              permission.setDescription(
                                                Language.apply
                                                  .setId(variantDef(3))
                                                  .setCode(variantDef(2))
                                                  .setLabel(variantDef(4)),
                                                variantDef(1)
                                              )
                                            })
                                        }
                                    }
                                    Right(permission)
                                  }
                                ) yield group.addPermission(permission)
                            }
                        }
                      })
                    case None =>
                  }
                  Right(group)
                }
              ) yield group).getOrElse(null)
            case None =>
          }
        })
        Future.successful(groups.toList.asInstanceOf[List[Group]])
      case Failure(cause) => Future.failed(GroupQueryExecutionException(cause))
    })
  }

  /** A method that gets all existing groups
    *
    * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future [[List List]] of [[Group Group]]
    */
  def getAllGroups(implicit ec: ExecutionContext): Future[List[Group]] = {
    getGroups(GroupStore.GetGroupsFilters().copy(
      orderBy = List(
        ("id", 1)
      )
    )).transformWith({
      case Success(groups) =>
        groups.length match {
          case 0 => Future.failed(NoEntryException("Group store is empty"))
          case _ => Future.successful(groups)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  /** A method that gets an existing group by its id
    *
    * @param id id of the group to retrieve
    * @param ec the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future [[Group Group]] that corresponds to the provided id
    */
  def getGroupById(id: String)(implicit ec: ExecutionContext): Future[Group] = {
    getGroups(
      GroupStore.GetGroupsFilters().copy(
        filters = List(
          GroupStore.GetGroupsFilter().copy(
            id = List(id)
          )
        )
      )
    ).transformWith({
      case Success(groups) =>
        groups.length match {
          case 0 => Future.failed(GroupNotFoundException(s"Group $id couldn't be found"))
          case 1 => Future.successful(groups.head)
          case _ => Future.failed(new DuplicateGroupException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  /** A method that persist a group to reflect changes in the database
    *
    * @param group the group to persist
    * @param ec    the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future confirmation of the state change
    */
  def persistGroup(group: Group)(implicit ec: ExecutionContext): Future[Transaction] = {
    makeTransaction match {
      case Success(tx) =>
        val relationCache: IgniteCache[String, GroupPermission] =
          wrapper.getCache[String, GroupPermission](cache)
        val profileStore: ProfileStore = new ProfileStore()
        Future.sequence(
          List(
            // Save entity
            Utils.igniteToScalaFuture(igniteCache.putAsync(
              group.id, group
            )),
            // Save permissions
            Utils.igniteToScalaFuture(relationCache.putAllAsync(
              group.permissions
                .filter(_._1 == true)
                .map({ org =>
                  (
                    s"${group.id}:${org._2.id}",
                    GroupPermission(
                      group.id,
                      org._2.id
                    )
                  )
                }).toMap[String, GroupPermission].asJava
            )),
            // Remove permissions
            Utils.igniteToScalaFuture(relationCache.removeAllAsync(
              group.permissions
                .filter(_._1 == false)
                .map(p => s"${group.id}:${p._2.id}").toSet[String].asJava
            )),
            profileStore.getProfiles(
              GetProfilesFilters(
                List(
                  GetProfilesFilter(
                    id = group.members.filter(_._1 == true).map(_._2.id.toString)
                  )
                )
              )
            ).transformWith({
              case Success(profiles) => Future.sequence(profiles.map(_.addGroup(group).persist))
              case Failure(cause) => throw GroupNotPersistedException(cause)
            }),
            profileStore.getProfiles(
              GetProfilesFilters(
                List(
                  GetProfilesFilter(
                    id = group.members.filter(_._1 == false).map(_._2.id.toString)
                  )
                )
              )
            ).transformWith({
              case Success(profiles) => Future.sequence(profiles.map(_.removeGroup(group).persist))
              case Failure(cause) => throw GroupNotPersistedException(cause)
            })
          )
        ).transformWith({
          case Success(_) =>
            Future.successful(tx)
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(GroupNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
    }
  }

  /** A method that persist several groups state in the database
    *
    * @param groups groups objects to persist
    * @param ec     the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future confirmation of the state change
    */
  def bulkPersistGroups(groups: List[Group])(implicit ec: ExecutionContext): Future[Unit] = {
    makeTransaction match {
      case Success(tx) =>
        val permissionRelationCache: IgniteCache[String, GroupPermission] =
          wrapper.getCache[String, GroupPermission](cache)
        val profileStore: ProfileStore = new ProfileStore()
        Future.sequence(
          List(
            // Save entity
            Utils.igniteToScalaFuture(igniteCache.putAllAsync(
              (groups.map(_.id) zip groups).toMap[UUID, Group].asJava
            )),
            // Save permission relations
            Utils.igniteToScalaFuture(permissionRelationCache.putAllAsync(
              groups.flatMap(gp => gp.permissions.filter(_._1 == true).map(relation => s"${gp.id}:${relation._2.id}") zip gp.permissions.map({ perm =>
                GroupPermission(
                  gp.id,
                  perm._2.id
                )
              })).toMap[String, GroupPermission].asJava
            )),
            // Remove permission relations
            Utils.igniteToScalaFuture(permissionRelationCache.removeAllAsync(
              groups.flatMap(gp => gp.permissions.filter(_._1 == false).map(relation => s"${gp.id}:${relation._2.id}")).toSet[String].asJava
            )),
            // Add and remove groups TODO: may be replaced by a map reduce operations aggregation
            Future.sequence(
              groups.map(group => {
                Future.sequence(
                  List(
                    profileStore.getProfiles(
                      GetProfilesFilters(
                        List(
                          GetProfilesFilter(
                            id = group.members.filter(_._1 == true).map(_._2.id.toString)
                          )
                        )
                      )
                    ).transformWith({
                      case Success(profiles) => Future.sequence(profiles.map(_.addGroup(group).persist))
                      case Failure(cause) => throw GroupNotPersistedException(cause)
                    }),
                    profileStore.getProfiles(
                      GetProfilesFilters(
                        List(
                          GetProfilesFilter(
                            id = group.members.filter(_._1 == false).map(_._2.id.toString)
                          )
                        )
                      )
                    ).transformWith({
                      case Success(profiles) => Future.sequence(profiles.map(_.removeGroup(group).persist))
                      case Failure(cause) => throw GroupNotPersistedException(cause)
                    })
                  )
                )
              })
            )
          )
        ).transformWith({
          case Success(_) =>
            commitTransaction(tx).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(GroupNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
    }
  }

  /** A method that delete group
    *
    * @param group the group to be deleted
    * @param ec    the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future confirmation of state change
    */
  def deleteGroup(group: Group)(implicit ec: ExecutionContext): Future[Transaction] = {
    makeTransaction match {
      case Success(tx) =>
        val relationCache: IgniteCache[String, GroupPermission] =
          wrapper.getCache[String, GroupPermission](cache)
        val profileStore = new ProfileStore()
        val organizationStore = new OrganizationStore()
        Future.sequence(
          List(
            Utils.igniteToScalaFuture(igniteCache.removeAsync(group.id)),
            // Delete relations
            // Delete permission relations
            Utils.igniteToScalaFuture(relationCache.removeAllAsync(
              group.permissions
                .map({ p =>
                  s"${group.id}:${p._2.id}"
                }).toSet[String].asJava
            )),
            // Delete member (Profile) relations
            profileStore
              .getProfiles(
                ProfileStore.GetProfilesFilters().copy(filters = List(
                  ProfileStore.GetProfilesFilter().copy(id = group.members.map(_._2.id.toString))
                ))
              ).transformWith({
              case Success(profiles) =>
                profileStore.bulkPersistProfiles(profiles.map(_.removeGroup(group)))
              case Failure(cause) => Future.failed(cause)
            }),
            // Delete organization relation
            organizationStore
              .getOrganizationById(
                group.relatedOrganization match {
                  case Some(org) => org.id.toString
                  case None => throw OrganizationNotFoundException("Specified group is organization orphan")
                }
              ).transformWith({
              case Success(organization) =>
                organization.removeRelatedGroup(group).persist
              case Failure(cause) => Future.failed(cause)
            })
          )
        ).transformWith({
          case Success(_) =>
            Future.successful(tx)
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(GroupNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
    }
  }

  /** A method that deletes several groups
    *
    * @param groups groups to be deleted
    * @param ec     the '''implicit''' [[ExecutionContext ExecutionContext]] used to parallelize computing
    * @return a future confirmation of state change
    */
  def bulkDeleteGroups(groups: List[Group])(implicit ec: ExecutionContext): Future[Unit] = {
    val transaction = makeTransaction
    transaction match {
      case Success(tx) =>
        val relationCache: IgniteCache[String, GroupPermission] =
          wrapper.getCache[String, GroupPermission](cache)
        val profileStore = new ProfileStore()
        val organizationStore = new OrganizationStore()
        Future.sequence(
          List(
            Utils.igniteToScalaFuture(igniteCache.removeAllAsync(groups.map(_.id).toSet.asJava)),
            // Delete relations
            // Delete permission relations
            Utils.igniteToScalaFuture(relationCache.removeAllAsync(groups.flatMap(g =>
              g.permissions
                .map({ p =>
                  s"${g.id}:${p._2.id}"
                })
            ).toSet[String].asJava)),
          ) ++ {
            // Delete member (Profile) relations
            groups.map(g =>
              profileStore
                .getProfiles(
                  ProfileStore.GetProfilesFilters().copy(filters = List(
                    ProfileStore.GetProfilesFilter().copy(id = g.members.map(_._2.id.toString))
                  ))
                ).transformWith({
                case Success(profiles) => profileStore.bulkPersistProfiles(profiles.map(_.removeGroup(g)))
                case Failure(cause) => Future.failed(cause)
              })
            )
          } ++ {
            // Delete organization relations
            List(
              Future.sequence(groups.foldLeft(List[Future[Organization]]())((acc, g) =>
                g.relatedOrganization match {
                  case Some(organization) =>
                    organizationStore
                      .getOrganizations(
                        OrganizationStore.GetOrganizationsFilters().copy(filters = List(
                          OrganizationStore.GetOrganizationsFilter().copy(id = List(organization.id.toString))
                        ))
                      ).transformWith({
                      case Success(organizations) =>
                        Future(organizations.head.removeRelatedGroup(g))
                      case Failure(cause) => Future.failed(cause)
                    }) :: acc
                  case None => acc
                }
              )).transformWith({
                case Success(organizations) => organizationStore.bulkPersistOrganizations(organizations)
                case Failure(cause) => Future.failed(cause)
              })
            )
          }
        ).transformWith({
          case Success(_) =>
            commitTransaction(tx).transformWith({
              case Success(_) => Future.unit
              case Failure(cause) => Future.failed(GroupNotPersistedException(cause))
            })
          case Failure(cause) =>
            rollbackTransaction(tx)
            Future.failed(GroupNotPersistedException(cause))
        })
      case Failure(cause) => Future.failed(cause)
    }
  }
}

object GroupStore {
  case class GetGroupsFilter(
                              id: List[String] = List(),
                              name: List[String] = List(),
                              createdAt: Option[(String, Timestamp)] = None, // (date, (eq, lt, gt, ne))
                              updatedAt: Option[(String, Timestamp)] = None // (date, (eq, lt, gt, ne))
                            )

  case class GetGroupsFilters(
                               filters: List[GetGroupsFilter] = List(),
                               orderBy: List[(String, Int)] = List() // (column, direction)
                             ) extends GetEntityFilters
}
