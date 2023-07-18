package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles

final case class ProfileNotFoundException(
    private val message: String = "Specified profile couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)