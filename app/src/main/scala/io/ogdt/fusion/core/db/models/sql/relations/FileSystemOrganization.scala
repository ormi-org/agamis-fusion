package io.ogdt.fusion.core.db.models.sql.relations

import io.ogdt.fusion.core.db.datastores.sql.FileSystemStore
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

abstract class FilesystemOrganization {

    @QuerySqlField(name = "filesystem_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_FILESYSTEM_ORGANIZATION", order = 0)))
    protected var _filesystemId: UUID = null

    @QuerySqlField(name = "organization_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_FILESYSTEM_ORGANIZATION", order = 1)))
    protected var _organizationId: UUID = null

    @QuerySqlField(name = "is_default", notNull = true)
    protected var _isDefault: Boolean = false
}