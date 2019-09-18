package dev.cchilds.repositories

import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.sqlclient.Row
import io.vertx.reactivex.sqlclient.SqlClient
import io.vertx.reactivex.sqlclient.Tuple


abstract class Repository(val table: String) {

    fun all(connection: SqlClient): Single<JsonArray> {
        return connection.rxPreparedQuery("select * from $table").map {
            JsonArray(it.map { jsonRow(it) })
        }
    }

    fun find(id: String, connection: SqlClient): Single<JsonObject> {
        return connection.rxPreparedQuery("select * from $table where id = $1", Tuple.of(id)).flatMapObservable {
            Observable.fromIterable(it.asIterable())
        }.map {
            jsonRow(it)
        }.singleOrError()
    }

    fun findBy(query: String, params: Tuple, connection: SqlClient): Single<JsonArray> {
        return connection.rxPreparedQuery(query, params).map {
            JsonArray(it.map { jsonRow(it) })
        }
    }

    fun delete(id: String, connection: SqlClient): Single<JsonObject> {
        return connection.rxPreparedQuery("delete from $table where id = $1", Tuple.of(id)).flatMapObservable {
            Observable.fromIterable(it.asIterable())
        }.map {
            jsonRow(it)
        }.singleOrError()
    }

    private fun jsonRow(row: Row): JsonObject {
        return buildOutJsonRow(row, json { obj() }, 0)
    }

    private fun buildOutJsonRow(dbRow: Row, currentJson: JsonObject, currentColumn: Int): JsonObject {
        return when (dbRow.size()) {
            currentColumn -> currentJson
            else -> {
                val columnName = dbRow.getColumnName(currentColumn)
                val dbRowValue = dbRow.getValue(columnName)
                val newJsonRow: JsonObject = if (columnName == "data") {
                    val dataJsonString = dbRow.getString("data")
                    val json = JsonObject(dataJsonString)
                    json.copy().map.putAll(currentJson.map)
                    json
                } else {
                    currentJson.put(columnName, dbRowValue)
                }
                buildOutJsonRow(dbRow, newJsonRow, currentColumn + 1)
            }
        }
    }
}