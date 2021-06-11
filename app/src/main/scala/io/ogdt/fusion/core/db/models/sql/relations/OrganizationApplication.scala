package io.ogdt.fusion.core.db.models.sql.relations

import scala.util.Try
import io.ogdt.fusion.core.db.models.sql.exceptions.organizations.InvalidOrganizationApplicationStatus

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class OrganizationApplication private() {

    @QuerySqlField(name = "organization_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_ORGANIZATION_APPLICATION", order = 0)))
    protected var _organizationId: UUID = null

    @QuerySqlField(name = "application_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_ORGANIZATION_APPLICATION", order = 1)))
    protected var _applicationId: UUID = null

    @QuerySqlField(name = "status", notNull = true)
    protected var _statusValue: Int = OrganizationApplication.DISABLED.toInt

    @transient
    protected var _status: OrganizationApplication.Status = OrganizationApplication.DISABLED

    @QuerySqlField(name = "license_file_fs_id", notNull = true)
    protected var _licenseFileFsId: UUID = null

    @QuerySqlField(name = "license_file_id", notNull = true)
    protected var _licenseFileId: String = null
}

object OrganizationApplication {

    def apply(organizationId: UUID, applicationId: UUID, status: OrganizationApplication.Status): OrganizationApplication = {
        var relation = new OrganizationApplication()
        relation._organizationId = organizationId
        relation._applicationId = applicationId
        relation._status = status
        relation._statusValue = status.toInt
        relation
    }

    sealed trait Status {
        def toInt: Int
    }
    object Status {
        def fromInt(input: Int): Try[Status] = {
            Try {
                input match {
                    case 0 => DISABLED
                    case 1 => ENABLED
                    case _ => throw InvalidOrganizationApplicationStatus(s"${input} is not a valid Status value")
                }
            }
        }
    }
    case object DISABLED extends Status {
        def toInt: Int = 0
    }
    case object ENABLED extends Status {
        def toInt: Int = 1
    }
}