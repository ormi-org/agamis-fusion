package io.agamis.fusion.core.db.datastore.document.aggregation.typed.file

import io.agamis.fusion.core.db.datastore.document.aggregation.typed.Pipeline
import io.agamis.fusion.core.db.datastore.document.aggregation.typed.PipelineWrapper
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.BSONString
import reactivemongo.api.bson.collection.BSONCollection

object GetFileFromPath extends PipelineWrapper {

    class GetFileFromPathPipeline(protected val _collection: BSONCollection)
        extends Pipeline {

        private var _path = "/"

        def setPath(path: String): GetFileFromPathPipeline = {
            _path = path
            this
        }

        def get: List[_collection.AggregationFramework.PipelineOperator] = {

            import _collection.AggregationFramework._

            List(
              Match(BSONDocument("name" -> "/".r.split(_path).last)),
              GraphLookup(
                from = _collection.name,
                startWith = BSONString("$parent"),
                connectFromField = "parent",
                connectToField = "_id",
                as = "ancestors",
                depthField = Some("order")
              ),
              Unwind(
                path = "ancestors",
                preserveNullAndEmptyArrays = Some(true),
                includeArrayIndex = None
              ),
              Sort(Descending("ancestors.order")),
              Group(BSONString("$_id"))(
                "name"      -> FirstField("name"),
                "type"      -> FirstField("type"),
                "parent"    -> FirstField("parent"),
                "chunkList" -> FirstField("chunkList"),
                "metadata"  -> FirstField("metadata"),
                "versioned" -> FirstField("versioned"),
                "acl"       -> FirstField("acl"),
                "owner"     -> FirstField("owner"),
                "ancestors" -> PushField("ancestors")
              ),
              Project(
                BSONDocument(
                  "name"      -> 1,
                  "type"      -> 1,
                  "parent"    -> 1,
                  "chunkList" -> 1,
                  "metadata"  -> 1,
                  "versioned" -> 1,
                  "acl"       -> 1,
                  "owner"     -> 1,
                  "path" -> BSONDocument(
                    "$concat" -> List(
                      BSONDocument(
                        "$reduce" -> BSONDocument(
                          "input"        -> BSONString("$ancestors"),
                          "initialValue" -> "",
                          "in" -> BSONDocument(
                            "$concat" -> List(
                              BSONString("$$value"),
                              BSONDocument(
                                "$cond" -> Array(
                                  BSONDocument(
                                    "$eq" -> Array("$$value", "/")
                                  ),
                                  BSONString(""),
                                  BSONString("/")
                                )
                              ),
                              BSONString("$$this.name")
                            )
                          )
                        )
                      ),
                      BSONString("/"),
                      BSONString("$name")
                    )
                  )
                )
              ),
              Match(BSONDocument("path" -> _path))
            )
        }
    }

    override def pipeline(col: BSONCollection): GetFileFromPathPipeline =
        new GetFileFromPathPipeline(col)
}
