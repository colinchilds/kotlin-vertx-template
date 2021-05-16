package dev.cchilds.controllers

import dev.cchilds.models.InventoryItem
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import io.vertx.kotlin.sqlclient.preparedQueryAwait
import kotlinx.coroutines.runBlocking
import me.koddle.exceptions.HTTPStatusCode
import me.koddle.json.jArr
import me.koddle.json.jObj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class InventoryControllerTest : BaseControllerTest() {

    private val testItem = jObj(
        InventoryItem.NAME to "widget",
        InventoryItem.MANUFACTURER to jObj(InventoryItem.Manufacturer.NAME to "manufacturer1"),
        InventoryItem.RELEASE_DATE to "2016-08-29T09:12:33.001Z",
        InventoryItem.COUNT to 10
    )

    @BeforeEach
    fun beforeEach() {
        runBlocking {
            dbAccess.getConnection { conn -> conn.preparedQueryAwait("TRUNCATE test.inventories") }
        }
    }

    @Test
    fun testInvalidPosts() = runBlocking {
        var response = webClient.postAbs("$baseURL/api/inventory").sendJsonAwait(testItem)
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.postAbs("$baseURL/api/inventory").setUserToken().sendJsonAwait(testItem)
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        val badTestItem = testItem.copy().remove("name")
        response = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJsonAwait(badTestItem)
        assertAll("Item missing required name",
            { Assertions.assertEquals(HTTPStatusCode.BAD_REQUEST.value(), response.statusCode()) }
        )
    }

    @Test
    fun testInvalidPatches() = runBlocking {
        val postResponse = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJsonAwait(testItem).bodyAsJsonObject()
        val id = postResponse.getString(InventoryItem.ID)

        var response = webClient.patchAbs("$baseURL/api/inventory/$id")
            .sendJsonAwait(jObj(InventoryItem.NAME to "anotherName"))
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/$id").setUserToken()
            .sendJsonAwait(jObj(InventoryItem.NAME to "anotherName"))
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/$id").setAdminToken()
            .sendJsonAwait(jObj(InventoryItem.COUNT to "A string, not a number"))
        assertAll("Bad parameter type",
            { Assertions.assertEquals(HTTPStatusCode.BAD_REQUEST.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/123abc").setAdminToken()
            .sendJsonAwait(jObj(InventoryItem.COUNT to 42))
        assertAll("Patching ID that does not exist",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

    @Test
    fun testInvalidDeletes() = runBlocking {
        val postResponse = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJsonAwait(testItem).bodyAsJsonObject()
        val id = postResponse.getString(InventoryItem.ID)

        var response = webClient.deleteAbs("$baseURL/api/inventory/123").sendAwait()
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.deleteAbs("$baseURL/api/inventory/$id").setUserToken().sendAwait()
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.deleteAbs("$baseURL/api/inventory/123abc").setAdminToken().sendAwait()
        assertAll("Deleting ID that does not exist",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

    @Test
    fun testHappyPaths() = runBlocking {
        var getAllBody = webClient.getAbs("$baseURL/api/inventory").sendAwait().bodyAsJsonArray()
        assertAll("body should be empty",
            { Assertions.assertEquals(jArr(), getAllBody) }
        )

        var body = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJsonAwait(testItem).bodyAsJsonObject()
        val id = body.remove(InventoryItem.ID)
        assertAll("Item should be stored and props should be equal to what we sent",
            { Assertions.assertEquals(testItem, body) }
        )

        body = webClient.getAbs("$baseURL/api/inventory/$id").sendAwait().bodyAsJsonObject()
        body.remove(InventoryItem.ID)
        assertAll("Getting item from DB should be equal to what we sent",
            { Assertions.assertEquals(testItem, body) }
        )

        getAllBody = webClient.getAbs("$baseURL/api/inventory").sendAwait().bodyAsJsonArray()
        getAllBody.getJsonObject(0).remove(InventoryItem.ID)
        assertAll("Getting all items from DB should be equal to what we sent",
            { Assertions.assertEquals(1, getAllBody.size()) },
            { Assertions.assertEquals(jArr(testItem), getAllBody) }
        )

        body = webClient.patchAbs("$baseURL/api/inventory/$id").setAdminToken()
            .sendJsonAwait(jObj(InventoryItem.NAME to "Another Name")).bodyAsJsonObject()
        assertAll("Patched item should have new name",
            { Assertions.assertEquals("Another Name", body.getString(InventoryItem.NAME)) }
        )

        body = webClient.getAbs("$baseURL/api/inventory/$id").sendAwait().bodyAsJsonObject()
        body.remove(InventoryItem.ID)
        assertAll("Getting item from DB should have new name",
            { Assertions.assertEquals("Another Name", body.getString(InventoryItem.NAME)) }
        )

        var response = webClient.deleteAbs("$baseURL/api/inventory/$id").setAdminToken().sendAwait()
        assertAll("Deletion should return successfully",
            { Assertions.assertEquals(HTTPStatusCode.OK.value(), response.statusCode()) }
        )

        response = webClient.getAbs("$baseURL/api/inventory/$id").sendAwait()
        assertAll("Getting item from DB should have new name",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

}