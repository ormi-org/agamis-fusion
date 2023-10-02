package io.agamis.fusion.core.db.datastore.cache.exceptions

final case class NotFoundException(
    private val msg: String = "Entity not found in cache",
    private val cause: Throwable = None.orNull
) extends Exception(msg, cause)
