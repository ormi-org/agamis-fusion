package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizations

final case class OrganizationNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist organization"
) extends Exception(message, cause)

object OrganizationNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): OrganizationNotPersistedException = {
        new OrganizationNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): OrganizationNotPersistedException = {
        new OrganizationNotPersistedException(cause)
    }

    def apply(
        message: String
    ): OrganizationNotPersistedException = {
        new OrganizationNotPersistedException(null, message)
    }
}