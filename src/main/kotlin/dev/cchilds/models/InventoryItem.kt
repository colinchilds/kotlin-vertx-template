package dev.cchilds.models

class InventoryItem {
    companion object {
        val ID = "id"
        val NAME = "name"
        val RELEASE_DATE = "releaseDate"
        val MANUFACTURER = "manufacturer"
        val COUNT = "count"
    }

    class Manufacturer {
        companion object {
            val NAME = "name"
            val HOME_PAGE = "homePage"
            val PHONE = "phone"
        }
    }
}