package io.agamis.fusion.core.db.datastores.typed.sql

trait EntityFilters {
    def filters: List[_]
    def orderBy: List[(EntityFilters.Column, Int)]
    def pagination: Option[EntityFilters.Pagination]
}

object EntityFilters {
    trait Column {
        def order: Int
        def name: String
    }

    case class Pagination(limit: Int, offset: Int)

    def IN_WHERE_CLAUSE(baseColName: String): String = {
        return s"W_${baseColName}"
    }
}