package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.organizationtypes

final case class OrganizationtypeQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occurred while executing organization type SQL query"
) extends Exception(message, cause)

object OrganizationtypeQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): OrganizationtypeQueryExecutionException = {
        new OrganizationtypeQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): OrganizationtypeQueryExecutionException = {
        new OrganizationtypeQueryExecutionException(cause)
    }
}