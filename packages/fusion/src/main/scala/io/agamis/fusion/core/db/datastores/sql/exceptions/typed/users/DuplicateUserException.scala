package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users

final case class DuplicateUserException(
    private val message: String = "Specified user has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)