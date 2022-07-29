package io.agamis.fusion.core.db.datastores.sql.common.exceptions

import io.agamis.fusion.core.actors.common.CachePolicy

final case class InvalidCachePolicyException(
    private val message: String = "Provided cache exception is invalid",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

object InvalidCachePolicyException {
  def apply(value: String): InvalidCachePolicyException = {
    InvalidCachePolicyException(s"Provided cache exception '${value}' is invalid. It must be either [${CachePolicy}]")
  }
}
