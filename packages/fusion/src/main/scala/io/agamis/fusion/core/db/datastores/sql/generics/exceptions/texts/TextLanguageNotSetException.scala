package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.texts

final case class TextLanguageNotSetException(
    private val message: String = "Specified text has no language attribute",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)