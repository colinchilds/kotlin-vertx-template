package dev.cchilds.controllers

import me.koddle.config.Config
import dev.cchilds.repositories.InventoryRepo
import me.koddle.service.buildAutoModule
import me.koddle.tools.DatabaseAccess
import me.koddle.tools.JWTHelper
import me.koddle.tools.RequestHelper
import me.koddle.tools.VertxRequestHelper
import dev.cchilds.verticles.HttpVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking
import me.koddle.json.jArr
import me.koddle.json.jObj
import me.koddle.models.User
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.CachingMode

fun Root.setup() {
    val vertx by memoized { Vertx.vertx() }
    val config = Config.config(vertx)
    val webClient by memoized { WebClient.create(vertx) }
    val deployIds by memoized(mode = CachingMode.EACH_GROUP, factory = { mutableListOf<String>() } )
    val jwtHelper by memoized { JWTHelper(config, vertx) }
    val dbAccess by memoized { DatabaseAccess(config, vertx) }

    val module = module(override = true) {
        single { vertx }
        single { jwtHelper }
        single<RequestHelper> { VertxRequestHelper(get()) }
        single { dbAccess }
        single { InventoryRepo("test") }
    }
    startKoin {
        modules(buildAutoModule(HttpVerticle::class.java))
        modules(module)
    }

    runBlocking {
        deployIds.add(vertx.deployVerticleAwait(HttpVerticle()))
    }

}

fun <T> HttpRequest<T>.setUserToken(jwtHelper: JWTHelper): HttpRequest<T> {
    val token = jwtHelper.generateToken(jObj(User.ROLES to jArr(User.Role.USER)))
    this.bearerTokenAuthentication(token)
    return this
}

fun <T> HttpRequest<T>.setAdminToken(jwtHelper: JWTHelper): HttpRequest<T> {
    val token = jwtHelper.generateToken(jObj(User.ROLES to jArr(User.Role.ADMIN)))
    this.bearerTokenAuthentication(token)
    return this
}