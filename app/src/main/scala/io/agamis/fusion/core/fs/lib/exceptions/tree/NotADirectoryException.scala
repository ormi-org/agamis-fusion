package io.agamis.fusion.core.fs.lib.exceptions.tree

case class NotADirectoryException(
                                   private val message: String = "Expected a Directory but got a File",
                                   private val cause: Throwable = None.orNull
                                 ) extends Exception(message, cause)
