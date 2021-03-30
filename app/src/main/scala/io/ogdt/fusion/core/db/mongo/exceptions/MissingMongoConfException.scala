package io.ogdt.fusion.core.db.mongo.exceptions

import java.lang.Exception

final case class MissingMongoConfException(
    private val message: String = "", 
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
