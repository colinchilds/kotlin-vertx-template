package dev.cchilds.service

import dev.cchilds.repositories.InventoryRepo
import dev.cchilds.security.PubSecJWTManager
import dev.cchilds.verticles.HttpVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.service.buildAutoModule
import me.koddle.tools.DatabaseAccess
import me.koddle.tools.RequestHelper
import me.koddle.tools.VertxRequestHelper
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun main() {
    start()
}

fun start(overrideModule: Module? = null) {
    val vertx = Vertx.vertx()
    val config = Config.config(vertx)

    val module = module(override = true) {
        single { vertx }
        single { PubSecJWTManager(config, vertx) }
        single<RequestHelper> { VertxRequestHelper(get()) }
        single { DatabaseAccess(config, vertx) }
        single { InventoryRepo("public") }
    }
    startKoin {
        modules(buildAutoModule(HttpVerticle::class.java))
        modules(module)
        overrideModule?.let {
            modules(it)
        }
    }

    runBlocking {
        vertx.deployVerticleAwait(HttpVerticle())
    }
}