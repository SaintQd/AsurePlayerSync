package org.saintqd.vineriumplayersync.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.saintqd.vineriumplayersync.VineriumPlayerSync

class PlayerListener : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerJoin(event: PlayerJoinEvent) {

        var loadData = true
        /*if (VineriumPlayerSync.inst().config.getBoolean("ServerConnector.Enabled", true)) {
            val currentServerName = VineriumPlayerSync.inst().config.getString("ServerConnector.ServerName", "")!!.lowercase()
            val possibleServerPermissionList = event.getPlayer().effectivePermissions
                .filter { permission -> permission.permission.startsWith("vineriumplayersync.lastserver.") }
                .map { permission -> permission.permission }.toList()

            if (possibleServerPermissionList.isNotEmpty()) {
                val fullPermission = possibleServerPermissionList.first()
                val serverName = fullPermission.split(".").last()
                if (serverName != currentServerName) {
                    val command = VineriumPlayerSync.inst().config
                        .getString("ServerConnector.Command", "cmi server {1} %player_name%")!!
                        .replace("%player_name%", event.getPlayer().name)
                        .replace("{1}", serverName)

                    possibleServerPermissionList.forEach { permission ->
                        VineriumLib.inst().vaultManager.permissionProvider.playerRemove(null,event.player,permission)
                    }

                    loadData = false

                    Bukkit.getScheduler().runTaskLater(VineriumPlayerSync.inst(), Runnable {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command)
                    }, 20L)
                }
            } else {
                if (!currentServerName.isEmpty()) {
                    val permission = "vineriumplayersync.lastserver.$currentServerName"
                    VineriumLib.inst().vaultManager.permissionProvider
                        .playerAdd(null, event.getPlayer(), permission)
                }
            }
        }*/

        if (loadData) {

            val prevTime = System.currentTimeMillis()

            val storage = VineriumPlayerSync.inst().storage ?: return
            if (!storage.hasPlayerData(event.player)) return

            storage.loadPlayerData(event.player)
            storage.removePlayerData(event.player)

            val time = System.currentTimeMillis()
            val timeDiff = time - prevTime
            if (VineriumPlayerSync.inst().config.getBoolean("Messages.LogLoad",true))
                VineriumPlayerSync.inst().logger.info("Loaded sync data for player ${event.player.name}. ($timeDiff ms)")
        }
    }

    @EventHandler()
    fun onPlayerQuit(event: PlayerQuitEvent) {

        val prevTime = System.currentTimeMillis()

        val storage = VineriumPlayerSync.inst().storage ?: return
        storage.savePlayerData(event.player)

        val time = System.currentTimeMillis()
        val timeDiff = time - prevTime
        if (VineriumPlayerSync.inst().config.getBoolean("Messages.LogSave",true))
            VineriumPlayerSync.inst().logger.info("Saved sync data for player ${event.player.name}. ($timeDiff ms)")
    }
}