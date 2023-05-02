import { BehaviorSubject } from "rxjs";

export class Cell {
    constructor(
        public uqId: string,
        public index: number,
        public value: string,
        public widthSubject: BehaviorSubject<number>
    ) {}
}

export function trackByUqId(_: number, el: Cell): string {
    return el.uqId;
}