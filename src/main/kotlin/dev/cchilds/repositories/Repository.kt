package dev.cchilds.repositories

import dev.cchilds.exceptions.ModelNotFoundException
import dev.cchilds.json.jArr
import dev.cchilds.json.jObj
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.sqlclient.preparedQueryAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple


abstract class Repository(val table: String) {

    suspend fun all(connection: SqlClient): JsonArray {
        return connection.preparedQueryAwait("select * from $table").getRows()
    }

    suspend fun find(id: String, connection: SqlClient): JsonObject {
        val result = connection.preparedQueryAwait("select * from $table where id = $1", Tuple.of(id)).getRow()
        if (result.isEmpty)
            throw ModelNotFoundException("No object found with ID", jArr(id))
        return result
    }

    suspend fun findBy(query: String, params: Tuple, connection: SqlClient): JsonArray {
        return connection.preparedQueryAwait(query, params).getRows()
    }

    suspend fun delete(id: String, connection: SqlClient): JsonObject {
        return connection.preparedQueryAwait("delete from $table where id = $1", Tuple.of(id)).getRow()
    }

    private fun RowSet.getRow(): JsonObject {
        return this.map { row -> jsonRow(row) }.firstOrNull() ?: jObj()
    }

    private fun RowSet.getRows(): JsonArray {
        return jArr(this.map { row -> jsonRow(row) })
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