package dev.cchilds.controllers

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import me.koddle.tools.DatabaseAccess
import me.koddle.tools.VertxRequestHelper
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

open class BaseController : KoinComponent {
    protected val requestHelper: VertxRequestHelper = get()
    protected val da: DatabaseAccess = get()
    protected val vertx: Vertx = get()
    protected val config: JsonObject = get(named("config"))
    protected var webClient: WebClient = WebClient.create(vertx)
}