package io.ogdt.fusion.core.db.datastores.sql.exceptions.organizationtypes

final case class OrganizationtypeQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing organization type SQL query"
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