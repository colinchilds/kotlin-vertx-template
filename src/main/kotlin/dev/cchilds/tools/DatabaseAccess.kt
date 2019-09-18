package dev.cchilds.tools

import dev.cchilds.config.Config
import dev.cchilds.exceptions.ServiceException
import io.reactivex.Single
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.impl.ClusterSerializable
import io.vertx.kotlin.pgclient.pgConnectOptionsOf
import io.vertx.kotlin.sqlclient.poolOptionsOf
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.reactivex.sqlclient.SqlClient
import io.vertx.reactivex.sqlclient.SqlConnection
import kotlin.jvm.functions.Function1

class DatabaseAccess {
    val pool: PgPool

    constructor(config: JsonObject, vertx: Vertx) {
        val dbName = config.getString("SERVICE_DB_NAME")
        val connectOptions = pgConnectOptionsOf(port = config.getInteger("SERVICE_DB_PORT"),
            host = config.getString("SERVICE_DB_HOST"),
            database = dbName,
            user = config.getString("SERVICE_DB_USER"),
            password = config.getString("SERVICE_DB_PASSWORD"),
            properties = mapOf("search_path" to config.getString("schema", "public")))
        val poolOptions = poolOptionsOf(maxSize = 10)
        pool = PgPool.pool(vertx, connectOptions, poolOptions)
    }

    fun <T : ClusterSerializable> getConnection(dbAction: (SqlClient) -> Single<T>): Single<T> {
        return pool.rxGetConnection()
            .flatMap { connection -> dbAction.invoke(connection)
                .doFinally { try { connection.close() } catch (ex: Exception) {} }
            }
    }

    fun <T : ClusterSerializable> getTransaction(dbAction: (SqlClient) -> Single<T>): Single<T> {
        return pool.rxBegin()
            .flatMap { connection -> dbAction.invoke(connection)
                .doOnSuccess { connection.commit() }
                .doOnError { try { connection.rollback() } catch (ex: Exception) {} }
                .doFinally { try { connection.close() } catch (ex: Exception) {} }
            }
    }
}