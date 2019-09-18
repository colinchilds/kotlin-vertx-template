package dev.cchilds.controllers

import dev.cchilds.annotations.Body
import dev.cchilds.repositories.InventoryRepo
import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.impl.ClusterSerializable

class InventoryController(val inventoryRepo: InventoryRepo) : BaseController() {

    fun get(id: String?, searchString: String?, limit: Int = 100): Single<*> {
        if (id != null)
            return da.getTransaction { connection ->  inventoryRepo.find(id, connection) }
        else
            return da.getTransaction { connection ->  inventoryRepo.all(connection) }
    }

    fun post(@Body body:JsonObject): JsonObject {
        return JsonObject(mapOf("body" to body))
    }
}