package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters

import io.ogdt.fusion.core.db.common.Utils

import io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems.{
    FilesystemNotPersistedException
}

import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.cache.CacheAtomicityMode

import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import scala.jdk.CollectionConverters._

import io.ogdt.fusion.core.db.models.sql.FileSystem
import io.ogdt.fusion.core.db.models.sql.relations.FilesystemOrganization
import java.util.UUID
import java.sql.Timestamp

import scala.collection.mutable.ListBuffer
import java.time.Instant
import scala.util.Try
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.datastores.sql.exceptions.NoEntryException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.filesystems.{
    DuplicateFilesystemException,
    FilesystemNotPersistedException,
    FilesystemNotFoundException,
    FilesystemQueryExecutionException
}
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.OrganizationNotFoundException
import io.ogdt.fusion.core.db.datastores.sql.exceptions.organizations.DuplicateOrganizationException

class FileSystemStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, FileSystem] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_FILESYSTEM"
    override protected var igniteCache: IgniteCache[UUID, FileSystem] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, FileSystem](cache)
        case false => {
            wrapper.createCache[UUID, FileSystem](
                wrapper.makeCacheConfig[UUID, FileSystem]
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
    }

    // Create and get new FileSystem Object
    def makeFileSystem: FileSystem = {
        implicit val fileSystemStore: FileSystemStore = this
        new FileSystem
    }

    def makeFileSystemsQuery(queryFilters: FileSystemStore.GetFileSystemsFilters): SqlStoreQuery = {
        var baseQueryString = queryString.replace("$schema", schema)
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (!filter.id.isEmpty) {
                innerWhereStatement += s"fs_id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage rootdirIds search
            if (!filter.rootdirId.isEmpty) {
                innerWhereStatement += s"fs_rootdir_id in (${(for (i <- 1 to filter.rootdirId.length) yield "?").mkString(",")})"
                queryArgs ++= filter.rootdirId
            }
            // manage labels search
            if (!filter.label.isEmpty) {
                innerWhereStatement += s"fs_label in (${(for (i <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage shared state search
            filter.shared match {
                case Some(value) => {
                    innerWhereStatement += s"fs_shared = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"fs_created_at ${
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
                    innerWhereStatement += s"fs_updated_at ${
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
                s"fs_${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        makeQuery(baseQueryString)
        .setParams(queryArgs.toList)
    }

    // Get existing fileSystems from database
    def getFileSystems(queryFilters: FileSystemStore.GetFileSystemsFilters)(implicit ec: ExecutionContext): Future[List[FileSystem]] = {
        executeQuery(makeFileSystemsQuery(queryFilters)).transformWith({
            case Success(rows) => {
                // map each fs from queryResult by grouping results by FS.id and mapping to fs objects creation
                val entityReflections = rows.groupBy(_(0))
                val fileSystems = rows.map(_(0)).distinct.map(entityReflections.get(_).get).map(entityReflection => {
                    // transform plain relation rows to iterables grouped by type and related entity id
                    val groupedRows = getRelationsGroupedRowsFrom(entityReflection, 6, 7)
                    groupedRows.get("FS") match {
                        case Some(filesystemReflections) => {
                            val fsDef = filesystemReflections.head.head._2
                            (for (
                                fileSystem <- Right(makeFileSystem
                                    .setId(fsDef(0).toString)
                                    .setRootdirId(fsDef(1).toString)
                                    .setLabel(fsDef(2).toString)
                                    .setCreatedAt(Utils.timestampFromString(fsDef(4)) match {
                                        case createdAt: Timestamp => createdAt
                                        case _ => null
                                    })
                                    .setUpdatedAt(Utils.timestampFromString(fsDef(5)) match {
                                        case updatedAt: Timestamp => updatedAt
                                        case _ => null
                                    })
                                ) flatMap { fileSystem =>
                                    Try(fsDef(3).toBoolean) match {
                                        case Success(shared) => {
                                            if (shared) Right(fileSystem.setShared)
                                            else Right(fileSystem.setUnshared)
                                        }
                                        case Failure(_) => Right(fileSystem)
                                    }
                                } flatMap { fileSystem => // pass created filesystem to relation mapping step
                                    // organization mapping
                                    groupedRows.get("ORGANIZATION") match {
                                        case Some(organizationReflections) => {
                                            organizationReflections.foreach({ organizationReflection =>
                                                organizationReflection.partition(_._1 == "ORGANIZATION") match {
                                                    case result => {
                                                        result._1.length match {
                                                            case 0 => Future.failed(OrganizationNotFoundException(s"Filesystem ${fileSystem.id}:${fileSystem.label} might be orphan"))
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
                                                                                        (for (
                                                                                            orgType <- Right(new OrganizationTypeStore()
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
                                                                                            ) flatMap { orgType =>
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
                                                                                                Right(orgType)
                                                                                            }
                                                                                        ) yield organization.setType(orgType))
                                                                                    }
                                                                                    case _ => 
                                                                                }
                                                                            }
                                                                        }
                                                                        Right(organization)
                                                                    }
                                                                ) yield fileSystem.addOrganization(organization))
                                                            }
                                                            case _ => Future.failed(DuplicateOrganizationException(s"Filesystem ${fileSystem.id}:${fileSystem.label} has duplicate organization relation"))
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                        case None => None
                                    }
                                    Right(fileSystem)
                                }
                            ) yield fileSystem).getOrElse(null)
                        }
                        case None => {}
                    }                  
                })
                Future.successful(fileSystems.toList.asInstanceOf[List[FileSystem]])
            }
            case Failure(cause) => Future.failed(FilesystemQueryExecutionException(cause))
        })
    }

    def getAllFileSystems(implicit ec: ExecutionContext): Future[List[FileSystem]] = {
        getFileSystems(FileSystemStore.GetFileSystemsFilters().copy(
            orderBy = List(
                ("id", 1)
            )
        )).transformWith({
            case Success(fileSystems) => 
                fileSystems.length match {
                    case 0 => Future.failed(new NoEntryException("FileSystem store is empty"))
                    case _ => Future.successful(fileSystems)
                }
            case Failure(cause) => throw cause
        })
    }

    def getFileSystemById(id: String)(implicit ec: ExecutionContext): Future[FileSystem] = {
        getFileSystems(
            FileSystemStore.GetFileSystemsFilters().copy(
                filters = List(
                    FileSystemStore.GetFileSystemsFilter().copy(
                        id = List(id)
                    )
                )
            )
        ).transformWith({
            case Success(fileSystems) => 
                fileSystems.length match {
                    case 0 => Future.failed(new FilesystemNotFoundException(s"FileSystem ${id} couldn't be found"))
                    case 1 => Future.successful(fileSystems.head)
                    case _ => Future.failed(new DuplicateFilesystemException)
                }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Save user object's modification to database
    def persistFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Future[Unit] = {
        if (fileSystem.organizations.filter(_._1 == true).isEmpty)
            return Future.failed(FilesystemNotPersistedException("FileSystem must be mounted on at least one organization before being persisted"))
        else {
            val transaction = makeTransaction
            transaction match {
                case Success(tx) => {
                    val relationCache: IgniteCache[String, FilesystemOrganization] =
                        wrapper.getCache[String, FilesystemOrganization](cache)
                    Future.sequence(
                        List(
                            // Save orgs
                            Utils.igniteToScalaFuture(relationCache.putAllAsync(
                                (fileSystem.organizations
                                .filter(_._1 == true)
                                .map({ org =>
                                    (
                                        fileSystem.id +":"+org._2._2.id,
                                        FilesystemOrganization(
                                            fileSystem.id,
                                            org._2._2.id,
                                            org._1
                                        )
                                    )
                                })).toMap[String, FilesystemOrganization].asJava
                            )),
                            // Remove orgs
                            Utils.igniteToScalaFuture(relationCache.removeAllAsync(
                                (fileSystem.organizations
                                .filter(_._1 == false)
                                .map(fileSystem.id +":"+_._2._2.id)).toSet[String].asJava
                            )),
                            // Save entity
                            Utils.igniteToScalaFuture(igniteCache.putAsync(
                                fileSystem.id, fileSystem
                            ))
                        )
                    ).transformWith({
                        case Success(value) => {
                            commitTransaction(transaction).transformWith({
                                case Success(value) => Future.unit
                                case Failure(cause) => Future.failed(FilesystemNotPersistedException(cause))
                            })
                        }
                        case Failure(cause) => {
                            rollbackTransaction(transaction)
                            Future.failed(FilesystemNotPersistedException(cause))
                        }
                    })
                }
                case Failure(cause) => Future.failed(FilesystemNotPersistedException(cause))
            }
        }
    }

    // Save several object's modifications
    def bulkPersistFileSystems(fileSystems: List[FileSystem])(implicit ec: ExecutionContext): Future[Unit] = {
        fileSystems.find({ fs => !fs.organizations.isEmpty }) match {
            case Some(found) => Future.failed(FilesystemNotPersistedException("FileSystem must be mounted on at least one organization before being persisted"))
            case None => {
                val transaction = makeTransaction
                transaction match {
                    case Success(tx) => {
                        val relationCache: IgniteCache[String, FilesystemOrganization] =
                            wrapper.getCache[String, FilesystemOrganization](cache)
                        Future.sequence(
                            List(
                                Utils.igniteToScalaFuture(igniteCache.putAllAsync(
                                    (fileSystems.map(_.id) zip fileSystems).toMap[UUID, FileSystem].asJava
                                )),
                                Utils.igniteToScalaFuture(relationCache.putAllAsync(
                                    (fileSystems.map(fs => fs.organizations.filter(_._1 == true).map(fs.id +":"+_._2._2.id) zip fs.organizations.map({ org =>
                                        FilesystemOrganization(
                                            fs.id,
                                            org._2._2.id,
                                            org._1
                                        )
                                    })).flatten.toMap[String, FilesystemOrganization].asJava)
                                )),
                                Utils.igniteToScalaFuture(relationCache.removeAllAsync(
                                    (fileSystems.map(fs =>
                                        fs.organizations.filter(_._1 == false).map(fs.id +":"+_._2._2.id)
                                    )).flatten.toSet[String].asJava
                                ))
                            )
                        ).transformWith({
                            case Success(value) => {
                                commitTransaction(transaction).transformWith({
                                    case Success(value) => Future.unit
                                    case Failure(cause) => Future.failed(FilesystemNotPersistedException(cause))
                                })
                            }
                            case Failure(cause) => {
                                rollbackTransaction(transaction)
                                Future.failed(FilesystemNotPersistedException(cause))
                            }
                        })
                    }
                    case Failure(cause) => Future.failed(FilesystemNotPersistedException(cause))
                }
            }
        }
    }

    // Delete fileSystem from database
    def deleteFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Future[Unit] = {
        if (!fileSystem.organizations.isEmpty) return Future.failed(FilesystemNotPersistedException("fileSystem is still attached to some organization and can't be deleted"))
        else {
            Utils.igniteToScalaFuture(igniteCache.removeAsync(fileSystem.id))
            .transformWith({
                case Success(done) => {
                    if (done) Future.unit
                    else Future.failed(FilesystemNotPersistedException())
                }
                case Failure(cause) => Future.failed(FilesystemNotPersistedException("Failed to remove fileSystem", cause))
            })
        }
    }

    // Delete several users from database
    def bulkDeleteFileSystems(fileSystems: List[FileSystem])(implicit ec: ExecutionContext): Future[Unit] = {
        fileSystems.find({ fs => !fs.organizations.isEmpty }) match {
            case Some(found) => throw FilesystemNotPersistedException("FileSystem must be unmounted off all its organization before being deleted")
            case None => {
                Utils.igniteToScalaFuture(igniteCache.removeAllAsync(
                    fileSystems.filter(_.organizations.isEmpty).map(_.id).toSet.asJava)
                ).transformWith({
                    case Success(value) => Future.unit
                    case Failure(cause) => Future.failed(FilesystemNotPersistedException(cause))
                })
            }
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
        orderBy: List[(String, Int)] = List()
    ) extends GetEntityFilters
}