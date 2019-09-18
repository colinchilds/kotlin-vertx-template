package dev.cchilds.service

import dev.cchilds.config.Config
import dev.cchilds.repositories.InventoryRepo
import dev.cchilds.tools.DatabaseAccess
import dev.cchilds.tools.JWTHelper
import dev.cchilds.tools.RequestHelper
import dev.cchilds.tools.VertxRequestHelper
import dev.cchilds.verticles.HttpVerticle
import io.reactivex.plugins.RxJavaPlugins
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.Vertx
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun main() {
    start()
}

fun start(overrideModule: Module? = null) {
    val vertx = Vertx.vertx()
    val config = Config.config(vertx).blockingGet()
    RxJavaPlugins.setComputationSchedulerHandler { s -> RxHelper.scheduler(vertx) }
    RxJavaPlugins.setIoSchedulerHandler { s -> RxHelper.blockingScheduler(vertx) }
    RxJavaPlugins.setNewThreadSchedulerHandler { s -> RxHelper.scheduler(vertx) }

    val module = module(override = true) {
        single { vertx }
        single { JWTHelper(config, vertx) }
        single<RequestHelper> { VertxRequestHelper(get()) }
        single { DatabaseAccess(config, vertx) }
        single { InventoryRepo() }
    }
    startKoin {
        modules(buildAutoModule())
        modules(module)
        overrideModule?.let {
            modules(it)
        }
    }

    RxHelper.deployVerticle(vertx, HttpVerticle())
        .doOnError { err ->
            err.printStackTrace()
        }.subscribe()
}