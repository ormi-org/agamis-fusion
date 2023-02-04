package io.agamis.fusion.core.db.datastores.sql.common

object Pagination {
  type Default = Int
  object Default {
    val Limit: Default = 25
    val Offset: Default = 0
  }
}