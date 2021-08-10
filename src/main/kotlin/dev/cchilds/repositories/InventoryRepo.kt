package dev.cchilds.repositories

import me.koddle.repositories.Repository
import me.koddle.tools.DatabaseAccess

class InventoryRepo(da: DatabaseAccess) : Repository("inventories", da) {

}