import { Application } from "./application.model";
import { OrganizationApplicationStatus } from "../enums/organization-application-status";
import { FileSystem } from "./file-system.model";
import { Profile } from "./profile.model";
import { LocalizedText } from "../typed/localized-text";
import TimeTracked from "../typed/time-tracked";

export interface Organization extends TimeTracked {
    id: string,
    label: string,
    type: LocalizedText,
    queryable: boolean,
    profiles?: Array<Profile>,
    // groups?: Array<Group>,
    defaultFileSystem?: FileSystem,
    fileSystems?: Array<FileSystem>,
    applications?: Array<[OrganizationApplicationStatus, Application]>,
}