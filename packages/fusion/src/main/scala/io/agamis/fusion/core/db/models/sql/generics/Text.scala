package io.agamis.fusion.core.db.models.sql.generics

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.annotation.nowarn

class Text {

    @QuerySqlField(
      name = "id",
      notNull = true,
      orderedGroups = Array(
        new QuerySqlField.Group(name = "IX_TEXT_LANGUAGE", order = 0)
      )
    )
    protected var _id: UUID = UUID.randomUUID()
    def id: UUID            = _id
    // Used to set UUID (mainly for setting uuid of existing text when fetching)
    def setId(id: String): Text = {
        _id = UUID.fromString(id)
        this
    }

    @QuerySqlField(
      name = "language_id",
      notNull = true,
      orderedGroups = Array(
        new QuerySqlField.Group(name = "IX_TEXT_LANGUAGE", order = 1)
      )
    )
    @nowarn
    private var _languageId: UUID = null

    @transient
    private var _relatedLanguage: Option[Language] = None
    def relatedLanguage: Option[Language]          = _relatedLanguage
    def setRelatedLanguage(language: Language): Text = {
        _relatedLanguage = Some(language)
        _languageId = language.id
        this
    }

    @QuerySqlField(name = "content", notNull = true)
    private var _content: String = null
    def content: String          = _content
    def setContent(content: String): Text = {
        _content = content
        this
    }
}
