package io.ogdt.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class GroupPermission private() {

    @QuerySqlField(name = "group_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_GROUP_PERMISSION", order = 0)))
    protected var _groupId: UUID = null

    @QuerySqlField(name = "permission_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_GROUP_PERMISSION", order = 1)))
    protected var _permissionId: UUID = null
}

object GroupPermission {

    def apply(groupId: UUID, permissionId: UUID): GroupPermission = {
        var relation = new GroupPermission()
        relation._groupId = groupId
        relation._permissionId = permissionId
        relation
    }
}