package io.ogdt.fusion.core.db.models.sql.exceptions.organizations

final case class UnsafeFilesystemUnmountException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Specified filesystem couldn't be unmount due to safety contraints failure"
) extends Exception(message, cause)

object UnsafeFilesystemUnmountException {

    def apply(
        message: String,
        cause: Throwable
    ): UnsafeFilesystemUnmountException = {
        new UnsafeFilesystemUnmountException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UnsafeFilesystemUnmountException = {
        new UnsafeFilesystemUnmountException(cause)
    }

    def apply(
        message: String
    ): UnsafeFilesystemUnmountException = {
        new UnsafeFilesystemUnmountException(null, message)
    }

    sealed trait Cause
    case object IS_DEFAULT_FS extends Cause {
        def apply(): UnsafeFilesystemUnmountException = {
            new UnsafeFilesystemUnmountException(null, "Default Filesystem can't be safely unmounted")
        }
    }
    case object IS_LICENSE_REPO extends Cause {
        def apply(): UnsafeFilesystemUnmountException = {
            new UnsafeFilesystemUnmountException(null, "Filesystem containing license file(s) can't be safely unmounted")
        }
    }
    case object IS_CONFIGURATION_REPO extends Cause {
        def apply(): UnsafeFilesystemUnmountException = {
            new UnsafeFilesystemUnmountException(null, "Filesystem containing configuration file(s) can't be safely unmounted")
        }
    }
}