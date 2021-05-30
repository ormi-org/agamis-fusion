package io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.emails

final case class EmailNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist email"
) extends Exception(message, cause)

object EmailNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): EmailNotPersistedException = {
        EmailNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): EmailNotPersistedException = {
        EmailNotPersistedException(cause)
    }
}