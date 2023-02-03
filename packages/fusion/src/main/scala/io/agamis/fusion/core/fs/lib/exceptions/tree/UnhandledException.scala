package io.agamis.fusion.core.fs.lib.exceptions.tree

case class UnhandledException(
                                 private val cause: Throwable = None.orNull,
                                 private val message: String = "Encountered a unhandled tree exception"
                                 ) extends Exception(message, cause)

object UnhandledException {

  def apply(
             message: String,
             cause: Throwable
           ): UnhandledException = {
    new UnhandledException(cause, message)
  }

  def apply(
             cause: Throwable
           ): UnhandledException = {
    new UnhandledException(cause)
  }

  def apply(
             message: String
           ): UnhandledException = {
    new UnhandledException(null, message)
  }
}
