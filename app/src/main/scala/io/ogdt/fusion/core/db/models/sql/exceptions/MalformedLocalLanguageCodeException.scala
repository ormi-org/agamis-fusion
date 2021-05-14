package io.ogdt.fusion.core.db.models.sql.exceptions

final case class MalformedLocalLanguageCodeException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
