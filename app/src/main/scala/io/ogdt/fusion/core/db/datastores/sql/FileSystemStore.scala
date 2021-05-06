package io.ogdt.fusion.core.db.datastores.sql

import io.ogdt.fusion.core.db.models.sql.FileSystem
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

class FileSystemStore(implicit wrapper: IgniteClientNodeWrapper) extends SqlStore[UUID, FileSystem] {

    override val schema: String = "FUSION"
    override val cache: String = s"SQL_${schema}_FILESYSTEM"
    override protected var igniteCache: IgniteCache[UUID, FileSystem] = null

    // super .init(List(
    //     makeQuery(
    //         "CREATE TABLE \"FUSION\".FILESYSTEM_ORGANIZATION ("
    //         + "`filesystem_id` VARCHAR(36) NOT NULL,"
    //         + "`organization_id` VARCHAR(36) NOT NULL,"
    //         + "`is_default` TINYINT NOT NULL DEFAULT 0,"
    //         + "PRIMARY KEY (`filesystem_id`,`organization_id`)"
    //         + ");"
    //     ),
    //     makeQuery("CREATE INDEX IX_ASSOCIATION ON \"FUSION\".FILESYSTEM_ORGANIZATION (`filesystem_id`,`organization_id`);"),
    //     makeQuery("CREATE INDEX IX_DEFAULT_FS ON \"FUSION\".FILESYSTEM_ORGANIZATION (`organization_id`,`is_default`);")
    // ))

    super .init()

    // Create and get new FileSystem Object
    def makeFileSystem: FileSystem = {
        implicit val fileSystemStore: FileSystemStore = this
        new FileSystem
    }

    // def getFileSystemById(id: String): Future[FileSystem] = {
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
    //                         makeFileSystem
    //                         .setId(row(0).toString)
    //                         .setFileSystemname(row(1).toString)
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
    // def getFileSystems(ids: List[String]): Future[List[FileSystem]] = {
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
    //                         makeFileSystem
    //                         .setId(row(0).toString)
    //                         .setFileSystemname(row(1).toString)
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
    def persistFileSystem(fileSystem: FileSystem): Future[Unit] = {
        if (fileSystem.organizations.length == 0)
            return Future.failed(new Error("FileSystem must be mounted on at least one organization before being persisted"))
        Utils.igniteToScalaFuture(igniteCache.putAsync(
            fileSystem.id, fileSystem
        )).transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
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
    def bulkPersistFileSystems(fileSystems: List[FileSystem]): Future[BulkPersistFileSystemsResult] = {
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
            case Failure(cause) => Future.failed(cause)
        })
    }

    // Delete fileSystem from database
    def deleteFileSystem(fileSystem: FileSystem): Future[Unit] = {
        if (!fileSystem.organizations.isEmpty) return Future.failed(new Error("fileSystem is still attached to some organization and can't be deleted"))
        Utils.igniteToScalaFuture(igniteCache.removeAsync(fileSystem.id))
        .transformWith({
            case Success(value) => Future.successful()
            case Failure(cause) => Future.failed(cause)
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
    def bulkDeleteFileSystems(fileSystems: List[FileSystem]): Future[BulkDeleteFileSystemsResult] = {
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
            case Failure(cause) => Future.failed(cause)
        })
    }
}