package io.agamis.fusion.core.db.datastore.cache.exceptions

final case class DuplicateEntityException(
    private val msg: String =
        "Several entities found where only one was expected",
    private val cause: Throwable = None.orNull
) extends Exception(msg, cause)
