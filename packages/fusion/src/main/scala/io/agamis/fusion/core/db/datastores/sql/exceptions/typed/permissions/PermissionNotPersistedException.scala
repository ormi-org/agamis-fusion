package io.agamis.fusion.core.db.datastores.sql.exceptions.typed.permissions

final case class PermissionNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist permission"
) extends Exception(message, cause)

object PermissionNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): PermissionNotPersistedException = {
        new PermissionNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): PermissionNotPersistedException = {
        new PermissionNotPersistedException(cause)
    }

    def apply(
        message: String
    ): PermissionNotPersistedException = {
        new PermissionNotPersistedException(null, message)
    }
}