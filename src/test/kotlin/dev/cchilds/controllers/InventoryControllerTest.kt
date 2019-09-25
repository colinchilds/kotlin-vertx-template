package dev.cchilds.controllers

import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import io.vertx.kotlin.sqlclient.preparedQueryAwait
import kotlinx.coroutines.runBlocking
import me.koddle.exceptions.HTTPStatusCode
import me.koddle.json.jObj
import me.koddle.tools.DatabaseAccess
import me.koddle.tools.JWTHelper
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object InventoryControllerTest : Spek({
    setup()
    val webClient: WebClient by memoized()
    val jwtHelper: JWTHelper by memoized()
    val dbAccess: DatabaseAccess by memoized()

    afterEachTest {
        runBlocking {
            dbAccess.getConnection { conn -> conn.preparedQueryAwait("TRUNCATE test.inventories") }
        }
    }

    group("Inventory Controller Testing") {
        describe("get all inventory objects") {
            context("there is no data") {
                val body = runBlocking {
                    val response = webClient.get(8080, "localhost", "/api/inventory").sendAwait()
                    response.bodyAsJsonArray()
                }
                it("Get should return an empty array") {
                    body.isEmpty `should be` true
                }
            }
        }

        describe("Test post") {
            val testItem = jObj(
                "name" to "widget",
                "manufacturer" to jObj("name" to "manufacturer1" ),
                "releaseDate" to "2016-08-29T09:12:33.001Z"
            )

            context("Check permissions for non logged in user") {
                val response = runBlocking {
                    webClient.post(8080, "localhost", "/api/inventory").sendJsonAwait(testItem)
                }
                it("Post should fail because of missing token") {
                    response.statusCode() `should be equal to` HTTPStatusCode.UNAUTHORIZED.value()
                }
            }

            context("Validate required properties") {
                val response = runBlocking {
                    webClient.post(8080, "localhost", "/api/inventory").setUserToken(jwtHelper).sendJsonAwait(testItem)
                }
                it("Post should fail because body is missing properties") {
                    response.statusCode() `should be equal to` HTTPStatusCode.BAD_REQUEST.value()
                }
            }

            testItem.put("count", 10)

            context("Check non admin permissions") {
                val response = runBlocking {
                    webClient.post(8080, "localhost", "/api/inventory").setUserToken(jwtHelper).sendJsonAwait(testItem)
                }
                it("Post should fail because user is not an admin") {
                    response.statusCode() `should be equal to` HTTPStatusCode.UNAUTHORIZED.value()
                }
            }

            context("Add some data") {
                val response = runBlocking {
                    webClient.post(8080, "localhost", "/api/inventory").setAdminToken(jwtHelper).sendJsonAwait(testItem)
                }
                it("Item should have been created") {
                    response.statusCode() `should be equal to` HTTPStatusCode.OK.value()
                    val body = response.bodyAsJsonObject()
                    val id = body.getString("id")
                    testItem.put("id", id)

                    body `should equal` testItem
                }
            }
        }
    }
})