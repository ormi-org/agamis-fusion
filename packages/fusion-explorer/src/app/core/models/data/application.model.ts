import { ApplicationStatus } from "../enums/application-status"
import { OrganizationApplicationStatus } from "../enums/organization-application-status"
import { FileSystem } from "./file-system.model"
import { Organization } from "./organization.model"
import { Permission } from "./permission.model"

export interface Application {
    id: string,
    appUniversalId: string,
    label: string,
    version: string,
    status: ApplicationStatus,
    manifestUrl: string,
    storeUrl: string,
    organizations: Array<[OrganizationApplicationStatus, Organization, [FileSystem, string]]>,
    relatedPermissions: Array<Permission>
}