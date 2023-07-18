package io.agamis.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class ProfilePermission private() {

    @QuerySqlField(name = "profile_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_PERMISSION", order = 0)))
    protected var _profileId: UUID = _

    @QuerySqlField(name = "permission_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_PERMISSION", order = 1)))
    protected var _permissionId: UUID = _
}

object ProfilePermission {

    def apply(profileId: UUID, permissionId: UUID): ProfilePermission = {
        val relation = new ProfilePermission()
        relation._profileId = profileId
        relation._permissionId = permissionId
        relation
    }
}