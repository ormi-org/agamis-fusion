package io.agamis.fusion.core.fs.lib.exceptions.tree

case class UnknownException(
                          private val cause: Throwable = None.orNull,
                          private val message: String = "Encountered a unknown tree exception"
                          ) extends Exception(message, cause)

object UnknownException {

  def apply(
             message: String,
             cause: Throwable
           ): UnknownException = {
    new UnknownException(cause, message)
  }

  def apply(
             cause: Throwable
           ): UnknownException = {
    new UnknownException(cause)
  }

  def apply(
             message: String
           ): UnknownException = {
    new UnknownException(null, message)
  }
}