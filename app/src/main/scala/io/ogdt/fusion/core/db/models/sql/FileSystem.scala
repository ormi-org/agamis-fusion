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

import io.ogdt.fusion.core.db.models.sql.exceptions.organizations.UnsafeFilesystemUnmountException
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException

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
    private var _organizations: List[(Boolean, (Boolean, Organization))] = List()
    def organizations: List[(Boolean, (Boolean, Organization))] = _organizations
    def addOrganization(organization: Organization, asDefault: Boolean = false): FileSystem = {
        _organizations.indexWhere(_._2._2.id == organization.id) match {
            case -1 => _organizations ::= (true, (asDefault, organization))
            case index => _organizations = _organizations.updated(index, _organizations(index).copy(_1 = true))
        }
        this
    }
    def removeOrganization(organization: Organization)(implicit ec: ExecutionContext): FileSystem = {
        _organizations.indexWhere(_._2._2.id == organization.id) match {
            case -1 => throw new RelationNotFoundException()
            case index => {
                val o = _organizations(index)
                if (o._2._1) throw UnsafeFilesystemUnmountException.IS_DEFAULT_FS()
                else _organizations = _organizations.updated(index, o.copy(_1 = false))
            }
        }
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistFileSystem(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteFileSystem(this)
    }
}