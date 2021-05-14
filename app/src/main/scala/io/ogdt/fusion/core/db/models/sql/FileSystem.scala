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

class FileSystem(implicit @transient protected val store: FileSystemStore) extends Model {

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

    @transient
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
            case Failure(cause) => Future.failed(new Exception("bla bla bla",cause)) // TODO : changer pour une custom
        })
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistFileSystem(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteFileSystem(this)
    }
}