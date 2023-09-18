package io.agamis.fusion.core.db.models.sql

import io.agamis.fusion.core.db.datastores.sql.ApplicationStore
import io.agamis.fusion.core.db.models.sql.exceptions.applications.InvalidApplicationStatus
import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException
import io.agamis.fusion.core.db.models.sql.relations.OrganizationApplication
import io.agamis.fusion.core.db.models.sql.relations.OrganizationApplication.Status
import io.agamis.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import org.apache.ignite.transactions.Transaction

import java.sql.Timestamp
import java.time.Instant
import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class Application(implicit @transient protected val store: ApplicationStore)
    extends Model {

    @QuerySqlField(name = "app_universal_id", notNull = true)
    private var _appUniversalId: String = null
    def appUniversalId: String          = _appUniversalId
    def setAppUniversalId(appUniversalId: String): Application = {
        _appUniversalId = appUniversalId
        this
    }

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = null
    def label: String          = _label
    def setLabel(label: String): Application = {
        _label = label
        this
    }

    @QuerySqlField(name = "version", notNull = true)
    private var _version: String = null
    def version: String          = _version
    def setVersion(version: String): Application = {
        _version = version
        this
    }

    @QuerySqlField(name = "status", notNull = true)
    @nowarn
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
    def manifestUrl: String          = _manifestUrl
    def setManifestUrl(url: String): Application = {
        _manifestUrl = url
        this
    }

    @QuerySqlField(name = "store_url", notNull = true)
    private var _storeUrl: String = null
    def storeUrl: String          = _storeUrl
    def setStoreUrl(url: String): Application = {
        _storeUrl = url
        this
    }

    @transient
    private var _organizations
        : List[(Boolean, (Status, Organization, (FileSystem, String)))] = List()
    def organizations: List[
      (
          Boolean,
          (OrganizationApplication.Status, Organization, (FileSystem, String))
      )
    ] = _organizations
    def addOrganization(
        organization: Organization,
        licenseStore: FileSystem,
        licenseFileId: String,
        status: OrganizationApplication.Status =
            OrganizationApplication.DISABLED
    ): Application = {
        _organizations.indexWhere(_._2._2.id == organization.id) match {
            case -1 =>
                _organizations ::= (true, (
                  status,
                  organization,
                  (licenseStore, licenseFileId)
                ))
            case index =>
                _organizations = _organizations.updated(
                  index,
                  _organizations(index).copy(_1 = true)
                )
        }
        this
    }
    def removeOrganization(organization: Organization): Application = {
        _organizations.indexWhere(_._2._2.id == organization.id) match {
            case -1 => throw new RelationNotFoundException()
            case index =>
                _organizations = _organizations.updated(
                  index,
                  _organizations(index).copy(_1 = false)
                )
        }
        this
    }

    def setOrganizationApplicationStatus(
        organization: Organization,
        status: OrganizationApplication.Status
    ): Application = {
        _organizations.indexWhere(_._2._2.id == organization.id) match {
            case -1 => throw new RelationNotFoundException()
            case index => {
                val o = _organizations(index)
                _organizations = _organizations.updated(
                  index,
                  o.copy(_2 = (status, o._2._2, o._2._3))
                )
            }
        }
        this
    }

    @transient
    private var _relatedPermissions: List[(Boolean, Permission)] = List()
    def relatedPermissions: List[(Boolean, Permission)] = _relatedPermissions
    def addPermission(permission: Permission): Application = {
        _relatedPermissions.indexWhere(_._2.id == permission.id) match {
            case -1 => _relatedPermissions ::= (true, permission)
            case index =>
                _relatedPermissions = _relatedPermissions.updated(
                  index,
                  _relatedPermissions(index).copy(_1 = true)
                )
        }
        this
    }

    def persist(implicit
        ec: ExecutionContext
    ): Future[(Transaction, Application)] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store
            .persistApplication(this)
            .transformWith({
                case Success(tx) => Future.successful((tx, this))
                case Failure(e)  => Future.failed(e)
            })
    }

    def remove(implicit
        ec: ExecutionContext
    ): Future[(Transaction, Application)] = {
        store
            .deleteApplication(this)
            .transformWith({
                case Success(tx) => Future.successful((tx, this))
                case Failure(e)  => Future.failed(e)
            })
    }
}

object Application {
    sealed trait Status {
        def toInt: Int
    }
    object Status {
        def fromInt(input: Int): Try[Status] = {
            Try {
                input match {
                    case 0 => NOT_INSTALLED
                    case 1 => INSTALLED
                    case _ =>
                        throw InvalidApplicationStatus(
                          s"${input} is not a valid Status value"
                        )
                }
            }
        }
    }
    case object NOT_INSTALLED extends Status {
        def toInt: Int = 0
    }
    case object INSTALLED extends Status {
        def toInt: Int = 1
    }
}
