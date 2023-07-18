import { TemplateRef } from "@angular/core"
import { BehaviorSubject } from "rxjs"

export class Cell {
    constructor(
        public uqId: string,
        public index: number,
        public context: {
            value: string
        },
        public template: TemplateRef<unknown>,
        public widthSubject: BehaviorSubject<number>
    ) {}
}

export function trackByUqId(_: number, el: Cell): string {
    return el.uqId
}