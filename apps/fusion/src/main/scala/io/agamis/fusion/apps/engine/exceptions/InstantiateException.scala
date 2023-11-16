package io.agamis.fusion.apps.engine.exceptions

final case class InstantiateException(
    private val msg: String = "An unhandled exception occured while instantiating nodejs script",
    private val cause: Throwable = None.orNull
) extends Exception(msg, cause)
