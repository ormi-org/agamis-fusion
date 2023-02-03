package io.agamis.fusion.core.db.datastores.typed.sql

abstract class GetEntityFilters {
    def filters: List[_]
    def orderBy: List[(String, Int)]
    def ordered: Boolean = orderBy.nonEmpty
}