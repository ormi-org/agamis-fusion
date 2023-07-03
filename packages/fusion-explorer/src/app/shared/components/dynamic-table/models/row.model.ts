import { TemplateRef } from "@angular/core";

export class Row<T> {
    constructor(
        public uqId: string,
        public index: number,
        public templating: ({
            key: string,
            compute: ((model: object) => { value: string }),
            template: TemplateRef<unknown>
        })[],
        public value: T,
    ) {}
}

export function trackByUqId(_: number, el: Row<unknown>): string {
    return el.uqId;
}