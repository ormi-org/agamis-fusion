package io.agamis.fusion.core.db.datastore.document.aggregation.typed

import reactivemongo.api.bson.collection.BSONCollection

trait Pipeline {
    protected val _collection: BSONCollection
    def get: List[_]
}
