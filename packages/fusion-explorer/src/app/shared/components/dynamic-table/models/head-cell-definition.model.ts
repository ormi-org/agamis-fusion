export class HeadCellDefinition {
    constructor(
        private value: string = "undefined text"
    ) {}

    getValue(): string {
        return this.value;
    };
}