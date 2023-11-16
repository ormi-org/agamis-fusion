package io.agamis.fusion.core.db.wrappers.ignite.exceptions

import java.lang.Exception

final case class MissingIgniteConfException(
    private val message: String = "", 
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)