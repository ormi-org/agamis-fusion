import { Uniquely } from "@shared/components/dynamic-table/typed/uniquely.interface";
import { Organization } from "./organization.model";
import { Permission } from "./permission.model";
import TimeTracked from "../typed/time-tracked";
import { User } from "./user.model";

export interface Profile extends TimeTracked, Uniquely {
    id: string,
    alias?: string,
    lastName: string,
    firstName: string,
    emails?: Array<string>,
    permissions?: Array<Permission>,
    organization?: Organization,
    lastLogin: Date,
    userId: string,
    user?: User
}