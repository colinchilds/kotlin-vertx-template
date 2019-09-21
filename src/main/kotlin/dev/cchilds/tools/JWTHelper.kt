package dev.cchilds.tools

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jwt.JWTOptions

class JWTHelper(val config: JsonObject, val vertx: Vertx) {
    val authProvider = JWTAuth.create(vertx, JWTAuthOptions()
        .addPubSecKey(
            PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setPublicKey(config.getString("JWT_PUB_KEY"))
                .setSecretKey(config.getString("JWT_PRIVATE_KEY"))
                .setSymmetric(true)))

    fun generateToken(json: JsonObject): String {
        return authProvider.generateToken(json, JWTOptions())
    }
}