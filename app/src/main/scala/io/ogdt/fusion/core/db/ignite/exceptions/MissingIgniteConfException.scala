package io.ogdt.fusion.core.db.ignite.exceptions

import java.lang.Exception

final case class MissingIgniteConfException(
    private val message: String = "", 
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)