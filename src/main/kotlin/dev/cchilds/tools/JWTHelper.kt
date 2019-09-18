package dev.cchilds.tools

import dev.cchilds.config.Config
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jwt.JWTOptions
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.auth.jwt.JWTAuth

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