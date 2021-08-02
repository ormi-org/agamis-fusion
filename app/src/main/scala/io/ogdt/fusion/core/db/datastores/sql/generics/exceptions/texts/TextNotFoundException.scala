package io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.texts

final case class TextNotFoundException(
    private val message: String = "Specified text couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)