package dev.cchilds.service

import dev.cchilds.security.PubSecJWTManager
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.koin.buildModulesForPackage
import me.koddle.tools.*
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
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
        val config = Config.config(vertx)
        val jwtManager = PubSecJWTManager(config, vertx)
        val pkg = this.javaClass.`package`.name.substringBeforeLast('.')

        configureModules(pkg, config, jwtManager)

        vertx.createHttpServer(getHttpOptions())
            .requestHandler(configureRouter(pkg, jwtManager))
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

    private fun configureModules(pkg: String, config: JsonObject, jwtManager: PubSecJWTManager) {
        val schema = if ("test" == config.getString("ENVIRONMENT")) "test" else "public"

        val module = module(override = true) {
            single { vertx }
            single(named("config")) { config }
            single { jwtManager }
            single { VertxRequestHelper(get()) }
            single { DatabaseAccess(get(named("config")), get(), schema) }
        }
        startKoin {
            modules(buildModulesForPackage("$pkg"))
            modules(module)
        }
    }

    private fun configureRouter(pkg: String, jwtManager: PubSecJWTManager): Router {
        val mainRouter = Router.router(vertx)
        val openAPIFile = OpenAPIMerger.mergeAllInDirectory("swagger") ?: throw RuntimeException("Unable to process Swagger file")

        val apiRouter = Router.router(vertx)
        apiRouter.route(openAPIFile, "$pkg.controllers", OpenAPIRouterOptions(authManager = jwtManager))
        mainRouter.mountSubRouter("/api", apiRouter)

        val staticHandler = StaticHandler.create()
            .setDirectoryListing(false)
            .setIncludeHidden(false)
        mainRouter.get().handler(staticHandler)

        return mainRouter
    }

}