package io.ogdt.fusion.core.db.datastores.sql.exceptions.users

final case class DuplicateUserException(
    private val message: String = "Specified user has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)