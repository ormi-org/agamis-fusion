package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.languages

final case class LanguageNotFoundException(
    private val message: String = "Specified language couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

