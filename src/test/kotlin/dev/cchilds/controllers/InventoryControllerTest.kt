package dev.cchilds.controllers

import dev.cchilds.models.InventoryItem
import io.vertx.kotlin.coroutines.await
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
            dbAccess.getConnection { conn -> conn.preparedQuery("TRUNCATE test.inventories").execute().await() }
        }
    }

    @Test
    fun testInvalidPosts() = runBlocking {
        var response = webClient.postAbs("$baseURL/api/inventory").sendJson(testItem).await()
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.postAbs("$baseURL/api/inventory").setUserToken().sendJson(testItem).await()
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        val badTestItem = testItem.copy().remove("name")
        response = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJson(badTestItem).await()
        assertAll("Item missing required name",
            { Assertions.assertEquals(HTTPStatusCode.BAD_REQUEST.value(), response.statusCode()) }
        )
    }

    @Test
    fun testInvalidPatches() = runBlocking {
        val postResponse = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJson(testItem).await().bodyAsJsonObject()
        val id = postResponse.getString(InventoryItem.ID)

        var response = webClient.patchAbs("$baseURL/api/inventory/$id")
            .sendJson(jObj(InventoryItem.NAME to "anotherName")).await()
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/$id").setUserToken()
            .sendJson(jObj(InventoryItem.NAME to "anotherName")).await()
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/$id").setAdminToken()
            .sendJson(jObj(InventoryItem.COUNT to "A string, not a number")).await()
        assertAll("Bad parameter type",
            { Assertions.assertEquals(HTTPStatusCode.BAD_REQUEST.value(), response.statusCode()) }
        )

        response = webClient.patchAbs("$baseURL/api/inventory/123abc").setAdminToken()
            .sendJson(jObj(InventoryItem.COUNT to 42)).await()
        assertAll("Patching ID that does not exist",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

    @Test
    fun testInvalidDeletes() = runBlocking {
        val postResponse = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJson(testItem).await().bodyAsJsonObject()
        val id = postResponse.getString(InventoryItem.ID)

        var response = webClient.deleteAbs("$baseURL/api/inventory/123").send().await()
        assertAll("Need valid auth token",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.deleteAbs("$baseURL/api/inventory/$id").setUserToken().send().await()
        assertAll("Authorized user but needs admin permissions",
            { Assertions.assertEquals(HTTPStatusCode.UNAUTHORIZED.value(), response.statusCode()) }
        )

        response = webClient.deleteAbs("$baseURL/api/inventory/123abc").setAdminToken().send().await()
        assertAll("Deleting ID that does not exist",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

    @Test
    fun testHappyPaths() = runBlocking {
        var getAllBody = webClient.getAbs("$baseURL/api/inventory").send().await().bodyAsJsonArray()
        assertAll("body should be empty",
            { Assertions.assertEquals(jArr(), getAllBody) }
        )

        var body = webClient.postAbs("$baseURL/api/inventory").setAdminToken().sendJson(testItem).await().bodyAsJsonObject()
        val id = body.remove(InventoryItem.ID)
        assertAll("Item should be stored and props should be equal to what we sent",
            { Assertions.assertEquals(testItem, body) }
        )

        body = webClient.getAbs("$baseURL/api/inventory/$id").send().await().bodyAsJsonObject()
        body.remove(InventoryItem.ID)
        assertAll("Getting item from DB should be equal to what we sent",
            { Assertions.assertEquals(testItem, body) }
        )

        getAllBody = webClient.getAbs("$baseURL/api/inventory").send().await().bodyAsJsonArray()
        getAllBody.getJsonObject(0).remove(InventoryItem.ID)
        assertAll("Getting all items from DB should be equal to what we sent",
            { Assertions.assertEquals(1, getAllBody.size()) },
            { Assertions.assertEquals(jArr(testItem), getAllBody) }
        )

        body = webClient.patchAbs("$baseURL/api/inventory/$id").setAdminToken()
            .sendJson(jObj(InventoryItem.NAME to "Another Name")).await().bodyAsJsonObject()
        assertAll("Patched item should have new name",
            { Assertions.assertEquals("Another Name", body.getString(InventoryItem.NAME)) }
        )

        body = webClient.getAbs("$baseURL/api/inventory/$id").send().await().bodyAsJsonObject()
        body.remove(InventoryItem.ID)
        assertAll("Getting item from DB should have new name",
            { Assertions.assertEquals("Another Name", body.getString(InventoryItem.NAME)) }
        )

        var response = webClient.deleteAbs("$baseURL/api/inventory/$id").setAdminToken().send().await()
        assertAll("Deletion should return successfully",
            { Assertions.assertEquals(HTTPStatusCode.OK.value(), response.statusCode()) }
        )

        response = webClient.getAbs("$baseURL/api/inventory/$id").send().await()
        assertAll("Getting item from DB should have new name",
            { Assertions.assertEquals(HTTPStatusCode.NOT_FOUND.value(), response.statusCode()) }
        )
    }

}