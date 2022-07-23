package io.agamis.fusion.core.db.datastores.sql.common

object Placeholder extends Enumeration {
  type Value = String
  val SCHEMA: Value = "$SCHEMA"
  val WHERE_STATEMENT: Value = "$WHERE_STATEMENT"
  val ORDER_BY_STATEMENT: Value = "$ORDER_BY_STATEMENT"
  val PAGINATION: Value = "$PAGINATION"
}