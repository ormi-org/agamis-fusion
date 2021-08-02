package io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions

final case class PermissionNotFoundException(
    private val message: String = "Specified permission couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)