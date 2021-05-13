package dev.cchilds.verticles

import dev.cchilds.security.PubSecJWTManager
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import me.koddle.config.Config
import me.koddle.tools.SwaggerMerger
import me.koddle.tools.SwaggerRouterOptions
import me.koddle.tools.route

class HttpVerticle : CoroutineVerticle() {

    override suspend fun start() {
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

        vertx.createHttpServer(HttpServerOptions().setCompressionSupported(true))
            .requestHandler(mainRouter)
            .listen(config.getInteger("http.port", 8080))
    }

}