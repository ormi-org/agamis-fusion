export class Cell {
    constructor(
        public uqId: string,
        public index: number,
        public value: string,
    ) {}
}

export function trackByUqId(_: number, el: Cell): string {
    return el.uqId;
}