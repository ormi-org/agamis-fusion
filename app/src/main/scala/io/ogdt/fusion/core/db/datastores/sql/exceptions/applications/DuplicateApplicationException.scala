package io.ogdt.fusion.core.db.datastores.sql.exceptions.applications

final case class DuplicateApplicationException(
    private val message: String = "Specified application has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)