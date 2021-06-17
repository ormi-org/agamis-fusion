package io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes

final case class OrganizationtypeNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist user"
) extends Exception(message, cause)

object OrganizationtypeNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): OrganizationtypeNotPersistedException = {
        new OrganizationtypeNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): OrganizationtypeNotPersistedException = {
        new OrganizationtypeNotPersistedException(cause)
    }

    def apply(
        message: String
    ): OrganizationtypeNotPersistedException = {
        new OrganizationtypeNotPersistedException(null, message)
    }
}