export type LocalStorageSuccess<T> = { result: 'found', item: T };
export type LocalStorageError = { result: 'error', error: string };

export type LocalStorageResult<T> =
    | LocalStorageSuccess<T>
    | LocalStorageError;