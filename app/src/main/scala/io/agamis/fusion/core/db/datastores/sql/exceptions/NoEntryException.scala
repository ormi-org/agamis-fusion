package io.agamis.fusion.core.db.datastores.sql.exceptions

final case class NoEntryException(
    private val message: String = "Table is empty",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
