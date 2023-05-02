export class Row<T> {
    constructor(
        public uqId: string,
        public index: number,
        public keys: string[],
        public value: T,
    ) {}
}

export function trackByUqId(_: number, el: Row<any>): string {
    return el.uqId;
}