export interface RowDefinition<T> {
    getIndex(): number
    getModel(): T
}