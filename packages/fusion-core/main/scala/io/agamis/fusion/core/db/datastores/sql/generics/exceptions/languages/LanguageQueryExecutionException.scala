package io.agamis.fusion.core.db.datastores.sql.generics.exceptions.languages

final case class LanguageQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occurred while executing language SQL query"
) extends Exception(message, cause)

object LanguageQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): LanguageQueryExecutionException = {
        new LanguageQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): LanguageQueryExecutionException = {
        new LanguageQueryExecutionException(cause)
    }
}