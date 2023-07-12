import { Ordering } from "@shared/constants/utils/ordering"
import { Observable } from "rxjs"

export interface HeadCellDefinition {
    getOrdering(): Observable<Ordering>
    clearOrdering(): void
}