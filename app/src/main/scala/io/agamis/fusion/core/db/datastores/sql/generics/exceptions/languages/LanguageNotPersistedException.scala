package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.languages

final case class LanguageNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist language"
) extends Exception(message, cause)

object LanguageNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): LanguageNotPersistedException = {
        new LanguageNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): LanguageNotPersistedException = {
        new LanguageNotPersistedException(cause)
    }
}
