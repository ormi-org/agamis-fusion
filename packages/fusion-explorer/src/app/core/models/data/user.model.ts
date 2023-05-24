import { Uniquely } from "@shared/components/dynamic-table/typed/uniquely.interface";
import { Profile } from "./profile.model";
import TimeTracked from "../typed/time-tracked";

export interface User extends TimeTracked, Uniquely {
    id: string,
    username: string,
    profiles: Array<Profile>
}