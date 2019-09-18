package dev.cchilds.tools

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.Shareable
import io.vertx.core.shareddata.impl.ClusterSerializable
import java.time.Instant
import java.util.stream.Stream

fun immutable(obj: JsonObject): ImmutableJsonObject =
    ImmutableJsonObject(obj)

fun immutable(arr: JsonArray): ImmutableJsonArray =
    ImmutableJsonArray(arr)

fun JsonObject.toImmutable(): ImmutableJsonObject =
    immutable(this)

fun JsonArray.toImmutable(): ImmutableJsonArray =
    immutable(this)

sealed class ImmutableJson<T>:
        Iterable<T>,
        ClusterSerializable,
        Shareable {
    abstract fun encode(): String
    abstract fun encodePrettily(): String
    abstract val isEmpty: Boolean
    abstract fun size(): Int
    abstract fun toBuffer(): Buffer
    abstract fun stream(): Stream<T>

    protected fun makeImmutable(obj: Any?) =
        when (obj) {
            is JsonObject -> ImmutableJsonObject(obj, false)
            is JsonArray -> ImmutableJsonArray(obj, false)
            else -> obj
        }
}

class ImmutableJsonObject: ImmutableJson<Pair<String, Any?>> {

    companion object {
        fun mapFrom(obj: Any): ImmutableJsonObject =
            immutable(JsonObject.mapFrom(obj))
    }

    private val obj: JsonObject

    constructor(obj: JsonObject): super() {
        this.obj = obj.copy()
    }

    internal constructor(obj: JsonObject, copy: Boolean): super() {
        this.obj = if (copy) obj.copy() else obj
    }

    fun containsKey(key: String): Boolean =
        obj.containsKey(key)

    override fun copy(): ImmutableJsonObject =
        immutable(obj.copy())

    fun copy(copyFunc: JsonObject.() -> Unit): ImmutableJsonObject =
        immutable(obj.copy().apply(copyFunc))

    override fun encode(): String =
        obj.encode()

    override fun encodePrettily(): String =
        obj.encodePrettily()

    override fun equals(other: Any?): Boolean =
        obj.equals(other)

    fun fieldNames(): Set<String> =
        obj.fieldNames()

    fun getBinary(key: String): ByteArray? =
        obj.getBinary(key)

    fun getBinary(key: String, def: ByteArray?): ByteArray? =
        obj.getBinary(key, def)

    fun getBoolean(key: String): Boolean? =
        obj.getBoolean(key)

    fun getBoolean(key: String, def: Boolean?): Boolean? =
        obj.getBoolean(key, def)

    fun getDouble(key: String): Double? =
        obj.getDouble(key)

    fun getDouble(key: String, def: Double?): Double? =
        obj.getDouble(key, def)

    fun getFloat(key: String): Float? =
        obj.getFloat(key)

    fun getFloat(key: String, def: Float?): Float? =
        obj.getFloat(key, def)

    fun getInstant(key: String): Instant? =
        obj.getInstant(key)

    fun getInstant(key: String, def: Instant?): Instant? =
        obj.getInstant(key, def)

    fun getInteger(key: String): Int? =
        obj.getInteger(key)

    fun getInteger(key: String, def: Int?): Int? =
        obj.getInteger(key, def)

    fun getJsonArray(key: String): ImmutableJsonArray? =
        obj.getJsonArray(key)?.let { immutable(it) }

    fun getJsonArray(key: String, def: JsonArray?): ImmutableJsonArray? =
        obj.getJsonArray(key, def)?.let { immutable(it) }

    fun getJsonObject(key: String): ImmutableJsonObject? =
        obj.getJsonObject(key)?.let { immutable(it) }

    fun getJsonObject(key: String, def: JsonObject?): ImmutableJsonObject? =
        obj.getJsonObject(key, def)?.let { immutable(it) }

    fun getLong(key: String): Long? =
        obj.getLong(key)

    fun getLong(key: String, def: Long?): Long? =
        obj.getLong(key, def)

    val map: Map<String, Any?>
        get() = obj.map

    fun getString(key: String): String? =
        obj.getString(key)

    fun getString(key: String, def: String?): String? =
        obj.getString(key, def)

    fun getValue(key: String): Any? =
        makeImmutable(obj.getValue(key))

    fun getValue(key: String, def: Any?): Any? =
        makeImmutable(obj.getValue(key, def))

    override fun hashCode(): Int =
        obj.hashCode()

    override val isEmpty: Boolean
        get() = obj.isEmpty

    override fun iterator(): Iterator<Pair<String, Any?>> {
        val objIterator = obj.iterator()
        return object : Iterator<Pair<String, Any?>> {
            override fun hasNext(): Boolean =
                objIterator.hasNext()

            override fun next(): Pair<String, Any?> =
                with (objIterator.next()) {
                    key to makeImmutable(value)
                }
        }
    }

    fun <T: Any> mapTo(type: Class<T>): T =
        obj.mapTo(type)

    inline fun <reified T: Any> mapTo(): T =
        mapTo(T::class.java)

    override fun readFromBuffer(pos: Int, buffer: Buffer): Int =
        obj.readFromBuffer(pos, buffer)

    override fun size(): Int =
        obj.size()

    override fun stream(): Stream<Pair<String, Any?>> =
        obj.stream()
            .map { (key, value) -> key to makeImmutable(value) }

    override fun toBuffer(): Buffer =
        obj.toBuffer()

    override fun toString(): String =
        obj.toString()

    override fun writeToBuffer(buffer: Buffer): Unit =
        obj.writeToBuffer(buffer)

    fun toJsonObject(): JsonObject =
        obj.copy()

    operator fun plus(other: JsonObject): ImmutableJsonObject =
        with(obj.copy()){
            other.forEach { (key, value) -> put(key, value) }
            ImmutableJsonObject(this, false)
        }

    operator fun plus(pair: Pair<String, *>): ImmutableJsonObject =
        with(obj.copy()) {
            put(pair.first, pair.second)
            ImmutableJsonObject(this, false)
        }

    operator fun plus(other: ImmutableJsonObject): ImmutableJsonObject =
        with(obj.copy()) {
            other.obj.forEach { (key, value) -> put(key, value) }
            ImmutableJsonObject(this, false)
        }

    operator fun minus(key: String): ImmutableJsonObject =
        with(obj.copy()) {
            remove(key)
            ImmutableJsonObject(this, false)
        }

    operator fun minus(keys: Collection<String>): ImmutableJsonObject =
        with(obj.copy()) {
            keys.forEach { remove(it) }
            ImmutableJsonObject(this, false)
        }
}

class ImmutableJsonArray: ImmutableJson<Any?> {

    companion object { }

    private val arr: JsonArray

    constructor(arr: JsonArray): super() {
        this.arr = arr.copy()
    }

    internal constructor(arr: JsonArray, copy: Boolean): super() {
        this.arr = if (copy) arr.copy() else arr
    }

    fun contains(value: Any?): Boolean =
        arr.contains(value)

    override fun copy(): ImmutableJsonArray =
        immutable(arr.copy())

    fun copy(copyFunc: JsonArray.() -> Unit): ImmutableJsonArray =
        immutable(arr.copy().apply(copyFunc))

    override fun encode(): String =
        arr.encode()

    override fun encodePrettily(): String =
        arr.encodePrettily()

    override fun equals(other: Any?): Boolean =
        arr.equals(other)

    fun getBinary(pos: Int): ByteArray? =
        arr.getBinary(pos)

    fun getBoolean(pos: Int): Boolean? =
        arr.getBoolean(pos)

    fun getDouble(pos: Int): Double? =
        arr.getDouble(pos)

    fun getFloat(pos: Int): Float? =
        arr.getFloat(pos)

    fun getInstant(pos: Int): Instant? =
        arr.getInstant(pos)

    fun getInteger(pos: Int): Int? =
        arr.getInteger(pos)

    fun getJsonArray(pos: Int): ImmutableJsonArray? =
        arr.getJsonArray(pos)?.let { immutable(it) }

    fun getJsonObject(pos: Int): ImmutableJsonObject? =
        arr.getJsonObject(pos)?.let { immutable(it) }

    val list: List<Any?>
        get() = arr.list

    fun getLong(pos: Int): Long? =
        arr.getLong(pos)

    fun getString(pos: Int): String? =
        arr.getString(pos)

    fun getValue(pos: Int): Any? =
        makeImmutable(arr.getValue(pos))

    override fun hashCode(): Int =
        arr.hashCode()

    fun hasNull(pos: Int): Boolean =
        arr.hasNull(pos)

    override val isEmpty: Boolean
        get() = arr.isEmpty

    override fun iterator(): Iterator<Any?> {
        val arrIterator = arr.iterator()
        return object : Iterator<Any?> {
            override fun hasNext(): Boolean =
                arrIterator.hasNext()

            override fun next(): Any? =
                with (arrIterator.next()) {
                    makeImmutable(this)
                }
        }
    }

    override fun readFromBuffer(pos: Int, buffer: Buffer): Int =
        arr.readFromBuffer(pos, buffer)

    override fun size(): Int =
        arr.size()

    override fun stream(): Stream<Any?> =
        arr.stream().map { makeImmutable(it) }

    override fun toBuffer(): Buffer =
        arr.toBuffer()

    override fun toString(): String =
        arr.toString()

    override fun writeToBuffer(buffer: Buffer): Unit =
        arr.writeToBuffer(buffer)

    fun toJsonArray(): JsonArray =
        arr.copy()

    operator fun plus(other: JsonArray): ImmutableJsonArray =
        with(arr.copy()) {
            addAll(other)
            ImmutableJsonArray(this, false)
        }

    operator fun plus(item: Any?): ImmutableJsonArray =
        with(arr.copy()) {
            add(item)
            ImmutableJsonArray(this, false)
        }

    operator fun plus(other: ImmutableJsonArray): ImmutableJsonArray =
        with(arr.copy()) {
            addAll(other.arr)
            ImmutableJsonArray(this, false)
        }

    operator fun minus(other: JsonArray): ImmutableJsonArray =
        with(arr.copy()) {
            other.forEach { remove(it) }
            ImmutableJsonArray(this, false)
        }

    operator fun minus(item: Any?): ImmutableJsonArray =
        with(arr.copy()) {
            remove(item)
            ImmutableJsonArray(this, false)
        }

    operator fun minus(index: Int): ImmutableJsonArray =
        with(arr.copy()) {
            remove(index)
            ImmutableJsonArray(this, false)
        }

    operator fun minus(other: ImmutableJsonArray): ImmutableJsonArray =
        with(arr.copy()) {
            other.arr.forEach { remove(it) }
            ImmutableJsonArray(this, false)
        }
}
