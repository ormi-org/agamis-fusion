export enum Ordering {
    DESC = -1,
    NONE = 0,
    ASC = 1
}

export type DefinedOrdering = Ordering.ASC | Ordering.DESC;