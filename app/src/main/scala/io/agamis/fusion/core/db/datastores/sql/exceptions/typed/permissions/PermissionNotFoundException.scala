package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.permissions

final case class PermissionNotFoundException(
    private val message: String = "Specified permission couldn't be found",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)