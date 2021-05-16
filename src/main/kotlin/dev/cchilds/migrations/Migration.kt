package dev.cchilds.migrations

import io.vertx.core.Vertx
import kotlinx.coroutines.runBlocking
import me.koddle.config.Config
import me.koddle.migrations.migrate

fun main() {
    // Create the Flyway instance and point it to the database
    val vertx = Vertx.vertx()
    val dbConfig = runBlocking { Config.config(vertx) }
    migrate(dbConfig)
    vertx.close()
}
