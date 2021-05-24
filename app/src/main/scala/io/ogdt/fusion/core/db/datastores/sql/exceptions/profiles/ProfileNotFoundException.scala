package io.ogdt.fusion.core.db.datastores.sql.exceptions.profiles

final case class ProfileNotFoundException(
    private val message: String = "Specified profile couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)