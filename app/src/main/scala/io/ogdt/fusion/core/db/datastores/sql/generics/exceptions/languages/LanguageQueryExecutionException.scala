package io.ogdt.fusion.core.db.datastores.sql.generics.exceptions.languages

final case class LanguageQueryExecutionException(
    private val cause: Throwable = None.orNull,
    private val message: String = "An error occured while executing language SQL query"
) extends Exception(message, cause)

object LanguageQueryExecutionException {

    def apply(
        message: String,
        cause: Throwable
    ): LanguageQueryExecutionException = {
        LanguageQueryExecutionException(cause, message)
    }

    def apply(
        cause: Throwable
    ): LanguageQueryExecutionException = {
        LanguageQueryExecutionException(cause)
    }
}