import { Organization } from "./organization.model";
import { Permission } from "./permission.model";

export interface Profile {
    id: string,
    alias?: string,
    lastName: string,
    firstName: string,
    emails?: Array<string>,
    permissions?: Array<Permission>,
    organization?: Organization,
    lastLogin: Date,
    userId?: string
}