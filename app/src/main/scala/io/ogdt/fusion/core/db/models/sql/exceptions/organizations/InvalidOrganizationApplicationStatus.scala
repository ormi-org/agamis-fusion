package io.ogdt.fusion.core.db.models.sql.exceptions.organizations

final case class InvalidOrganizationApplicationStatus(
    private val cause: Throwable = None.orNull,
    private val message: String = None.orNull
) extends Exception(message, cause)

object InvalidOrganizationApplicationStatus {

    def apply(
        message: String,
        cause: Throwable
    ): InvalidOrganizationApplicationStatus = {
        InvalidOrganizationApplicationStatus(cause, message)
    }

    def apply(
        cause: Throwable
    ): InvalidOrganizationApplicationStatus = {
        InvalidOrganizationApplicationStatus(cause)
    }

    def apply(
        message: String
    ): InvalidOrganizationApplicationStatus = {
        InvalidOrganizationApplicationStatus(null, message)
    }
}