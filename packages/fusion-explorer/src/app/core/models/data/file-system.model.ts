import { Application } from "./application.model"
import { Organization } from "./organization.model"

export interface FileSystem {
    id: string,
    rootdirId: string,
    label: string,
    shared: boolean,
    organizations?: Array<[boolean, Organization]>,
    licensedApplications?: Array<Application>
}