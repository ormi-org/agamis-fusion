package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.FileSystemStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import scala.util.Success
import scala.util.Failure

class FileSystem(implicit protected val store: FileSystemStore) extends Model with Serializable {

    @QuerySqlField(index = true, name = "id", notNull = true)
    protected var _id: UUID = null
    def id: UUID = _id
    // Used to set UUID (mainly for setting uuid of existing user when fetching)
    def setId(id: String): FileSystem = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(name = "rootdir_id", notNull = true)
    private var _rootdirId: String = null
    def rootdirId: String = _rootdirId
    def setRootdirId(rootdirId: String): FileSystem = {
        _rootdirId = rootdirId
        this
    }

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = null
    def label: String = _label
    def setLabel(label: String): FileSystem = {
        _label = label
        this
    }

    @QuerySqlField(name = "shared", notNull = true)
    private var _shared: Boolean = false
    def shared: Boolean = _shared
    def setShared: FileSystem = {
        _shared = true
        this
    }
    def setUnshared: FileSystem = {
        _shared = false
        this
    }

    @QuerySqlField(name = "queryable", notNull = true)
    private var _queryable: Boolean = false
    def queryable: Boolean = _queryable
    def setQueryable(queryable: Boolean): FileSystem = {
        _queryable = queryable
        this
    }

    private var _organizations: List[Organization] = List()
    def organizations: List[Organization] = _organizations
    def addOrganization(organization: Organization): FileSystem = {
        _organizations ::= organization
        organization.addFileSystem(this)
        this
    }
    def removeOrganization(organization: Organization)(implicit ec: ExecutionContext): Future[FileSystem] = {
        organization.removeFileSystem(this).transformWith({
            case Success(value) => {
                _organizations = _organizations.filter(o => o.id != organization.id)
                Future.successful(this)
            }
            case Failure(cause) => Future.failed(cause)
        })
    }

    // private var _relatedGroups: List[Group] = List()
    // def relatedGroups: List[Group] = _relatedGroups
    // def addRelatedGroup(group: Group): FileSystem = {
    //     _relatedGroups ::= group
    //     group.setRelatedFileSystem(this)
    //     this
    // }
    // def deleteRelatedGroup(group: Group): FileSystem = {
    //     _relatedGroups = _relatedGroups.filter(g => g.id != group.id)
    //     this
    // }

    // private var _fileSystems: List[FileSystem] = List()
    // def relatedFileSystems: List[FileSystem] = _fileSystems
    // def addFileSystem(fileSystem: FileSystem): FileSystem = {
    //     _fileSystems ::= fileSystem
    //     fileSystem.setRelatedFileSystem(this)
    //     this
    // }
    // def deleteFileSystem(fileSystem: FileSystem): FileSystem = {
    //     _fileSystems = _fileSystems.filter(f => f.id != fileSystem.id)
    //     this
    // }

    // private var _applications: List[Application] = List()
    // def applications: List[Application] = _applications
    // def addApplication(application: Application): FileSystem = {
    //     _applications ::= application
    //     application.setRelatedFileSystem(this)
    //     this
    // }
    // def deleteApplication(application: Application): FileSystem = {
    //     _applications = _applications.filter(a => a.id != application.id)
    //     this
    // }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistFileSystem(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteFileSystem(this)
    }
}