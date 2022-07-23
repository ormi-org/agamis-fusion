package io.agamis.fusion.core.db.datastores.sql

import io.agamis.fusion.core.db.common.Utils
import io.agamis.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.filesystems.{
  DuplicateFilesystemException,
  FilesystemNotFoundException,
  FilesystemNotPersistedException,
  FilesystemQueryExecutionException
}
import io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations.{
  DuplicateOrganizationException,
  OrganizationNotFoundException
}
import io.agamis.fusion.core.db.datastores.typed.SqlMutableStore
import io.agamis.fusion.core.db.datastores.typed.sql.{
  GetEntityFilters,
  SqlStoreQuery
}
import io.agamis.fusion.core.db.models.sql.{Application, FileSystem}
import io.agamis.fusion.core.db.models.sql.generics.Language
import io.agamis.fusion.core.db.models.sql.relations.FilesystemOrganization
import io.agamis.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.{CacheAtomicityMode, CacheMode, QueryEntity}

import java.sql.Timestamp
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/** A class to manage and reflects [[FileSystem FileSystem]] life-cycle in the
  * SQL database
  *
  * @param wrapper
  *   the '''implicit''' [[IgniteClientNodeWrapper IgniteClientNodeWrapper]]
  *   used to handle SQL & key-value operations
  */
class FileSystemStore(implicit wrapper: IgniteClientNodeWrapper)
    extends SqlMutableStore[UUID, FileSystem] {

  override val schema: String = "FUSION"
  override val cache: String = s"SQL_${schema}_FILESYSTEM"
  override protected val igniteCache: IgniteCache[UUID, FileSystem] =
    if (wrapper.cacheExists(cache)) {
      wrapper.getCache[UUID, FileSystem](cache)
    } else {
      wrapper.createCache[UUID, FileSystem](
        wrapper
          .makeCacheConfig[UUID, FileSystem]
          .setCacheMode(CacheMode.REPLICATED)
          .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
          .setDataRegionName("Fusion")
          .setQueryEntities(
            List(
              new QueryEntity(classOf[String], classOf[FilesystemOrganization])
                .setTableName("FILESYSTEM_ORGANIZATION")
            ).asJava
          )
          .setName(cache)
          .setSqlSchema(schema)
          .setIndexedTypes(classOf[UUID], classOf[FileSystem])
      )
    }

  /** A factory method that generates a new fileSystem object
    *
    * [[FileSystem FileSystem]] is generated along with its '''implicit'''
    * [[ApplicationStore ApplicationStore]]
    *
    * @return
    *   a simple [[FileSystem FileSystem]]
    */
  def makeFileSystem: FileSystem = {
    implicit val fileSystemStore: FileSystemStore = this
    new FileSystem
  }

  /** A factory method that generates an SQL query based on provided filters
    *
    * @param queryFilters
    *   the filters used to populate the query
    * @return
    *   a simple [[SqlStoreQuery SqlStoreQuery]]
    */
  def makeFileSystemsQuery(
      queryFilters: FileSystemStore.GetFileSystemsFilters
  ): SqlStoreQuery = {
    var baseQueryString = queryString.replace("$schema", schema)
    val queryArgs: ListBuffer[String] = ListBuffer()
    val whereStatements: ListBuffer[String] = ListBuffer()
    queryFilters.filters.foreach({ filter =>
      val innerWhereStatement: ListBuffer[String] = ListBuffer()
      // manage ids search
      if (filter.id.nonEmpty) {
        innerWhereStatement += s"fs_id in (${(for (_ <- 1 to filter.id.length)
          yield "?").mkString(",")})"
        queryArgs ++= filter.id
      }
      // manage rootdirIds search
      if (filter.rootdirId.nonEmpty) {
        innerWhereStatement += s"fs_rootdir_id in (${(for (_ <- 1 to filter.rootdirId.length)
          yield "?").mkString(",")})"
        queryArgs ++= filter.rootdirId
      }
      // manage labels search
      if (filter.label.nonEmpty) {
        innerWhereStatement += s"fs_label in (${(for (_ <- 1 to filter.label.length)
          yield "?").mkString(",")})"
        queryArgs ++= filter.label
      }
      // manage shared state search
      filter.shared match {
        case Some(value) =>
          innerWhereStatement += s"fs_shared = ?"
          queryArgs += value.toString
        case None => ()
      }
      // manage metadate search
      filter.createdAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"fs_created_at ${test match {
            case "eq"  => "="
            case "gt"  => ">"
            case "lt"  => "<"
            case "neq" => "<>"
          }} ?"
          queryArgs += time.toString
        case None => ()
      }
      filter.updatedAt match {
        case Some((test, time)) =>
          innerWhereStatement += s"fs_updated_at ${test match {
            case "eq"  => "="
            case "gt"  => ">"
            case "lt"  => "<"
            case "neq" => "<>"
          }} ?"
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
      baseQueryString += s" ORDER BY ${queryFilters.orderBy
        .map(o =>
          s"fs_${o._1} ${o._2 match {
            case 1  => "ASC"
            case -1 => "DESC"
          }}"
        )
        .mkString(", ")}"
    }
    makeQuery(baseQueryString)
      .setParams(queryArgs.toList)
  }

  /** A method that gets several existing fileSystems from database based on
    * provided filters
    *
    * @note
    *   used as a generic methods wich parse result in Object sets to process
    *   it; it is used in regular SELECT based methods
    * @param queryFilters
    *   the filters used to populate the query
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future [[List List]] of [[FileSystem FileSystem]]
    */
  def getFileSystems(
      queryFilters: FileSystemStore.GetFileSystemsFilters
  )(implicit ec: ExecutionContext): Future[List[FileSystem]] = {
    executeQuery(makeFileSystemsQuery(queryFilters)).transformWith({
      case Success(rows) =>
        // map each fs from queryResult by grouping results by FS.id and mapping to fs objects creation
        val entityReflections = rows.groupBy(_.head)
        val fileSystems = rows
          .map(_.head)
          .distinct
          .map(entityReflections(_))
          .map(entityReflection => {
            // transform plain relation rows to iterables grouped by type and related entity id
            val groupedRows =
              getRelationsGroupedRowsFrom(entityReflection, 6, 7)
            groupedRows.get("FS") match {
              case Some(filesystemReflections) =>
                val fsDef = filesystemReflections.head.head._2
                (for (
                  fileSystem <- Right(
                    makeFileSystem
                      .setId(fsDef(0))
                      .setRootdirId(fsDef(1))
                      .setLabel(fsDef(2))
                      .setCreatedAt(Utils.timestampFromString(fsDef(4)) match {
                        case createdAt: Timestamp => createdAt
                        case _                    => null
                      })
                      .setUpdatedAt(Utils.timestampFromString(fsDef(5)) match {
                        case updatedAt: Timestamp => updatedAt
                        case _                    => null
                      })
                  ) flatMap { fileSystem =>
                    Try(fsDef(3).toBoolean) match {
                      case Success(shared) =>
                        if (shared) Right(fileSystem.setShared())
                        else Right(fileSystem.setUnshared())
                      case Failure(_) => Right(fileSystem)
                    }
                  } flatMap { fileSystem =>
                    groupedRows.get("APPLICATION") match {
                      case Some(applicationReflections) =>
                        applicationReflections.foreach({
                            applicationReflection =>
                              applicationReflection
                                .partition(_._1 == "APPLICATION") match {
                                case result =>
                                  val appDef = result._1(0)._2
                                  for (
                                    application <- Right(
                                      new ApplicationStore().makeApplication
                                        .setId(appDef(0))
                                        .setLabel(appDef(1))
                                        .setVersion(appDef(2))
                                        .setAppUniversalId(appDef(3))
                                        .setManifestUrl(appDef(5))
                                        .setStoreUrl(appDef(6))
                                        .setCreatedAt(
                                          Utils.timestampFromString(
                                            appDef(7)
                                          ) match {
                                            case createdAt: Timestamp =>
                                              createdAt
                                            case _ => null
                                          }
                                        )
                                        .setUpdatedAt(
                                          Utils.timestampFromString(
                                            appDef(8)
                                          ) match {
                                            case updatedAt: Timestamp =>
                                              updatedAt
                                            case _ => null
                                          }
                                        )
                                    ) flatMap { application =>
                                      Application.Status
                                        .fromInt(appDef(4).toInt) match {
                                        case Success(status) =>
                                          Right(application.setStatus(status))
                                        case Failure(_) => Right(application)
                                      }
                                    }
                                  )
                                    yield fileSystem
                                      .addLicensedApplication(application)
                              }
                          }
                        )
                      case None =>
                    }
                    Right(fileSystem)
                  } flatMap { fileSystem => // pass created filesystem to relation mapping step
                    // organization mapping
                    groupedRows.get("ORGANIZATION") match {
                      case Some(organizationReflections) =>
                        organizationReflections.foreach({ organizationReflection =>
                          organizationReflection
                            .partition(_._1 == "ORGANIZATION") match {
                            case result =>
                              result._1.length match {
                                case 0 =>
                                  Future.failed(
                                    OrganizationNotFoundException(
                                      s"Filesystem ${fileSystem.id}:${fileSystem.label} might be orphan"
                                    )
                                  )
                                case 1 =>
                                  val orgDef = result._1(0)._2
                                  for (
                                    organization <- Right(
                                      new OrganizationStore().makeOrganization
                                        .setId(orgDef(0))
                                        .setLabel(orgDef(1))
                                        .setCreatedAt(
                                          Utils.timestampFromString(
                                            orgDef(3)
                                          ) match {
                                            case createdAt: Timestamp =>
                                              createdAt
                                            case _ => null
                                          }
                                        )
                                        .setUpdatedAt(
                                          Utils.timestampFromString(
                                            orgDef(4)
                                          ) match {
                                            case updatedAt: Timestamp =>
                                              updatedAt
                                            case _ => null
                                          }
                                        )
                                    ) flatMap { organization =>
                                      Try(orgDef(2).toBoolean) match {
                                        case Success(queryable) =>
                                          if (queryable)
                                            Right(organization.setQueryable())
                                          else
                                            Right(organization.setUnqueryable())
                                        case Failure(_) => Right(organization)
                                      }
                                    } flatMap { organization =>
                                      result._2
                                        .partition(_._1 == "ORGTYPE") match {
                                        case result =>
                                          result._1.length match {
                                            case 0 =>
                                            case 1 =>
                                              val orgTypeDef = result._1(0)._2
                                              for (
                                                orgType <- Right(
                                                  new OrganizationTypeStore().makeOrganizationType
                                                    .setId(orgTypeDef(1))
                                                    .setLabelTextId(
                                                      orgTypeDef(2)
                                                    )
                                                    .setCreatedAt(
                                                      Utils.timestampFromString(
                                                        orgTypeDef(3)
                                                      ) match {
                                                        case createdAt: Timestamp =>
                                                          createdAt
                                                        case _ => null
                                                      }
                                                    )
                                                    .setUpdatedAt(
                                                      Utils.timestampFromString(
                                                        orgTypeDef(4)
                                                      ) match {
                                                        case updatedAt: Timestamp =>
                                                          updatedAt
                                                        case _ => null
                                                      }
                                                    )
                                                ) flatMap { orgType =>
                                                  result._2.foreach({ result =>
                                                    val orgTypeLangVariantDef =
                                                      result._2
                                                    orgType.setLabel(
                                                      Language.apply
                                                        .setId(
                                                          orgTypeLangVariantDef(
                                                            4
                                                          )
                                                        )
                                                        .setCode(
                                                          orgTypeLangVariantDef(
                                                            3
                                                          )
                                                        )
                                                        .setLabel(
                                                          orgTypeLangVariantDef(
                                                            5
                                                          )
                                                        ),
                                                      orgTypeLangVariantDef(2)
                                                    )
                                                  })
                                                  Right(orgType)
                                                }
                                              )
                                                yield organization
                                                  .setType(orgType)
                                            case _ =>
                                          }
                                      }
                                      Right(organization)
                                    }
                                  )
                                    yield fileSystem
                                      .addOrganization(organization)
                                case _ =>
                                  Future.failed(
                                    DuplicateOrganizationException(
                                      s"Filesystem ${fileSystem.id}:${fileSystem.label} has duplicate organization relation"
                                    )
                                  )
                              }
                          }
                        })
                      case None =>
                    }
                    Right(fileSystem)
                  }
                ) yield fileSystem).getOrElse(null)
              case None =>
            }
          })
        Future.successful(fileSystems.toList.asInstanceOf[List[FileSystem]])
      case Failure(cause) =>
        Future.failed(FilesystemQueryExecutionException(cause))
    })
  }

  /** A method that gets all existing fileSystems
    *
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future [[List List]] of [[FileSystem FileSystem]]
    */
  def getAllFileSystems(implicit
      ec: ExecutionContext
  ): Future[List[FileSystem]] = {
    getFileSystems(
      FileSystemStore
        .GetFileSystemsFilters()
        .copy(
          orderBy = List(
            (FileSystemStore.Column.ID(), 1)
          )
        )
    ).transformWith({
      case Success(fileSystems) =>
        fileSystems.length match {
          case 0 => Future.failed(NoEntryException("FileSystem store is empty"))
          case _ => Future.successful(fileSystems)
        }
      case Failure(cause) => throw cause
    })
  }

  /** A method that fetches fileSystem by its id
    *
    * @param id
    *   the id of the fileSystem to be fetched
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future [[FileSystem FileSystem]] which reflects application state
    *   fetched from database
    */
  def getFileSystemById(
      id: String
  )(implicit ec: ExecutionContext): Future[FileSystem] = {
    getFileSystems(
      FileSystemStore
        .GetFileSystemsFilters()
        .copy(
          filters = List(
            FileSystemStore
              .GetFileSystemsFilter()
              .copy(
                id = List(id)
              )
          )
        )
    ).transformWith({
      case Success(fileSystems) =>
        fileSystems.length match {
          case 0 =>
            Future.failed(
              FilesystemNotFoundException(s"FileSystem $id couldn't be found")
            )
          case 1 => Future.successful(fileSystems.head)
          case _ => Future.failed(new DuplicateFilesystemException)
        }
      case Failure(cause) => Future.failed(cause)
    })
  }

  /** A method that persist fileSystem state in the database
    *
    * @param fileSystem
    *   the fileSystem object to persist
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future confirmation of the state change
    */
  def persistFileSystem(
      fileSystem: FileSystem
  )(implicit ec: ExecutionContext): Future[Unit] = {
    if (!fileSystem.organizations.exists(_._1 == true))
      Future.failed(
        FilesystemNotPersistedException(
          "FileSystem must be mounted on at least one organization before being persisted"
        )
      )
    else {
      makeTransaction match {
        case Success(tx) =>
          val relationCache: IgniteCache[String, FilesystemOrganization] =
            wrapper.getCache[String, FilesystemOrganization](cache)
          Future
            .sequence(
              List(
                // Save orgs
                Utils.igniteToScalaFuture(
                  relationCache.putAllAsync(
                    fileSystem.organizations
                      .filter(_._1 == true)
                      .map({ org =>
                        (
                          s"${fileSystem.id}:${org._2._2.id}",
                          FilesystemOrganization(
                            fileSystem.id,
                            org._2._2.id,
                            org._1
                          )
                        )
                      })
                      .toMap[String, FilesystemOrganization]
                      .asJava
                  )
                ),
                // Remove orgs
                Utils.igniteToScalaFuture(
                  relationCache.removeAllAsync(
                    fileSystem.organizations
                      .filter(_._1 == false)
                      .map(relation => s"${fileSystem.id}:${relation._2._2.id}")
                      .toSet[String]
                      .asJava
                  )
                ),
                // Save entity
                Utils.igniteToScalaFuture(
                  igniteCache.putAsync(
                    fileSystem.id,
                    fileSystem
                  )
                )
              )
            )
            .transformWith({
              case Success(_) =>
                commitTransaction(tx).transformWith({
                  case Success(_) => Future.unit
                  case Failure(cause) =>
                    Future.failed(FilesystemNotPersistedException(cause))
                })
              case Failure(cause) =>
                rollbackTransaction(tx)
                Future.failed(FilesystemNotPersistedException(cause))
            })
        case Failure(cause) =>
          Future.failed(FilesystemNotPersistedException(cause))
      }
    }
  }

  /** A method that persist several fileSystems state in the database
    *
    * @param fileSystems
    *   fileSystems objects to persist
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future confirmation of the state change
    */
  def bulkPersistFileSystems(
      fileSystems: List[FileSystem]
  )(implicit ec: ExecutionContext): Future[Unit] = {
    fileSystems.find({ fs => fs.organizations.nonEmpty }) match {
      case Some(_) =>
        Future.failed(
          FilesystemNotPersistedException(
            "FileSystem must be mounted on at least one organization before being persisted"
          )
        )
      case None =>
        makeTransaction match {
          case Success(tx) =>
            val relationCache: IgniteCache[String, FilesystemOrganization] =
              wrapper.getCache[String, FilesystemOrganization](cache)
            Future
              .sequence(
                List(
                  Utils.igniteToScalaFuture(
                    igniteCache.putAllAsync(
                      (fileSystems.map(_.id) zip fileSystems)
                        .toMap[UUID, FileSystem]
                        .asJava
                    )
                  ),
                  Utils.igniteToScalaFuture(
                    relationCache.putAllAsync(
                      fileSystems
                        .flatMap(fs =>
                          fs.organizations
                            .filter(_._1 == true)
                            .map(relation =>
                              s"${fs.id}:${relation._2._2.id}"
                            ) zip fs.organizations.map({ org =>
                            FilesystemOrganization(
                              fs.id,
                              org._2._2.id,
                              org._1
                            )
                          })
                        )
                        .toMap[String, FilesystemOrganization]
                        .asJava
                    )
                  ),
                  Utils.igniteToScalaFuture(
                    relationCache.removeAllAsync(
                      fileSystems
                        .flatMap(fs =>
                          fs.organizations
                            .filter(_._1 == false)
                            .map(relation => s"${fs.id}:${relation._2._2.id}")
                        )
                        .toSet[String]
                        .asJava
                    )
                  )
                )
              )
              .transformWith({
                case Success(_) =>
                  commitTransaction(tx).transformWith({
                    case Success(_) => Future.unit
                    case Failure(cause) =>
                      Future.failed(FilesystemNotPersistedException(cause))
                  })
                case Failure(cause) =>
                  rollbackTransaction(tx)
                  Future.failed(FilesystemNotPersistedException(cause))
              })
          case Failure(cause) =>
            Future.failed(FilesystemNotPersistedException(cause))
        }
    }
  }

  /** A method that deletes fileSystem
    *
    * @param fileSystem
    *   the fileSystem to be deleted
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future confirmation of state change
    */
  def deleteFileSystem(
      fileSystem: FileSystem
  )(implicit ec: ExecutionContext): Future[Unit] = {
    if (fileSystem.organizations.nonEmpty)
      Future.failed(
        FilesystemNotPersistedException(
          "fileSystem is still attached to some organization and can't be deleted"
        )
      )
    else {
      Utils
        .igniteToScalaFuture(igniteCache.removeAsync(fileSystem.id))
        .transformWith({
          case Success(done) =>
            if (done) Future.unit
            else Future.failed(FilesystemNotPersistedException())
          case Failure(cause) =>
            Future.failed(
              FilesystemNotPersistedException(
                "Failed to remove fileSystem",
                cause
              )
            )
        })
    }
  }

  /** A method that deletes several fileSystems
    *
    * @param fileSystems
    *   fileSystems to be deleted
    * @param ec
    *   the '''implicit''' [[ExecutionContext ExecutionContext]] used to
    *   parallelize computing
    * @return
    *   a future confirmation of state change
    */
  def bulkDeleteFileSystems(
      fileSystems: List[FileSystem]
  )(implicit ec: ExecutionContext): Future[Unit] = {
    fileSystems.find({ fs => fs.organizations.nonEmpty }) match {
      case Some(_) =>
        throw FilesystemNotPersistedException(
          "FileSystem must be unmounted of all its organization before being deleted"
        )
      case None =>
        Utils
          .igniteToScalaFuture(
            igniteCache.removeAllAsync(fileSystems.map(_.id).toSet.asJava)
          )
          .transformWith({
            case Success(_) => Future.unit
            case Failure(cause) =>
              Future.failed(FilesystemNotPersistedException(cause))
          })
    }
  }
}

object FileSystemStore {
  case class GetFileSystemsFilter(
      id: List[String] = List(),
      rootdirId: List[String] = List(),
      label: List[String] = List(),
      shared: Option[Boolean] = None,
      createdAt: Option[(String, Timestamp)] = None,
      updatedAt: Option[(String, Timestamp)] = None
  )

  case class GetFileSystemsFilters(
      filters: List[GetFileSystemsFilter] = List(),
      orderBy: List[(GetEntityFilters.Column, Int)] = List(),
      pagination: Option[GetEntityFilters.Pagination] = None // (limit, offset)
  ) extends GetEntityFilters

  object Column {
    case class ID(val order: Int = 0, val name: String = "fs.ID") extends GetEntityFilters.Column
  }
}
