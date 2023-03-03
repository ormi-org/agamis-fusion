package io.agamis.fusion.core.db.datastores.typed.sql

trait EntityQueryParams {
    def filters: List[_]
    def orderBy: List[(EntityQueryParams.Column, Int)]
    def pagination: Option[EntityQueryParams.Pagination]
}

object EntityQueryParams {
    trait Column {
        def order: Int
        def name: String
    }

    case class Pagination(limit: Int, offset: Int)

    def IN_WHERE_CLAUSE(baseColName: String): String = {
        return s"W_${baseColName}"
    }
}