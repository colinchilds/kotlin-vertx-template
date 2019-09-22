package dev.cchilds.tools

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jwt.JWTOptions
import java.time.LocalDateTime
import java.time.ZoneOffset

class JWTHelper(val config: JsonObject, val vertx: Vertx) {
    val EXPIRATION_MILLIS = 1000 * 60 * 60 * 30

    val authProvider = JWTAuth.create(vertx, JWTAuthOptions()
        .addPubSecKey(
            PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setPublicKey(config.getString("JWT_PUB_KEY"))
                .setSecretKey(config.getString("JWT_PRIVATE_KEY"))
                .setSymmetric(true)))

    fun generateToken(json: JsonObject): String {
        json.put("created", getCurrentUTCMillis())
        return authProvider.generateToken(json, JWTOptions())
    }

    fun isTokenExpired(created: Long): Boolean {
        return getCurrentUTCMillis() - created > EXPIRATION_MILLIS
    }

    private fun getCurrentUTCMillis(): Long {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        return now.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!!
    }
}