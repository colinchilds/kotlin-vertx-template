package dev.cchilds.service

import dev.cchilds.repositories.InventoryRepo
import dev.cchilds.security.PubSecJWTManager
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.service.buildAutoModule
import me.koddle.tools.*
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    runBlocking {
        Vertx.vertx(getVertxOptions()).deployVerticle(MyService()).await()
    }
}

fun getVertxOptions(): VertxOptions {
    val options = VertxOptions()
    options.preferNativeTransport = true

    return options
}

class MyService : CoroutineVerticle() {

    override suspend fun start() {
        configureModules()

        vertx.createHttpServer(getHttpOptions())
            .requestHandler(configureRouter())
            .listen(config.getInteger("http.port", 8080))
        println("Using native transport: ${vertx.isNativeTransportEnabled}")
    }

    private fun getHttpOptions(): HttpServerOptions {
        return HttpServerOptions()
            .setCompressionSupported(true)
            .setTcpFastOpen(true)
            .setTcpCork(true)
            .setTcpQuickAck(true)
            .setReusePort(true)
    }

    private suspend fun configureModules() {
        val config = Config.config(vertx)
        val schema = if ("test" == config.getString("ENVIRONMENT")) "test" else "public"

        val module = module(override = true) {
            single { vertx }
            single { PubSecJWTManager(config, vertx) }
            single<RequestHelper> { VertxRequestHelper(get()) }
            single { DatabaseAccess(config, vertx) }
            single { InventoryRepo(schema) }
        }
        startKoin {
            modules(buildAutoModule(MyService::class.java))
            modules(module)
        }
    }

    private suspend fun configureRouter(): Router {
        val mainRouter = Router.router(vertx)
        val pkg = this.javaClass.`package`.name.substringBeforeLast('.') + ".controllers"
        val swaggerFile = SwaggerMerger.mergeAllInDirectory("swagger") ?: throw RuntimeException("Unable to process Swagger file")
        val staticHandler = StaticHandler.create()
            .setDirectoryListing(false)
            .setIncludeHidden(false)

        val apiRouter = Router.router(vertx)
        val jwtManager = PubSecJWTManager(Config.config(vertx), vertx)
        apiRouter.route(swaggerFile, pkg, SwaggerRouterOptions(authManager = jwtManager))
        mainRouter.mountSubRouter("/api", apiRouter)

        mainRouter.get().handler(staticHandler)
        return mainRouter
    }

}