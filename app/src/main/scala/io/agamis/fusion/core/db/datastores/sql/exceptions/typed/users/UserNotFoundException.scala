package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.users

final case class UserNotFoundException(
    private val message: String = "Specified user couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)