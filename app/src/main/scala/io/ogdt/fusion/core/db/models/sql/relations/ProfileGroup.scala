package io.ogdt.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class ProfileGroup private() {

    @QuerySqlField(name = "profile_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_GROUP", order = 0)))
    protected var _profileId: UUID = null

    @QuerySqlField(name = "group_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_GROUP", order = 1)))
    protected var _groupId: UUID = null
}

object ProfileGroup {

    def apply(profileId: UUID, groupId: UUID): ProfileGroup = {
        var relation = new ProfileGroup()
        relation._profileId = profileId
        relation._groupId = groupId
        relation
    }
}