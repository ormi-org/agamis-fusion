package io.agamis.fusion.core.db.datastore.document.aggregation.typed.file

import io.agamis.fusion.core.db.datastore.document.aggregation.typed.Pipeline
import io.agamis.fusion.core.db.datastore.document.aggregation.typed.PipelineWrapper
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.BSONObjectID
import reactivemongo.api.bson.BSONString
import reactivemongo.api.bson.collection.BSONCollection

object GetFileChildrenFromId extends PipelineWrapper {

    class GetFileChildrenFromIdPipeline(
        protected val _collection: BSONCollection
    ) extends Pipeline {

        private var _id = ""

        def setId(id: String): GetFileChildrenFromIdPipeline = {
            _id = id
            this
        }

        private var _pathPrefix = "/"

        def setPathPrefix(path: String): GetFileChildrenFromIdPipeline = {
            _pathPrefix = path
            this
        }

        def get: List[_collection.AggregationFramework.PipelineOperator] = {

            import _collection.AggregationFramework.{
                GraphLookup,
                Match,
                Project,
                Unwind
            }

            List(
              Match(BSONDocument("_id" -> BSONObjectID.parse(_id).get)),
              GraphLookup(
                from = _collection.name,
                startWith = BSONString("$_id"),
                connectFromField = "_id",
                connectToField = "parent",
                as = "children",
                maxDepth = Some(0)
              ),
              Unwind(field = "children"),
              Project(
                BSONDocument(
                  "_id"       -> "$children._id",
                  "name"      -> "$children.name",
                  "type"      -> "$children.type",
                  "parent"    -> "$children.parent",
                  "chunkList" -> "$children.chunkList",
                  "metadata"  -> "$children.metadata",
                  "versioned" -> "$children.versioned",
                  "acl"       -> "$children.acl",
                  "owner"     -> "$children.owner",
                  "path" -> BSONDocument(
                    "$concat" -> List(
                      _pathPrefix,
                      "/",
                      "$children.name"
                    )
                  )
                )
              )
            )
        }
    }

    override def pipeline(col: BSONCollection): GetFileChildrenFromIdPipeline =
        new GetFileChildrenFromIdPipeline(col)
}
