package io.agamis.fusion.core.db.datastores.typed.sql

import scala.language.implicitConversions

abstract class IncludableFields extends Enumeration {
    protected case class IncludableDetails(i: Int, name: String, placeholder: String) extends super.Val(i, name)
    implicit def valueToIncludableDetails(x: Value): IncludableDetails = x.asInstanceOf[IncludableDetails]
}
