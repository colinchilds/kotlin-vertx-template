package dev.cchilds.controllers

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.tools.DatabaseAccess
import me.koddle.tools.RequestHelper
import org.koin.core.KoinComponent
import org.koin.core.inject

open class BaseController : KoinComponent {
    protected val requestHelper: RequestHelper by inject()
    protected val da: DatabaseAccess by inject()
    protected val vertx: Vertx by inject()
    protected val config: JsonObject = runBlocking { Config.config(vertx) }
    protected var webClient: WebClient

    constructor() {
        webClient = WebClient.create(vertx);
    }
}