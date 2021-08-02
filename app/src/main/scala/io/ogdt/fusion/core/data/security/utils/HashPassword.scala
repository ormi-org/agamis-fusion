package io.ogdt.fusion.core.data.security.utils

import io.ogdt.fusion.external.http.entities.User

import java.util.UUID

import at.favre.lib.crypto.bcrypt.BCrypt

object HashPassword {

    val username = "user"
        val password = hashPassword("admin")

    def hashPassword(password: String): String = {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    def checkPassword(userInput: String): Boolean = {
        return BCrypt.verifyer().verify(userInput.toCharArray(), password).verified
    }

}