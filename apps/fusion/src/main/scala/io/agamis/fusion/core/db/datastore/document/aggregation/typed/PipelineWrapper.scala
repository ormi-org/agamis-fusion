package io.agamis.fusion.core.db.datastore.document.aggregation.typed

import reactivemongo.api.bson.collection.BSONCollection

trait PipelineWrapper {
    def pipeline(col: BSONCollection): Pipeline
}
