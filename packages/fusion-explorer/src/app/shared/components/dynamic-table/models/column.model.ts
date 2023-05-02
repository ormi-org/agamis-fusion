import { Ordering } from "@shared/constants/utils/ordering";
import { BehaviorSubject } from "rxjs";

export class Column {
    constructor(
        public key: string,
        public value: string,
        public resizable: boolean,
        public ordering: Ordering = Ordering.NONE,
        public widthSubject: BehaviorSubject<number>
    ) {}
}