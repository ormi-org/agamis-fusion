import Filtering from "@shared/components/dynamic-table/typed/data-source/typed/filtering.interface"
import Sorting from "@shared/components/dynamic-table/typed/data-source/typed/sorting.interface"

export enum IncludableProfileFields {
    EMAILS = "emails",
    PERMISSIONS = "permissions",
    ORGANIZATION = "organization",
    USER = "user"
}

export type ProfileQuery = {
    filters: Filtering[],
    include: [IncludableProfileFields],
    sorting: Sorting,
    offset: number,
    limit: number
}