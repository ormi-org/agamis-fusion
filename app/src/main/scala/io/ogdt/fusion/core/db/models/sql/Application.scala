package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.ApplicationStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import scala.util.Success
import scala.util.Failure

import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.ogdt.fusion.core.db.models.sql.exceptions.EntityRelationConstraintViolationException
import io.ogdt.fusion.core.db.models.sql.relations.OrganizationApplication

class Application(implicit @transient protected val store: ApplicationStore) extends Model {

    @QuerySqlField(name = "app_universal_id", notNull = true)
    private var _appUniversalId: String = null
    def appUniversalId: String = _appUniversalId
    def setAppUniversalId(appUniversalId: String): Application = {
        _appUniversalId = appUniversalId
        this
    }

    @QuerySqlField(name = "version", notNull = true)
    private var _version: String = null
    def version: String = _version
    def setVersion(version: String): Application = {
        _version = version
        this
    }

    @QuerySqlField(name = "status", notNull = true)
    private var _statusValue: Int = Application.NOT_INSTALLED.toInt

    @transient
    private var _status: Application.Status = Application.NOT_INSTALLED
    def status: Application.Status = _status
    def setStatus(status: Application.Status): Application = {
        _status = status
        _statusValue = status.toInt
        this
    }

    @QuerySqlField(name = "manifest_url", notNull = true)
    private var _manifestUrl: String = null
    def manifestUrl: String = _manifestUrl
    def setManifestUrl(url: String): Application = {
        _manifestUrl = url
        this
    }

    @QuerySqlField(name = "store_url", notNull = true)
    private var _storeUrl: String = null
    def storeUrl: String = _storeUrl
    def setStoreUrl(url: String): Application = {
        _storeUrl = url
        this
    }

    @transient
    private var _organizations: List[(Boolean, (OrganizationApplication.Status, Organization, FileSystem))] = List()
    def organizations: List[(Boolean, (OrganizationApplication.Status, Organization, FileSystem))] = _organizations
    def addOrganization(organization: Organization, fileSystem: FileSystem, status: OrganizationApplication.Status = OrganizationApplication.DISABLED): Application = {
        _organizations.find(_._2._2.id == organization.id) match {
            case Some(found) => throw new RelationAlreadyExistsException()
            case None => _organizations ::= (true, (status, organization, fileSystem))
        }
        this
    }
    def removeOrganization(organization: Organization): Application = {
        _organizations =
            _organizations.map({ o =>
                if (o._2._2.id == organization.id) o.copy(_1 = false)
                else o
            })
        this
    }
    def setApplicationStatus(organization: Organization, status: OrganizationApplication.Status): Application = {
        _organizations =
            _organizations.map({ o =>
                if (o._2._2.id == organization.id) o.copy(_2 = (status, o._2._2, o._2._3))
                else o
            })
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistApplication(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteApplication(this)
    }
}

object Application {
    sealed trait Status {
        def toInt: Int
    }
    case object NOT_INSTALLED extends Status {
        def toInt: Int = 0
    }
    case object INSTALLED extends Status {
        def toInt: Int = 1
    }
}