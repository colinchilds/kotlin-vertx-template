package dev.cchilds.controllers

import dev.cchilds.models.User
import dev.cchilds.security.PubSecJWTManager
import io.vertx.core.http.Cookie
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import me.koddle.annotations.Body
import me.koddle.exceptions.AuthorizationException

class LoginController(val jwtManager: PubSecJWTManager) : BaseController() {

    fun post(context: RoutingContext, @Body("username") username: String, @Body("password") password: String): String {
        if (username == "bob" && password == "secret") {
            val token = jwtManager.generateToken(json {
                obj("roles" to array(User.Role.ADMIN))
            })
            val cookie = Cookie.cookie("identityToken", token).setHttpOnly(true)
            // cookie.setSecure(true) You'll want to use this on HTTPS
            context.response().addCookie(cookie)
            return token
        } else {
            throw AuthorizationException()
        }
    }
}