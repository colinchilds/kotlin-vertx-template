package dev.cchilds.repositories

import dev.cchilds.exceptions.ModelNotFoundException
import dev.cchilds.json.jArr
import dev.cchilds.json.jObj
import io.reactivex.Single
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.sqlclient.Row
import io.vertx.reactivex.sqlclient.RowSet
import io.vertx.reactivex.sqlclient.SqlClient
import io.vertx.reactivex.sqlclient.Tuple


abstract class Repository(val table: String) {

    fun all(connection: SqlClient): Single<JsonArray> {
        return connection.rxPreparedQuery("select * from $table").getRows()
    }

    fun find(id: String, connection: SqlClient): Single<JsonObject> {
        return connection.rxPreparedQuery("select * from $table where id = $1", Tuple.of(id)).getRow()
            .map { if (it.isEmpty) throw ModelNotFoundException("No object found with ID", jArr(id)) else it }
    }

    fun findBy(query: String, params: Tuple, connection: SqlClient): Single<JsonArray> {
        return connection.rxPreparedQuery(query, params).getRows()
    }

    fun delete(id: String, connection: SqlClient): Single<JsonObject> {
        return connection.rxPreparedQuery("delete from $table where id = $1", Tuple.of(id)).getRow()
    }

    private fun Single<RowSet>.getRow(): Single<JsonObject> {
        return this.map { rows -> rows.map { row -> jsonRow(row) }.firstOrNull() ?: jObj() }
    }

    private fun Single<RowSet>.getRows(): Single<JsonArray> {
        return this.map { rows -> jArr(rows.map { row -> jsonRow(row) }) }
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
                    val json = jObj(dataJsonString)
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