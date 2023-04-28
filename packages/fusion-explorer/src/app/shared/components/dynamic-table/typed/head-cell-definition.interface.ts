import { Ordering } from "@shared/constants/utils/ordering";
import { Observable } from "rxjs";

export interface HeadCellDefinition {
    getValue(): string;
    isResizable(): boolean;
    getOrdering(): Observable<Ordering>;
}