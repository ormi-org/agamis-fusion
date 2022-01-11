package io.agamis.fusion.core.db.models.sql

import io.agamis.fusion.core.db.datastores.sql.OrganizationStore
import io.agamis.fusion.core.db.models.sql.exceptions.applications.UnsafeApplicationRemovalException
import io.agamis.fusion.core.db.models.sql.exceptions.organizations.{UnsafeFilesystemMountException, UnsafeFilesystemUnmountException}
import io.agamis.fusion.core.db.models.sql.generics.exceptions.{RelationAlreadyExistsException, RelationNotFoundException}
import io.agamis.fusion.core.db.models.sql.relations.OrganizationApplication
import io.agamis.fusion.core.db.models.sql.typed.Model
import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class Organization(implicit @transient protected val store: OrganizationStore) extends Model {

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = _
    def label: String = _label
    def setLabel(label: String): Organization = {
        _label = label
        this
    }

    @QuerySqlField(name = "organizationtype_id", notNull = true)
    private var _organizationtypeId: UUID = _

    @transient
    private var _type: Option[OrganizationType] = None
    def `type`: Option[OrganizationType] = _type
    def setType(`type`: OrganizationType): Organization = {
        _type = Some(`type`)
        _organizationtypeId = `type`.id
        this
    }

    @QuerySqlField(name = "queryable", notNull = true)
    private var _queryable: Boolean = false
    def queryable: Boolean = _queryable
    def setQueryable(): Organization = {
        _queryable = true
        this
    }
    def setUnqueryable(): Organization = {
        _queryable = false
        this
    }

    @transient
    private var _relatedProfiles: List[(Boolean, Profile)] = List()
    def relatedProfiles: List[(Boolean, Profile)] = _relatedProfiles
    def addRelatedProfile(profile: Profile): Organization = {
        _relatedProfiles.indexWhere(_._2.id == profile.id) match {
            case -1 => _relatedProfiles ::= (true, profile)
            case index => _relatedProfiles = _relatedProfiles.updated(index, _relatedProfiles(index).copy(_1 = true))
        }
        this
    }
    def removeRelatedProfile(profile: Profile): Organization = {
        _relatedProfiles.indexWhere(_._2.id == profile.id) match {
            case -1 => throw RelationNotFoundException()
            case index => _relatedProfiles = _relatedProfiles.updated(index, _relatedProfiles(index).copy(_1 = false))
        }
        this
    }

    @transient
    private var _relatedGroups: List[(Boolean, Group)] = List()
    def relatedGroups: List[(Boolean, Group)] = _relatedGroups
    def addRelatedGroup(group: Group): Organization = {
        _relatedGroups.indexWhere(_._2.id == group.id) match {
            case -1 => _relatedGroups ::= (true, group)
            case index => _relatedGroups = _relatedGroups.updated(index, _relatedGroups(index).copy(_1 = true))
        }
        this
    }
    def removeRelatedGroup(group: Group): Organization = {
        _relatedGroups.indexWhere(_._2.id == group.id) match {
            case -1 => throw RelationNotFoundException()
            case index => _relatedGroups = _relatedGroups.updated(index, _relatedGroups(index).copy(_1 = false))
        }
        this
    }

    @transient
    private var _defaultFileSystem: FileSystem = _
    def defaultFileSystem: FileSystem = _defaultFileSystem
    def setDefaultFileSystem(fileSystem: FileSystem): Organization = {
        _fileSystems.find(_._2.id == fileSystem.id) match {
            case Some(_) =>
                _fileSystems = _fileSystems.map({ f =>
                    if (f._2.id == fileSystem.id) f.copy(_1 = false)
                    else f
                })
                _defaultFileSystem = fileSystem
                this
            case None => throw UnsafeFilesystemMountException.MUST_BE_MOUNTED_FIRST()
        }
    }

    @transient
    private var _fileSystems: List[(Boolean, FileSystem)] = List()
    def fileSystems: List[(Boolean, FileSystem)] = _fileSystems
    def addFileSystem(fileSystem: FileSystem): Organization = {
        _fileSystems.find(_._2.id == fileSystem.id) match {
            case Some(_) => throw RelationAlreadyExistsException()
            case None => _fileSystems ::= (true,fileSystem)
        }
        this
    }
    def removeFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Organization = {
        _fileSystems.find(o => o._2.id == fileSystem.id && o._1) match {
            case Some(_) => throw UnsafeFilesystemUnmountException.IS_DEFAULT_FS()
            case None =>
                _fileSystems =
                    _fileSystems.map({ o =>
                        if(o._2.id == fileSystem.id) o.copy(_1 = false)
                        else o
                    })
                this
        }
    }

    @transient
    private var _applications: List[(Boolean, (OrganizationApplication.Status, Application))] = List()
    def applications: List[(Boolean, (OrganizationApplication.Status, Application))] = _applications
    def addApplication(application: Application, status: OrganizationApplication.Status): Organization = {
        _applications.indexWhere(_._2._2.id == application.id) match {
            case -1 => _applications ::= (true, (status, application))
            case index => _applications = _applications.updated(index, _applications(index).copy(_1 = true))
        }
        this
    }
    def removeApplication(application: Application): Organization = {
        _applications.indexWhere(_._2._2.id == application.id) match {
            case -1 => throw RelationNotFoundException()
            case index =>
                val a = _applications(index)
                if (a._2._1 == OrganizationApplication.DISABLED) throw UnsafeApplicationRemovalException.IS_ENABLED()
                else _applications = _applications.updated(index, a.copy(_1 = false))
        }
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistOrganization(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteOrganization(this)
    }
}