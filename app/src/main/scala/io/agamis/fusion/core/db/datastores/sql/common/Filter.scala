package io.agamis.fusion.core.db.datastores.sql.common

object Filter {
  object ComparisonOperator extends Enumeration {
    type Value = String
    val Equal: Value = "eq"
    val GreaterThan: Value = "gt"
    val LowerThan: Value = "lt"
    val NotEqual: Value = "neq"
    object SQL extends Enumeration {
      type Value = String
      val Equal: Value = "="
      val GreaterThan: Value = ">"
      val LowerThan: Value = "<"
      val NotEqual: Value = "<>"
    }
  }
  object OrderingOperators extends Enumeration {
    type Value = Int
    val Ascending: Value = 1
    val Descending: Value = -1
    object SQL extends Enumeration {
      type Value = String
      val Ascending: Value = "ASC"
      val Descending: Value = "DESC"
    }
  }
}