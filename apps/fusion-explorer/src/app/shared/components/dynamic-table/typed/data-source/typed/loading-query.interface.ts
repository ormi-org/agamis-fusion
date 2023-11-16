import Filtering from "./filtering.interface"
import Sorting from "./sorting.interface"

export default interface LoadingQuery {
    filters: Filtering[]
    sorting: Sorting
    pageIndex: number
    pageSize: number
}