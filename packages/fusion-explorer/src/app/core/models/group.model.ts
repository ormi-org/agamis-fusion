import { Organization } from "./organization.model";
import { Permission } from "./permission.model";
import { Profile } from "./profile.model";
import TimeTracked from "./typed/time-tracked";

export interface Group extends TimeTracked {
    id: string,
    name: string,
    members?: Array<Profile>,
    permissions?: Array<Permission>,
    relatedOrganization?: Organization
}