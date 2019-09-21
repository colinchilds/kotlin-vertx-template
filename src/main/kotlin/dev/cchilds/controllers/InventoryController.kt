package dev.cchilds.controllers

import dev.cchilds.annotations.Body
import dev.cchilds.json.jObj
import dev.cchilds.repositories.InventoryRepo
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.impl.ClusterSerializable

class InventoryController(val inventoryRepo: InventoryRepo) : BaseController() {

    suspend fun get(id: String?, searchString: String?, limit: Int = 100): ClusterSerializable {
        return if (id != null)
            da.getConnection { connection -> inventoryRepo.find(id, connection) }
        else
            da.getConnection { connection -> inventoryRepo.all(connection) }
    }

    fun post(@Body body:JsonObject): JsonObject {
        return jObj("body" to body)
    }
}