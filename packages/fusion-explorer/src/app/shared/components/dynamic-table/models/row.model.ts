import { TemplateRef } from "@angular/core";

export class Row<T> {
    constructor(
        public uqId: string,
        public index: number,
        public templating: ({
            key: string,
            compute: ((model: Object) => { value: string }),
            template: TemplateRef<any>
        })[],
        public value: T,
    ) {}
}

export function trackByUqId(_: number, el: Row<any>): string {
    return el.uqId;
}