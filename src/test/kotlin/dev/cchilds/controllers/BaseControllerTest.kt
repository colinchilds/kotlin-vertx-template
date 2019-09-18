package dev.cchilds.controllers

import dev.cchilds.config.Config
import dev.cchilds.tools.JWTHelper
import dev.cchilds.tools.VertxRequestHelper
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.client.WebClient
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.CachingMode

fun Root.setup() {
    val vertx by memoized { Vertx.vertx() }
    val config = Config.config(vertx).blockingGet()
    val webClient by memoized { WebClient.create(vertx) }
    val deployIds by memoized(mode = CachingMode.EACH_GROUP, factory = { mutableListOf<String>() } )
    val jwtHelper by memoized { JWTHelper(config, vertx) }

    val module = module {
        single { vertx }
        single { jwtHelper }
        single { VertxRequestHelper(get()) }
        single { DirectoryController(get()) }
        single { InventoryController(get()) }
    }
    startKoin {
        modules(module)
    }

}