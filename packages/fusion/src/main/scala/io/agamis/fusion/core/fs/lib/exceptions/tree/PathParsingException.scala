package io.agamis.fusion.core.fs.lib.exceptions.tree

case class PathParsingException(
                                 private val cause: Throwable = None.orNull,
                                 private val message: String = "Could not parse provided path"
                               ) extends Exception(message, cause)

object PathParsingException {

  def apply(
             message: String,
             cause: Throwable
           ): PathParsingException = {
    new PathParsingException(cause, message)
  }

  def apply(
             cause: Throwable
           ): PathParsingException = {
    new PathParsingException(cause)
  }

  def apply(
             message: String
           ): PathParsingException = {
    new PathParsingException(null, message)
  }
}
