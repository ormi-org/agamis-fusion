package io.ogdt.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class ProfileEmail private() {

    @QuerySqlField(name = "profile_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_EMAIL", order = 0)))
    protected var _profileId: UUID = null

    @QuerySqlField(name = "email_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_EMAIL", order = 1)))
    protected var _emailId: UUID = null

    @QuerySqlField(name = "is_main", notNull = true)
    protected var _isMain: Boolean = false
}

// object ProfileEmail {
    
//     def apply()
// }