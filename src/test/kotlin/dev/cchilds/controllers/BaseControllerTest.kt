package dev.cchilds.controllers

import dev.cchilds.models.User
import dev.cchilds.security.PubSecJWTManager
import dev.cchilds.service.MyService
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.json.jArr
import me.koddle.json.jObj
import me.koddle.tools.DatabaseAccess
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseControllerTest {

    lateinit var vertx: Vertx
    lateinit var webClient: WebClient
    lateinit var config: JsonObject
    lateinit var authManager: PubSecJWTManager
    lateinit var dbAccess: DatabaseAccess
    val baseURL = "http://localhost:8080"

    @BeforeAll
    internal fun beforeAll() {
        vertx = Vertx.vertx()
        System.setProperty("ENVIRONMENT", "test")
        webClient = WebClient.create(vertx)

        runBlocking {
            config = Config.config(vertx)
            authManager = PubSecJWTManager(config, vertx)
            dbAccess = DatabaseAccess(config, vertx)
            vertx.deployVerticle(MyService()).await()
        }
    }

    fun <T> HttpRequest<T>.setUserToken(): HttpRequest<T> {
        val token = authManager.generateToken(jObj(User.ROLES to jArr(User.Role.USER)))
        this.bearerTokenAuthentication(token)
        return this
    }

    fun <T> HttpRequest<T>.setAdminToken(): HttpRequest<T> {
        val token = authManager.generateToken(jObj(User.ROLES to jArr(User.Role.ADMIN)))
        this.bearerTokenAuthentication(token)
        return this
    }
}
