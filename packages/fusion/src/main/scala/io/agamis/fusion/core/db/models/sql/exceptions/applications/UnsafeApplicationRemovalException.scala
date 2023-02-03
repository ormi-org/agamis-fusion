package io.agamis.fusion.core.db.models.sql.exceptions.applications

final case class UnsafeApplicationRemovalException(
    private val cause: Throwable = None.orNull,
    private val message: String = "Specified application couldn't be remove from organization due to safety constraints failure"
) extends Exception(message, cause)

object UnsafeApplicationRemovalException {

    def apply(
        message: String,
        cause: Throwable
    ): UnsafeApplicationRemovalException = {
        new UnsafeApplicationRemovalException(cause, message)
    }

    def apply(
        cause: Throwable
    ): UnsafeApplicationRemovalException = {
        new UnsafeApplicationRemovalException(cause)
    }

    def apply(
        message: String
    ): UnsafeApplicationRemovalException = {
        new UnsafeApplicationRemovalException(null, message)
    }

    sealed trait Cause
    case object IS_ENABLED extends Cause {
        def apply(): UnsafeApplicationRemovalException = {
            new UnsafeApplicationRemovalException(null, "An application must be disabled before being removed from an organization context")
        }
    }
}