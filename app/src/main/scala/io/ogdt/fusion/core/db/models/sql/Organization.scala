package io.ogdt.fusion.core.db.models.sql

import io.ogdt.fusion.core.db.datastores.sql.OrganizationStore
import io.ogdt.fusion.core.db.models.sql.typed.Model

import io.ogdt.fusion.core.db.models.sql.relations.OrganizationApplication

import org.apache.ignite.cache.query.annotations.QuerySqlField

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.sql.Timestamp
import scala.util.Success
import scala.util.Failure

import io.ogdt.fusion.core.db.models.sql.exceptions.organizations.{
    UnsafeFilesystemUnmountException,
    UnsafeFilesystemMountException
}
import io.ogdt.fusion.core.db.models.sql.generics.exceptions.RelationAlreadyExistsException

class Organization(implicit @transient protected val store: OrganizationStore) extends Model {

    @QuerySqlField(name = "label", notNull = true)
    private var _label: String = null
    def label: String = _label
    def setLabel(label: String): Organization = {
        _label = label
        this
    }

    @QuerySqlField(name = "organizationtype_id", notNull = true)
    private var _organizationtypeId: UUID = null

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
    def setQueryable: Organization = {
        _queryable = true
        this
    }
    def setUnqueryable: Organization = {
        _queryable = false
        this
    }

    @transient
    private var _relatedProfiles: List[Profile] = List()
    def relatedProfiles: List[Profile] = _relatedProfiles
    def addRelatedProfile(profile: Profile): Organization = {
        _relatedProfiles ::= profile
        this
    }
    def removeRelatedProfile(profile: Profile): Organization = {
        _relatedProfiles = _relatedProfiles.filter(p => p.id != profile.id)
        this
    }

    @transient
    private var _relatedGroups: List[Group] = List()
    def relatedGroups: List[Group] = _relatedGroups
    def addRelatedGroup(group: Group): Organization = {
        _relatedGroups ::= group
        this
    }
    def removeRelatedGroup(group: Group): Organization = {
        _relatedGroups = _relatedGroups.filter(g => g.id != group.id)
        this
    }

    @transient
    private var _defaultFileSystem: FileSystem = null
    def defaultFileSystem: FileSystem = _defaultFileSystem
    def setDefaultFileSystem(fileSystem: FileSystem): Organization = {
        _fileSystems.find(_._2.id == fileSystem.id) match {
            case Some(found) => {
                _fileSystems = _fileSystems.map({ f =>
                    if (f._2.id == fileSystem.id) f.copy(_1 = false)
                    else f
                })
                _defaultFileSystem = fileSystem
                this
            }
            case None => throw UnsafeFilesystemMountException.MUST_BE_MOUNTED_FIRST()
        }
    }

    @transient
    private var _fileSystems: List[(Boolean, FileSystem)] = List()
    def fileSystems: List[(Boolean, FileSystem)] = _fileSystems
    def addFileSystem(fileSystem: FileSystem): Organization = {
        _fileSystems.find(_._2.id == fileSystem.id) match {
            case Some(found) => throw new RelationAlreadyExistsException()
            case None => _fileSystems ::= (true,fileSystem)
        }
        this
    }
    def removeFileSystem(fileSystem: FileSystem)(implicit ec: ExecutionContext): Organization = {
        _fileSystems.find(o => o._2.id == fileSystem.id && o._1 == true) match {
            case Some(found) => throw UnsafeFilesystemUnmountException.IS_DEFAULT_FS()
            case None => {
                _fileSystems = 
                    _fileSystems.map({ o => 
                        if(o._2.id == fileSystem.id) o.copy(_1 = false)
                        else o
                    })
                this
            }
        }
    }

    @transient
    private var _applications: List[(Boolean, (OrganizationApplication.Status, Application))] = List()
    def applications: List[(Boolean, (OrganizationApplication.Status, Application))] = _applications
    def addApplication(application: Application, status: OrganizationApplication.Status): Organization = {
        _applications.find(_._2._2.id == application.id) match {
            case Some(found) => throw new RelationAlreadyExistsException()
            case None => _applications ::= (true, (status, application))
        }
        this
    }
    def removeApplication(application: Application): Organization = {
        _applications =
            _applications.map({ a =>
                if (a._2._2.id == application.id) a.copy(_1 = false)
                else a
            })
        this
    }

    def persist(implicit ec: ExecutionContext): Future[Unit] = {
        store.persistOrganization(this)
    }

    def remove(implicit ec: ExecutionContext): Future[Unit] = {
        store.deleteOrganization(this)
    }
}