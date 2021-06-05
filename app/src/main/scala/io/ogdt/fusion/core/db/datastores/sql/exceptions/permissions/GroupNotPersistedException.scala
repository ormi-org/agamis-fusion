package io.ogdt.fusion.core.db.datastores.sql.exceptions.permissions

final case class PermissionNotPersistedException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Failed to persist permission"
) extends Exception(message, cause)

object PermissionNotPersistedException {

    def apply(
        message: String,
        cause: Throwable
    ): PermissionNotPersistedException = {
        PermissionNotPersistedException(cause, message)
    }

    def apply(
        cause: Throwable
    ): PermissionNotPersistedException = {
        PermissionNotPersistedException(cause)
    }

    def apply(
        message: String
    ): PermissionNotPersistedException = {
        PermissionNotPersistedException(null, message)
    }
}