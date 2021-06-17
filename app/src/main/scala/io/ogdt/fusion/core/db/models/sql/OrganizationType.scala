package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationTypeStore
import java.util.UUID
import org.apache.ignite.cache.query.annotations.QuerySqlField
import io.ogdt.fusion.core.db.models.sql.typed.Model
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import io.ogdt.fusion.core.db.models.sql.generics.Language
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException

class OrganizationType(implicit @transient protected val store: OrganizationTypeStore) extends Model {

    @QuerySqlField(name = "label_text_id", notNull = true)
    private var _labelTextId: UUID = null
    def labelTextId: UUID = _labelTextId
    def setLabelTextId(labelTextId: String): OrganizationType = {
        _labelTextId = UUID.fromString(labelTextId)
        this
    }
    
    @transient
    private var _labels: Map[(UUID, UUID), (String, String)] = Map()
    /** A mapping of all labels for this OrgnizationType
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
    def setLabel(language: Language, content: String): OrganizationType = {
        _labels = _labels.+(((_labelTextId, language.id), (language.code, content)))
        this
    }

    @transient
    private var _relatedOrganizations: List[(Boolean, Organization)] = List()
    def relatedOrganizations: List[(Boolean, Organization)] = _relatedOrganizations
    def addRelatedOrganization(organization: Organization): OrganizationType = {
        _relatedOrganizations.indexWhere(_._2.id == organization.id) match {
            case -1 => _relatedOrganizations ::= (true, organization)
            case index => _relatedOrganizations = _relatedOrganizations.updated(index, _relatedOrganizations(index).copy(_1 = true))
        }
        this
    }
    def removeRelatedOrganization(organization: Organization): OrganizationType = {
        _relatedOrganizations.indexWhere(_._2.id == organization.id) match {
            case -1 => throw new RelationNotFoundException()
            case index => _relatedOrganizations = _relatedOrganizations.updated(index, _relatedOrganizations(index).copy(_1 = false))
        }
        this
    }

    override protected def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistOrganizationType(this)
    }

    override protected def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteOrgnizationType(this)
    }
}
