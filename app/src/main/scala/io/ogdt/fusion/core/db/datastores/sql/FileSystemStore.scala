package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.FileSystem
import io.ogdt.fusion.core.db.models.sql.relations.FilesystemOrganization
import io.ogdt.fusion.core.db.datastores.typed.SqlMutableStore
import io.ogdt.fusion.core.db.datastores.typed.sql.GetEntityFilters
import io.ogdt.fusion.core.db.wrappers.ignite.IgniteClientNodeWrapper

import org.apache.ignite.IgniteCache

import scala.util.Success
import scala.util.Failure

import scala.concurrent.Future
import java.sql.Timestamp
import java.util.UUID
import scala.jdk.CollectionConverters._
import io.ogdt.fusion.core.db.common.Utils
import io.ogdt.fusion.core.db.datastores.typed.sql.SqlStoreQuery
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import org.apache.ignite.cache.QueryEntity
import org.apache.ignite.cache.CacheMode
import java.util.Collection

class FileSystemStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlMutableStore[UUID, FileSystem] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_FILESYSTEM"
    override protected var igniteCache: IgniteCache[UUID, FileSystem] = wrapper.cacheExists(cache) match {
        case true => wrapper.getCache[UUID, FileSystem](cache)
        case false => {
            wrapper.createCache[UUID, FileSystem](
                wrapper.makeCacheConfig[UUID, FileSystem]
                .setCacheMode(CacheMode.REPLICATED)
                .setDataRegionName("Fusion")
                .setQueryEntities(
                    List(
                        new QueryEntity(classOf[UUID], classOf[FilesystemOrganization])
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
        var queryString: String = 
            "SELECT FS.id, rootdir_id, FS.label, shared, FS.created_at, FS.updated_at, " +
            "ORG.id, ORG.label, type, queryable, ORG.created_at, ORG.updated_at " +
            s"FROM $schema.FILESYSTEM AS FS " +
            s"LEFT OUTER JOIN $schema.FILESYSTEM_ORGANIZATION as FS_ORG ON FS_ORG.filesystem_id = FS.id " +
            s"LEFT OUTER JOIN $schema.ORGANIZATION AS ORG ON FS_ORG.organization_id = ORG.id"
        var queryArgs: ListBuffer[String] = ListBuffer()
        var whereStatements: ListBuffer[String] = ListBuffer()
        queryFilters.filters.foreach({ filter =>
            var innerWhereStatement: ListBuffer[String] = ListBuffer()
            // manage ids search
            if (filter.id.length > 0) {
                innerWhereStatement += s"FS.id in (${(for (i <- 1 to filter.id.length) yield "?").mkString(",")})"
                queryArgs ++= filter.id
            }
            // manage rootdirIds search
            if (filter.rootdirId.length > 0) {
                innerWhereStatement += s"FS.rootdir_id in (${(for (i <- 1 to filter.rootdirId.length) yield "?").mkString(",")})"
                queryArgs ++= filter.rootdirId
            }
            // manage labels search
            if (filter.label.length > 0) {
                innerWhereStatement += s"FS.label in (${(for (i <- 1 to filter.label.length) yield "?").mkString(",")})"
                queryArgs ++= filter.label
            }
            // manage shared state search
            filter.shared match {
                case Some(value) => {
                    innerWhereStatement += s"FS.shared = ?"
                    queryArgs += value.toString
                }
                case None => ()
            }
            // manage metadate search
            filter.createdAt match {
                case Some((test, time)) => {
                    innerWhereStatement += s"FS.created_at ${
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
                    innerWhereStatement += s"FS.updated_at ${
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
        if (whereStatements.length > 0) {
            queryString += " WHERE " + whereStatements.reverse.mkString(" OR ")
        }
        // manage order
        if (queryFilters.orderBy.length > 0) {
            queryString += s" ORDER BY ${queryFilters.orderBy.map( o =>
                s"FS.${o._1} ${o._2 match {
                    case 1 => "ASC"
                    case -1 => "DESC"
                }}"
            ).mkString(", ")}"
        }
        println(queryArgs)
        makeQuery(queryString)
        .setParams(queryArgs.toList)
    }

    // Get existing fileSystems from database
    def getFileSystems(queryFilters: FileSystemStore.GetFileSystemsFilters)(implicit ec: ExecutionContext): Future[List[FileSystem]] = {
        executeQuery(makeFileSystemsQuery(queryFilters)).transformWith({
            case Success(fileSystemResults) => {
                // map each fs from queryResult by grouping results by FS.id and mapping to fs objects creation
                var fileSystems = fileSystemResults.toList.groupBy(_(0)).map(entityReflection => {
                    (for (
                        // Start a for comprehension
                        fileSystem <- Right(makeFileSystem
                            .setId(entityReflection._2(0)(0).toString)
                            .setRootdirId(entityReflection._2(0)(1).toString)
                            .setLabel(entityReflection._2(0)(2).toString)
                            .setCreatedAt(entityReflection._2(0)(4) match {
                                case createdAt: Timestamp => createdAt
                                case _ => null
                            })
                            .setUpdatedAt(entityReflection._2(0)(5) match {
                                case updatedAt: Timestamp => updatedAt
                                case _ => null
                            })
                        ) flatMap { fileSystem =>
                            entityReflection._2(0)(3) match {
                                case shared: Boolean => {
                                    if (shared) Right(fileSystem.setShared)
                                    else Right(fileSystem.setUnshared)
                                }
                                case _ => Right(fileSystem)
                            }
                        } flatMap { fileSystem => // pass created filesystem to organization mapping step
                            // filter entities to exclude results where compulsory fields are missing or set to NULL
                            entityReflection._2.filter(
                                (row: List[_]) => (row(6) != null && row(7) != null && row(8) != null && row(9) != null && row(10) != null && row(11) != null)
                            ).collect(row => { // collect inner List to transform it to organizations objects
                                ( for(
                                    organization <- Right(
                                        new OrganizationStore().makeOrganization
                                        .setId(row(6).toString)
                                        .setLabel(row(7).toString)
                                        // .setType(row(8).toString)
                                        .setCreatedAt(row(10) match {
                                            case createdAt: Timestamp => createdAt
                                            case _ => null
                                        })
                                        .setUpdatedAt(row(11) match {
                                            case updatedAt: Timestamp => updatedAt
                                            case _ => null
                                        })
                                    ) flatMap { organization =>
                                        row(9) match {
                                            case queryable: Boolean => {
                                                if (queryable) Right(organization.setQueryable)
                                                else Right(organization.setUnqueryable)
                                            }
                                            case _ => Right(organization)
                                        }
                                    }
                                ) yield organization)
                                .getOrElse(null)
                            }).foreach(organization => {
                                // Add each valid organizations to fs
                                fileSystem.addOrganization(organization)
                            })
                            Right(fileSystem)
                        }
                    ) yield fileSystem).getOrElse(null)                   
                })
                Future.successful(fileSystems.toList)
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    def getFileSystemById(id: String)(implicit ec: ExecutionContext): Future[FileSystem] = {
        getFileSystems(
            FileSystemStore.GetFileSystemsFilters(
                List(
                    FileSystemStore.GetFileSystemsFilter(
                        List(id),
                        List(),
                        List(),
                        None,
                        None,
                        None
                    )
                ),
                List()
            )
        ).transformWith({
            case Success(fileSystems) => 
                fileSystems.length match {
                    case 0 => Future.failed(new Error(s"User ${id} couldn't be found")) // TODO : changer pour une custom
                    case 1 => Future.successful(fileSystems(0))
                    case _ => Future.failed(new Error(s"Duplicate id issue in FileSystemStore")) // TODO : changer pour une custom
                }
            case Failure(cause) => Future.failed(new Exception("bla bla bla", cause)) // TODO : changer pour une custom
        })
    }

    // Save user object's modification to database
    def persistFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Future[Unit] = {
        if (fileSystem.organizations.length == 0)
            return Future.failed(new Error("FileSystem must be mounted on at least one organization before being persisted"))
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            fileSystem.id, fileSystem
        )).transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A result of bulkPersistFileSystems method
      * 
      * @constructor create a new BulkPersistFileSystemsResult with a count of inserted FileSystems and a list of errors
      * @param inserts a count of the effectively inserted FileSystems
      * @param errors a list of errors catched from a file deletion
      */
    case class BulkPersistFileSystemsResult(inserts: Int, errors: List[String])

    // Save several object's modifications
    def bulkPersistFileSystems(fileSystems: List[FileSystem])(implicit ec: ExecutionContext): Future[BulkPersistFileSystemsResult] = {
        Utils.igniteToScalaFuture(igniteCache.putAllAsync(
            (fileSystems.map(_.id) zip fileSystems).toMap[UUID, FileSystem].asJava
        )).transformWith({
            case Success(value) => {
                Future.sequence(
                    fileSystems.map(fileSystem => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(fileSystem.id)))
                ).map(lookup => (fileSystems zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkPersistFileSystemsResult(
                        lookup.get.filter(_._2 == true).length,
                        lookup.get.filter(_._2 == false).map("Insert fileSystem "+_._1.toString+" failed")
                    ))
                })
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    // Delete fileSystem from database
    def deleteFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Future[Unit] = {
        if (!fileSystem.organizations.isEmpty) return Future.failed(new Error("fileSystem is still attached to some organization and can't be deleted"))
        Utils.igniteToScalaFuture(igniteCache.removeAsync(fileSystem.id))
        .transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    /** A result of bulkDeleteFileSystems method
      * 
      * @constructor create a new BulkDeleteFileSystemsResult with a count of deleted FileSystems and a list of errors
      * @param inserts a count of the effectively deleted FileSystems
      * @param errors a list of errors catched from a fileSystem deletion
      */
    case class BulkDeleteFileSystemsResult(inserts: Int, errors: List[String])

    // Delete several users from database
    def bulkDeleteFileSystems(fileSystems: List[FileSystem])(implicit ec: ExecutionContext): Future[BulkDeleteFileSystemsResult] = {
        Utils.igniteToScalaFuture(igniteCache.removeAllAsync(
            fileSystems.filter(_.organizations.isEmpty).map(_.id).toSet.asJava)
        )
        .transformWith({
            case Success(value) => {
                Future.sequence(
                    fileSystems.map(fileSystem => Utils.igniteToScalaFuture(igniteCache.containsKeyAsync(fileSystem.id)))
                ).map(lookup => (fileSystems zip lookup))
                .transformWith(lookup => {
                    Future.successful(BulkDeleteFileSystemsResult(
                        lookup.get.filter(_._2 == false).length,
                        lookup.get.filter(_._2 == true).map("Failed to delete fileSystem "+_._1.toString)
                    ))
                })
            }
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }
}

object FileSystemStore {
    case class GetFileSystemsFilter(
        id: List[String],
        rootdirId: List[String],
        label: List[String],
        shared: Option[Boolean],
        createdAt: Option[(String, Timestamp)],
        updatedAt: Option[(String, Timestamp)]
    )
    case class GetFileSystemsFilters(
        filters: List[GetFileSystemsFilter],
        orderBy: List[(String, Int)]
    ) extends GetEntityFilters
}