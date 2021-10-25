package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.languages

final case class DuplicateLanguageException(
    private val message: String = "Specified language has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)

