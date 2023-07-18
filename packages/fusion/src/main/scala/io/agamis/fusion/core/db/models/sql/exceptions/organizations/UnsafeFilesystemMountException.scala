package io.agamis.fusion.core.db.models.sql.exceptions.organizations

final case class UnsafeFilesystemMountException(
    private val cause: Throwable = None.orNull,
    private val message: String = None.orNull
) extends Exception(message, cause)

object UnsafeFilesystemMountException {

    def apply(
        message: String,
        cause: Throwable
    ): UnsafeFilesystemMountException = {
        new UnsafeFilesystemMountException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UnsafeFilesystemMountException = {
        new UnsafeFilesystemMountException(cause)
    }

    def apply(
        message: String
    ): UnsafeFilesystemMountException = {
        new UnsafeFilesystemMountException(null, message)
    }

    sealed trait Cause
    case object MUST_BE_MOUNTED_FIRST extends Cause {
        def apply(): UnsafeFilesystemMountException = {
            new UnsafeFilesystemMountException(null, "The new default filesystem must be mounted first")
        }
    }
}