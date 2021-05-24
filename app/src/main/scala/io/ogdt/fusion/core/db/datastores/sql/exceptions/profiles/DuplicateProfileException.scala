package io.ogdt.fusion.core.db.datastores.sql.exceptions.profiles

final case class DuplicateProfileException(
    private val message: String = "Specified profile has duplicates",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)