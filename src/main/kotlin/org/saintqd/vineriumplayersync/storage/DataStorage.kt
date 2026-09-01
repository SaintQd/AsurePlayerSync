package org.saintqd.vineriumplayersync.storage

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

interface DataStorage {

    fun save()

    fun savePlayerData(player : Player)

    fun loadPlayerData(player : Player)

    fun removePlayerData(player: Player)

    fun removePlayerData(uuid: UUID)

    fun hasPlayerData(player : Player) : Boolean

    fun hasPlayerData(uuid: UUID) : Boolean

    fun getPlayerInventory(player : Player) : HashMap<Int, ItemStack>

    fun getPlayerInventory(uuid : UUID) : HashMap<Int, ItemStack>
}