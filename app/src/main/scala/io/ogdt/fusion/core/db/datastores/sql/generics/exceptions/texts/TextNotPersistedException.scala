package io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.texts

final case class TextNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist text"
) extends Exception(message, cause)

object TextNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): TextNotPersistedException = {
        new TextNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): TextNotPersistedException = {
        new TextNotPersistedException(cause)
    }
}