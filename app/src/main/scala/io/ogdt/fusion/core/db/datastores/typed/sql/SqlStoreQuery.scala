package io.ogdt.fusion.core.db.datastores.typed.sql

class SqlStoreQuery(var query: String) {
    private var _params: List[_] = List()
    def params: List[_] = _params

    def setParams(newParams: List[_]): SqlStoreQuery = {
        _params = newParams
        this
    }
}