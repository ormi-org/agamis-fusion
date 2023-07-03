import { Ordering } from "@shared/constants/utils/ordering";
import { BehaviorSubject } from "rxjs";

export class Column {
    private width = 0;

    constructor(
        public key: string,
        public value: string,
        public resizable: boolean,
        public ordering: Ordering = Ordering.NONE,
        public widthSubject: BehaviorSubject<number>
    ) {
        this.widthSubject.subscribe((updatedWidth) => {
            this.width = updatedWidth;
        })
    }

    getCurrentWidth(): number {
        return this.width;
    }
}