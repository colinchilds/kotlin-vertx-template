package dev.cchilds.controllers

import dev.cchilds.repositories.InventoryRepo
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.impl.ClusterSerializable
import me.koddle.annotations.Body

class InventoryController(private val inventoryRepo: InventoryRepo) : BaseController() {

    suspend fun get(id: String?): ClusterSerializable {
        return if (id != null) inventoryRepo.find(id) else inventoryRepo.all()
    }

    suspend fun post(@Body body:JsonObject): JsonObject {
        return inventoryRepo.insert(body)
    }

    suspend fun patch(id: String, @Body body:JsonObject): JsonObject {
        return da.getTransaction { conn ->
            val fromDb = inventoryRepo.find(id, conn)
            fromDb.mergeIn(body)
            body.forEach { (key, value) -> if (value == null) fromDb.remove(key) }
            inventoryRepo.update(id, fromDb, conn)
        }
    }

    suspend fun delete(id: String) {
        inventoryRepo.delete(id)
    }
}