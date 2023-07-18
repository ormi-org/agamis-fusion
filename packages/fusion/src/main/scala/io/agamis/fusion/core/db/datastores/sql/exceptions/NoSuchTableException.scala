package io.agamis.fusion.core.db.datastores.sql.exceptions

final case class NoSuchTableException(
    private val message: String = "Table does not exist",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
