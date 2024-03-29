package dev.cchilds.security

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.JWTAuthHandler
import me.koddle.exceptions.AuthorizationException
import me.koddle.security.AuthManager
import me.koddle.security.RequiredRoles
import java.time.LocalDateTime
import java.time.ZoneOffset

class PubSecJWTManager(val config: JsonObject, val vertx: Vertx) : AuthManager {

    private val EXPIRATION_MILLIS = 1000 * 60 * 30
    private val authProvider: JWTAuth

    init {
        val keyOptions = PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer(config.getString("JWT_PRIVATE_KEY"))
        val authOptions = JWTAuthOptions()
            .addPubSecKey(keyOptions)
        authProvider = JWTAuth.create(vertx, authOptions)
    }

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

    private fun authenticateUser(requiredRoles: RequiredRoles, principal: JsonObject) {
        val userRoles = principal.getJsonArray("roles", JsonArray())
        val created = principal.getLong("created", 0)
        if (isTokenExpired(created))
            throw AuthorizationException()

        with (requiredRoles) {
            if ((taggedWith("oneOf") && !userRoles.oneOf(rolesIn("oneOf"))) ||
                (taggedWith("anyOf") && !userRoles.anyOf(rolesIn("anyOf"))) ||
                (taggedWith("allOf") && !userRoles.allOf(rolesIn("allOf")))
            )
                throw AuthorizationException()
        }
    }

    override fun addAuthHandlers(route: Route, roles: RequiredRoles) {
        route.handler { context ->
            val token = context.getCookie("identityToken")
            if (token != null && token.value != null)
                context.request().headers().set("authorization", "Bearer ${token.value}")
            context.next()
        }
        .handler(JWTAuthHandler.create(authProvider))
        .handler { context ->
            authenticateUser(roles, context.user().principal())
            context.next()
        }
    }
}