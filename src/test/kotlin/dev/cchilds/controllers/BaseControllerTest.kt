package dev.cchilds.controllers

import dev.cchilds.config.Config
import dev.cchilds.repositories.InventoryRepo
import dev.cchilds.service.buildAutoModule
import dev.cchilds.tools.DatabaseAccess
import dev.cchilds.tools.JWTHelper
import dev.cchilds.tools.RequestHelper
import dev.cchilds.tools.VertxRequestHelper
import dev.cchilds.verticles.HttpVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.deployVerticleAwait
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.CachingMode

fun Root.setup() {
    val vertx by memoized { Vertx.vertx() }
    val config = Config.config(vertx)
    val webClient by memoized { WebClient.create(vertx) }
    val deployIds by memoized(mode = CachingMode.EACH_GROUP, factory = { mutableListOf<String>() } )

    val module = module(override = true) {
        single { vertx }
        single { JWTHelper(config, vertx) }
        single<RequestHelper> { VertxRequestHelper(get()) }
        single { DatabaseAccess(config, vertx) }
        single { InventoryRepo("test") }
    }
    startKoin {
        modules(buildAutoModule())
        modules(module)
    }

    runBlocking {
        deployIds.add(vertx.deployVerticleAwait(HttpVerticle()))
    }

}