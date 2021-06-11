package io.ogdt.fusion.core.db.models.sql.exceptions.applications

final case class InvalidApplicationStatus(
    private val cause: Throwable = None.orNull,
    private val message: String = None.orNull
) extends Exception(message, cause)

object InvalidApplicationStatus {

    def apply(
        message: String,
        cause: Throwable
    ): InvalidApplicationStatus = {
        InvalidApplicationStatus(cause, message)
    }

    def apply(
        cause: Throwable
    ): InvalidApplicationStatus = {
        InvalidApplicationStatus(cause)
    }

    def apply(
        message: String
    ): InvalidApplicationStatus = {
        InvalidApplicationStatus(null, message)
    }
}