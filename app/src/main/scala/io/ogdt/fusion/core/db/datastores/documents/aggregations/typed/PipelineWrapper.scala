package io.ogdt.fusion.core.db.datastores.documents.aggregations.typed

import reactivemongo.api.bson.collection.BSONCollection

trait PipelineWrapper {
    def pipeline(col: BSONCollection): Pipeline
}
