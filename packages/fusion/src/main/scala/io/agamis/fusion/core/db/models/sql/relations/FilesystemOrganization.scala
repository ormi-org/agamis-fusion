package io.agamis.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID

class FilesystemOrganization private () {

    @QuerySqlField(
      name = "filesystem_id",
      notNull = true,
      orderedGroups = Array(
        new QuerySqlField.Group(name = "IX_FILESYSTEM_ORGANIZATION", order = 0)
      )
    )
    protected var _filesystemId: UUID = _

    @QuerySqlField(
      name = "organization_id",
      notNull = true,
      orderedGroups = Array(
        new QuerySqlField.Group(name = "IX_FILESYSTEM_ORGANIZATION", order = 1)
      )
    )
    protected var _organizationId: UUID = _

    @QuerySqlField(name = "is_default", notNull = true)
    protected var _isDefault: Boolean = false
}

object FilesystemOrganization {

    def apply(
        filesystemId: UUID,
        organizationId: UUID,
        isDefault: Boolean
    ): FilesystemOrganization = {
        val relation = new FilesystemOrganization()
        relation._filesystemId = filesystemId
        relation._organizationId = organizationId
        relation._isDefault = isDefault
        relation
    }
}
