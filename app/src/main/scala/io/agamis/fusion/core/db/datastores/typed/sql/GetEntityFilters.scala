package io.agamis.fusion.core.db.datastores.typed.sql

trait GetEntityFilters {
    def filters: List[_]
    def orderBy: List[(GetEntityFilters.Column, Int)]
    def pagination: Option[GetEntityFilters.Pagination]
    // def ordered: Boolean = orderBy.nonEmpty
}

object GetEntityFilters {
    trait Column {
        def order: Int
        def name: String
    }

    trait Pagination {
        def limit: Int
        def offset: Int
    }
}