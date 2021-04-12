package io.ogdt.fusion.core.db.datastores.documents.aggregations.typed

import reactivemongo.api.bson.collection.BSONCollection

trait Pipeline {
    protected val _collection: BSONCollection
    def get: List[_]
}
