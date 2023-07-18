import Filtering from "@shared/components/dynamic-table/typed/data-source/typed/filtering.interface"
import Sorting from "@shared/components/dynamic-table/typed/data-source/typed/sorting.interface"

export type UserQuery = {
    filters: Filtering[],
    sorting: Sorting,
    pageIndex: number,
    pageSize: number
}