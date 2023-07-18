package io.agamis.fusion.core.db.models.sql

import io.agamis.fusion.core.db.datastores.sql.FileSystemStore
import io.agamis.fusion.core.db.models.sql.exceptions.organizations.UnsafeFilesystemUnmountException
import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException
import io.agamis.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField

import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp
import java.time.Instant
import scala.util.Success
import scala.util.Failure
import org.apache.ignite.transactions.Transaction

class FileSystem(implicit @transient protected val store: FileSystemStore) extends Model {

    @QuerySqlField(name = "rootdir_id", notNull = true)
    private var _rootdirId: String = _
    def rootdirId: String = _rootdirId
    def setRootdirId(rootdirId: String): FileSystem = {
        _rootdirId = rootdirId
        this
    }

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = _
    def label: String = _label
    def setLabel(label: String): FileSystem = {
        _label = label
        this
    }

    @QuerySqlField(name = "shared", notNull = true)
    private var _shared: Boolean = false
    def shared: Boolean = _shared
    def setShared(): FileSystem = {
        _shared = true
        this
    }
    def setUnshared(): FileSystem = {
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
            case -1 => throw RelationNotFoundException()
            case index =>
                val o = _organizations(index)
                if (o._2._1) throw UnsafeFilesystemUnmountException.IS_DEFAULT_FS()
                else _organizations = _organizations.updated(index, o.copy(_1 = false))
        }
        this
    }

    /**
      * An unpersisted state that reflects installed applications
      */
    @transient
    private var _licensedApplications: List[Application] = List()
    def licensedApplications: List[Application] = _licensedApplications
    def addLicensedApplication(application: Application): FileSystem = {
        _licensedApplications.indexWhere(_.id == application.id) match {
            case -1 => _licensedApplications ::= application
            case index => _licensedApplications = _licensedApplications.updated(index, application)
        }
        this
    }

    def persist(implicit ec: ExecutionContext): Future[(Transaction, FileSystem)] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store.persistFileSystem(this).transformWith({
            case Success(tx) => Future.successful((tx, this))
            case Failure(e) => Future.failed(e)
        })
    }

    def remove(implicit ec: ExecutionContext): Future[(Transaction, FileSystem)] = {
        store.deleteFileSystem(this).transformWith({
            case Success(tx) => Future.successful((tx, this))
            case Failure(e) => Future.failed(e)
        })
    }
}