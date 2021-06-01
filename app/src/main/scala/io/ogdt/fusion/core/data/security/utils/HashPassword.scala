package io.ogdt.fusion.core.data.security.utils

import at.favre.lib.crypto.bcrypt.BCrypt

object HashPassword {
    def hashPassword(password: String): String = {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    def verifyPassword(password: String): BCrypt.Result = {
        return BCrypt.verifyer().verify(password.toCharArray(), this.hashPassword(password))
    }

}