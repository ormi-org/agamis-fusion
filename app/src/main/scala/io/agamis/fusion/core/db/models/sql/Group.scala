package io.agamis.fusion.core.db.models.sql

import io.agamis.fusion.core.db.datastores.sql.GroupStore

import io.agamis.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID
import java.sql.Timestamp
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.Instant
import io.agamis.fusion.core.db.models.sql.generics.Email

import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException
import io.agamis.fusion.core.db.models.sql.generics.exceptions.RelationNotFoundException
import scala.util.Success
import scala.util.Failure

/** A class that reflects the state of the group entity in the database
  * 
  * @param store an implicit parameter for accessing store methods
  */
class Group(implicit @transient protected val store: GroupStore) extends Model {

    @QuerySqlField(name = "name", notNull = true)
    private var _name: String = _

    /** Group name property
      *
      * @return group name [[java.lang.String String]]
      */
    def name: String = _name

    /** A method for setting name property
      *
      * @param name a [[java.lang.String String]] to assign to group name property
      * @return this object
      */
    def setName(name: String): Group = {
        _name = name
        this
    }

    @transient
    private var _members: List[(Boolean, Profile)] = List()
    def members: List[(Boolean, Profile)] = _members
    def addMember(profile: Profile): Group = {
        _members.indexWhere(_._2.id == profile.id) match {
            case -1 => _members ::= (true, profile)
            case index => _members = _members.updated(index, _members(index).copy(_1 = true))
        }
        this
    }
    def removeMember(profile: Profile): Group = {
        _members.indexWhere(_._2.id == profile.id) match {
            case -1 => throw RelationNotFoundException()
            case index => _members = _members.updated(index, _members(index).copy(_1 = false))
        }
        this
    }

    @transient
    private var _permissions: List[(Boolean, Permission)] = List()
    def permissions: List[(Boolean, Permission)] = _permissions
    def addPermission(permission: Permission): Group = {
        _permissions.indexWhere(_._2.id == permission.id) match {
            case -1 => _permissions ::= (true, permission)
            case index => _permissions = _permissions.updated(index, _permissions(index).copy(_1 = true))
        }
        this
    }
    def removePermission(permission: Permission): Group = {
        _permissions.indexWhere(_._2.id == permission.id) match {
            case -1 => throw RelationAlreadyExistsException()
            case index => _permissions = _permissions.updated(index, _permissions(index).copy(_1 = false))
        }
        this
    }

    @QuerySqlField(name = "organization_id", notNull = true)
    private var _organizationId: UUID = _

    @transient
    private var _relatedOrganization: Option[Organization] = None

    /** Group property that reflects the organization to which this group is related
      *
      * @return related [[io.agamis.fusion.core.db.models.sql.Organization Organization]]
      */
    def relatedOrganization: Option[Organization] = _relatedOrganization

    /** A method for setting group/organization relation
      * 
      * This method automatically set organizationId foreign key
      * 
      * @param organization a [[io.agamis.fusion.core.db.models.sql.Organization Organization]] to assign to this relation
      * @return this object
      */
    def setRelatedOrganization(organization: Organization): Group = {
        _relatedOrganization = Some(organization)
        _organizationId = organization.id
        this
    }

    /** @inheritdoc */
    def persist(implicit ec: ExecutionContext): Future[Group] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store.persistGroup(this).transformWith({
            case Success(_) => Future.successful(this)
            case Failure(e) => Future.failed(e)
        })
    }

    /** @inheritdoc */
    def remove(implicit ec: ExecutionContext): Future[Group] = {
        store.deleteGroup(this).transformWith({
            case Success(_) => Future.successful(this)
            case Failure(e) => Future.failed(e)
        })
    }
}
