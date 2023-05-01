import { Ordering } from "@shared/constants/utils/ordering";

export class Column {
    constructor(
        public key: string,
        public value: string,
        public resizable: boolean,
        public ordering: Ordering = Ordering.NONE
    ) {}
}