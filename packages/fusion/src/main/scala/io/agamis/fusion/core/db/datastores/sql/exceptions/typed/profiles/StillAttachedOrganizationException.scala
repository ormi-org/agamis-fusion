package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.profiles

final case class StillAttachedOrganizationException(
    private val message: String = "Specified profile has still attached Organization",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)