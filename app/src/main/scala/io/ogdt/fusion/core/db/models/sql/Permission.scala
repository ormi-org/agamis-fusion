package io.ogdt.fusion.core.db.models.sql

import org.apache.ignite.cache.query.annotations.QuerySqlField
import io.ogdt.fusion.core.db.datastores.sql.PermissionStore
import io.ogdt.fusion.core.db.models.sql.typed.Model
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import io.ogdt.fusion.core.db.models.sql.generics.Language

class Permission(implicit @transient protected val store: PermissionStore) extends Model {

    @QuerySqlField(name = "key", notNull = true)
    private var _key: String = null
    def key: String = _key
    def setKey(key: String): Permission = {
        _key = key
        this
    }

    @QuerySqlField(name = "label_text_id", notNull = true)
    private var _labelTextId: UUID = UUID.randomUUID()
    def labelTextId: UUID = _labelTextId
    def setLabelTextId(labelTextId: String): Permission = {
        _labelTextId = UUID.fromString(labelTextId)
        this
    }
    
    @transient
    private var _labels: Map[(UUID, UUID), (String, String)] = Map()
    /** A mapping of all labels for this Permission
      *
      * @return a map of [(text_id, language_id), (language_code, content)]
      */
    def labels: Map[(UUID, UUID), (String, String)] = _labels
    def label(languageCode: String): String = {
        _labels.find(_._2._1 == languageCode) match {
            case Some(value) => value._2._2
            case None => throw new Exception("Text not found for this languageCode") // TODO : custom
        }
    }
    def label(languageId: UUID): String = {
        _labels.get((_labelTextId, languageId)) match {
            case Some(value) => value._2
            case None => throw new Exception("Text not found for this languageId") // TODO : custom
        }
    }
    def setLabel(language: Language, content: String): Permission = {
        _labels = _labels.+(((_labelTextId, language.id), (language.code, content)))
        this
    }

    @QuerySqlField(name = "description_text_id", notNull = true)
    private var _descriptionTextId: UUID = UUID.randomUUID()
    def descriptionTextId: UUID = _descriptionTextId
    def setDescriptionTextId(descriptionTextId: String): Permission = {
        _descriptionTextId = UUID.fromString(descriptionTextId)
        this
    }

    @transient
    private var _descriptions: Map[(UUID, UUID), (String, String)] = Map()
    /** A mapping of all descriptions for this Permission
      *
      * @return a map of [(text_id, language_id), (language_code, content)]
      */
    def descriptions: Map[(UUID, UUID), (String, String)] = _descriptions
    def description(languageCode: String): String = {
        _descriptions.find(_._2._1 == languageCode) match {
            case Some(value) => value._2._2
            case None => throw new Exception("Text not found for this languageCode") // TODO : custom
        }
    }
    def description(languageId: UUID): String = {
        _descriptions.get((_descriptionTextId, languageId)) match {
            case Some(value) => value._2
            case None => throw new Exception("Text not found for this languageId") // TODO : custom
        }
    }
    def setDescription(language: Language, content: String): Permission = {
        _descriptions = _descriptions.+(((_descriptionTextId, language.id), (language.code, content)))
        this
    }

    @QuerySqlField(name = "editable", notNull = true)
    private var _editable: Boolean = false
    def editable: Boolean = _editable
    def setEditable: Permission = {
        _editable = true
        this
    }
    def setReadonly: Permission = {
        _editable = false
        this
    }

    @QuerySqlField(name = "app_id", notNull = true)
    private var _appId: UUID = null
    def appId: UUID = _appId
    def setAppId(appId: UUID): Permission = {
        _appId = appId
        this
    }

    /** @inheritdoc */
    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        Future.unit
    }

    /** @inheritdoc */
    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        Future.unit
    }
}