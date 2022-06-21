package io.agamis.fusion.core.db.models.sql.generics.exceptions.languages

final case class MalformedLocalLanguageCodeException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
