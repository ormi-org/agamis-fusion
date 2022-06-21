package io.agamis.fusion.core.db.common

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import java.util.Base64
import io.agamis.fusion.core.db.common.security.exceptions.CryptoHashException

object Security {

    object SecurePasswordHashing {
        private val RandomSource = new SecureRandom()
        private val HashPartSeparator = ":"
        private val DefaultNbrOfHashIter = 2000
        private val SizeOfPasswordSaltInBytes = 32
        private val SizeOfPasswordHashInBytes = 64

        def hashPassword(pwd: String): String = hashPassword(pwd, generateRandomBytes(SizeOfPasswordSaltInBytes))
        def hashPassword(pwd: String, salt: Array[Byte]): String = hashPassword(pwd, salt, DefaultNbrOfHashIter)
        def hashPassword(pwd: String, salt: Array[Byte], nbrOfIterations: Int): String ={
            val hash = pbkdf2(pwd, salt, nbrOfIterations)
            val salt64 = new String(Base64.getUrlEncoder().encode(salt))
            val hash64 = new String(Base64.getUrlEncoder().encode(hash))

            Vector(nbrOfIterations.toString, hash64, salt64).mkString(HashPartSeparator)
        }

        def validatePassword(pwd: String, hash: String): Boolean = {
            def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = {
                var diff = a.length ^ b.length
                for (i <- 0 until math.min(a.length, b.length)) diff |= a(i) ^ b(i)
                return diff == 0
            }

            val hashParts = hash.split(HashPartSeparator)

            if (hashParts.length != 3) throw new CryptoHashException("Got a malformed hash, it should have exactly 3 parts separate by :")
            if (!hashParts(0).forall(_.isDigit)) throw new CryptoHashException("Got a malformed hash iterrations count")

            val nbrOfIterations = hash(0).toInt
            try {
                val hash64 = Base64.getUrlDecoder().decode(hashParts(1))
                val salt64 = Base64.getUrlDecoder().decode(hashParts(2))

                val calculatedHash = pbkdf2(pwd, salt64, nbrOfIterations)

                slowEquals(calculatedHash, hash64)
            } catch {
                case e: IllegalArgumentException => throw e
            }
        }

        private def pbkdf2(password: String, salt: Array[Byte], nbrOfIterations: Int): Array[Byte] = {
            val keySpec = new PBEKeySpec(password.toCharArray(), salt, nbrOfIterations, SizeOfPasswordHashInBytes * 8)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

            keyFactory.generateSecret(keySpec).getEncoded()
        }

        private def generateRandomBytes(lenght: Int): Array[Byte] = {
            val keyData = new Array[Byte](lenght)
            RandomSource.nextBytes(keyData)
            keyData
        }
    }
}