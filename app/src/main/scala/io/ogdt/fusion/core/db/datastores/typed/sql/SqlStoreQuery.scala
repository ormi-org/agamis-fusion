package io.ogdt.fusion.core.db.datastores.typed.sql

class SqlStoreQuery(var query: String) {
    var params: Array[_] = Array()

    def setParam(newParams: Array[_]): SqlStoreQuery = {
        params = newParams
        this
    }
}