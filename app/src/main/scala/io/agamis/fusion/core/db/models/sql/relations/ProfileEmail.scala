package io.agamis.fusion.core.db.models.sql.relations

import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID

class ProfileEmail private() {

    @QuerySqlField(name = "profile_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_EMAIL", order = 0)))
    protected var _profileId: UUID = _

    @QuerySqlField(name = "email_id", notNull = true, orderedGroups = Array(new QuerySqlField.Group(name = "IX_PROFILE_EMAIL", order = 1)))
    protected var _emailId: UUID = _

    @QuerySqlField(name = "is_main", notNull = true)
    protected var _isMain: Boolean = false
}

object ProfileEmail {
    
    def apply(profileId: UUID, emailId: UUID, isMain: Boolean = false): ProfileEmail = {
        val relation = new ProfileEmail()
        relation._profileId = profileId
        relation._emailId = emailId
        relation._isMain = isMain
        relation
    }
}