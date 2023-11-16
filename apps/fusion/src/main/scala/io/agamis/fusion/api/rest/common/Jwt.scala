package io.agamis.fusion.api.rest.common

import pdi.jwt.algorithms.JwtHmacAlgorithm

import java.util.concurrent.TimeUnit

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import scala.util.{Failure, Success}


object Jwt {

    val privateKey = "secret_key" // TODO à changer
    val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256

    //"""{"user":user","userPermissions": [EDITING,READING,WRITING],"groupPermissions": [], "listgroups": [Group1,Group2]}"""

    // create a refresh token
    def refreshToken(/*user: User /*(db USER)*/*/): String = {
        val claims = JwtClaim(
            content = """{"userId": "123456"}""",
            // Number in second
            expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.SECONDS.toSeconds(172800)), // 2 days
            issuedAt = Some(System.currentTimeMillis() / 1000),
            issuer = Some("ogdt-fusion"),
        ) // mettre le user sous forme de JSON sans le mdp et avec une concaténation complète de ses permissions (user et groupe) + la liste de ses groupes
        JwtSprayJson.encode(claims,privateKey,algorithm)
    }

    // encoding for the creation of token 
    // tokenExpiration in seconds 
    def createToken(/*user: User /*(db USER)*/*/): String = {
        val claims = JwtClaim(
            content = """{"userId": "123456"}""",
            // Number in second
            expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.SECONDS.toSeconds(86400)), // 1 day
            issuedAt = Some(System.currentTimeMillis() / 1000),
            issuer = Some("ogdt-fusion") 
        )// mettre le user sous forme de JSON sans le mdp et avec une concaténation complète de ses permissions (user et groupe) + la liste de ses groupes
        JwtSprayJson.encode(claims,privateKey,algorithm)
    }


    def isTokenExpired(token: String): Boolean = {
        JwtSprayJson.decode(token, privateKey, Seq(JwtAlgorithm.HS256)) match {
            // if expiration is lower than the current time
            case Success(claim) => claim.expiration.getOrElse(0L) < System.currentTimeMillis() / 1000
            case Failure(_) => true
        }
    }

    def isTokenValid(token: String): Boolean = {
        JwtSprayJson.isValid(token, privateKey, Seq(JwtAlgorithm.HS256))
    }

}