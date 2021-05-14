package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationTypeStore
import java.util.UUID
import org.apache.ignite.cache.query.annotations.QuerySqlField
import io.ogdt.fusion.core.db.models.sql.typed.Model
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class OrganizationType(implicit @transient protected val store: OrganizationTypeStore) extends Model {

    @QuerySqlField(name = "label_text_id", notNull = true)
    private var _labelTextId: UUID = null
    def labelTextId: UUID = _labelTextId
    def setLabelTextId(labelTextId: String): OrganizationType = {
        _labelTextId = UUID.fromString(labelTextId)
        this
    }
    
    @transient
    private var _labels: Map[UUID, (String, String)] = Map()
    def labels: Map[UUID, (String, String)] = _labels
    // def label(languageCode: String) = _labels.find(_._2._1)

    @transient
    private var _relatedOrganizations: List[Organization] = List()
    def relatedOrganizations: List[Organization] = _relatedOrganizations
    def addRelatedOrganization(organization: Organization): OrganizationType = {
        _relatedOrganizations ::= organization
        this
    }
    def removeRelatedOrganization(organization: Organization): OrganizationType = {
        _relatedOrganizations = _relatedOrganizations.filterNot(o => o.id == organization.id)
        this
    }

    override protected def persist(implicit ec: ExecutionContext): Future[Unit] = {
        Future.successful()
    }

    override protected def remove(implicit ec: ExecutionContext): Future[Unit] = {
        Future.successful()
    }
}
