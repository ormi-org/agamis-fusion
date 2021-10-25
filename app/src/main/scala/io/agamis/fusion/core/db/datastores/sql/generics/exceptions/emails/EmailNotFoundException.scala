package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.emails

final case class EmailNotFoundException(
    private val message: String = "Specified email couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)