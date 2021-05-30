package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.GroupStore

import io.ogdt.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField
import java.util.UUID
import java.sql.Timestamp
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.time.Instant
import io.ogdt.fusion.core.db.models.sql.generics.Email

import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException

/** A class that reflects the state of the group entity in the database
  * 
  * @param store an implicit parameter for accessing store methods
  */
class Group(implicit @transient protected val store: GroupStore) extends Model {

    @QuerySqlField(name = "name", notNull = true)
    private var _name: String = null

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
        _members.find(_._2.id == profile.id) match {
            case Some(found) => throw new RelationAlreadyExistsException()
            case None => _members ::= (true, profile)
        }
        this
    }
    def removeMember(profile: Profile): Group = {
        _members =
            _members.map({ p =>
                if (p._2.id == profile.id) p.copy(_1 = false)
                else p
            })
        this
    }

    @transient
    private var _permissions: List[(Boolean, Permission)] = List()
    def permissions: List[(Boolean, Permission)] = _permissions
    def addPermission(permission: Permission): Group = {
        _permissions.find(_._2.id == permission.id) match {
            case Some(found) => throw new RelationAlreadyExistsException()
            case None => _permissions ::= (true, permission)
        }
        this
    }
    def removePermission(permission: Permission): Group = {
        _permissions =
            _permissions.map({ p =>
                if (p._2.id == permission.id) p.copy(_1 = false)
                else p
            })
        this
    }

    @QuerySqlField(name = "organization_id", notNull = true)
    private var _organizationId: UUID = null

    @transient
    private var _relatedOrganization: Option[Organization] = None

    /** Group property that reflects the organization to which this group is related
      *
      * @return related [[io.ogdt.fusion.core.db.models.sql.Organization Organization]]
      */
    def relatedOrganization: Option[Organization] = _relatedOrganization

    /** A method for setting group/organization relation
      * 
      * This method automatically set organizationId foreign key
      * 
      * @param user a [[io.ogdt.fusion.core.db.models.sql.Organization Organization]] to assign to this relation
      * @return this object
      */
    def setRelatedOrganization(organization: Organization): Group = {
        _relatedOrganization = Some(organization)
        _organizationId = organization.id
        this
    }

    /** @inheritdoc */
    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        this.setUpdatedAt(Timestamp.from(Instant.now()))
        store.persistGroup(this)
    }

    /** @inheritdoc */
    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteGroup(this)
    }
}
