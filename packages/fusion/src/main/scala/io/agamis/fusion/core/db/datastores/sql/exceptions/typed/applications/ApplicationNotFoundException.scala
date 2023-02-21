package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.applications

final case class ApplicationNotFoundException(
    private val message: String = "Specified application couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)