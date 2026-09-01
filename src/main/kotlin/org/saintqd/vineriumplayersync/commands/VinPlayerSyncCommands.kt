package org.saintqd.vineriumplayersync.commands

import com.Zrips.CMI.CMI
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.saintqd.vineriumlib.VineriumLib
import org.saintqd.vineriumplayersync.VineriumPlayerSync

class VinPlayerSyncCommands {

    companion object {
        fun setupCommands(plugin: VineriumPlayerSync) {
            val manager = plugin.lifecycleManager
            manager.registerEventHandler(LifecycleEvents.COMMANDS) {
                val commands: Commands = it.registrar()
                commands.register(
                    Commands.literal("vinplayersync")
                        .then(
                            Commands.literal("reload")
                                .requires { predicate: CommandSourceStack ->
                                    predicate.sender.hasPermission("vineriumplayersync.reload")
                                }
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    reloadCommand(
                                        ctx.getSource().sender
                                    )
                                    Command.SINGLE_SUCCESS
                                }
                        )
                        .then(Commands.literal("removedata")
                            .requires { predicate: CommandSourceStack ->
                                predicate.sender.hasPermission("vineriumplayersync.removedata")
                            }
                            .then(Commands.argument("player", StringArgumentType.string())
                                .suggests { _, builder ->
                                    val partName = builder.remaining
                                    Bukkit.getOnlinePlayers().forEach { player ->
                                        if (player.name.lowercase().startsWith(partName.lowercase()))
                                            builder.suggest(player.name)
                                    }
                                    return@suggests builder.buildFuture()
                                }
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    removePlayerDataCommand(
                                        ctx.getSource().sender,
                                        ctx.getArgument("player", String::class.java)
                                    )
                                    Command.SINGLE_SUCCESS
                                }
                            )
                        )
                        .then(Commands.literal("forcesync")
                            .requires { predicate: CommandSourceStack ->
                                VineriumPlayerSync.inst().cmiEnabled &&
                                predicate.sender.hasPermission("vineriumplayersync.forcesync")
                            }
                            .then(Commands.argument("player", StringArgumentType.string())
                                .suggests { _, builder ->
                                    val partName = builder.remaining
                                    Bukkit.getOnlinePlayers().forEach { player ->
                                        if (player.name.lowercase().startsWith(partName.lowercase()))
                                            builder.suggest(player.name)
                                    }
                                    return@suggests builder.buildFuture()
                                }
                                .executes { ctx: CommandContext<CommandSourceStack> ->
                                    forceSyncCommand(
                                        ctx.getSource().sender,
                                        ctx.getArgument("player", String::class.java)
                                    )
                                    Command.SINGLE_SUCCESS
                                }
                            )
                        )
                        .build(),
                    "Основная команда.",
                    listOf("vps")
                )
            }
        }

        private fun reloadCommand(sender: CommandSender) {
            VineriumPlayerSync.inst().loadData()
            if (sender is Player) sender.sendMessage(
                VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(), "reload_message")
            )
        }

        private fun removePlayerDataCommand(sender: CommandSender, playerName : String) {

            val possiblePlayer = Bukkit.getOfflinePlayer(playerName)
            if (!possiblePlayer.hasPlayedBefore()) {
                sender.sendMessage(
                    VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(),
                        "command_remove_data_offline_player_not_found",playerName))
                return
            }
            val uuid = possiblePlayer.uniqueId

            VineriumPlayerSync.inst().storage?.removePlayerData(uuid)

            sender.sendMessage(
                VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(), "command_remove_data_success",uuid.toString()))
        }

        private fun forceSyncCommand(sender: CommandSender, playerName : String) {

            val possiblePlayer = Bukkit.getOfflinePlayer(playerName)
            if (!possiblePlayer.hasPlayedBefore()) {
                sender.sendMessage(
                    VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(),
                        "command_remove_data_offline_player_not_found",playerName))
                return
            }
            val uuid = possiblePlayer.uniqueId

            val storage = VineriumPlayerSync.inst().storage ?: return

            if (!storage.hasPlayerData(uuid)) {
                sender.sendMessage(
                    VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(),
                        "command_force_sync_no_data",playerName))
                return
            }

            val user = CMI.getInstance().playerManager.getUser(uuid)
            if (user == null) {
                sender.sendMessage(
                    VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(),
                        "command_force_sync_user_not_found",playerName))
                return
            }

            val loadedPlayer = user.getPlayer(true)

            for (slot in 0..40)
                loadedPlayer.inventory.clear(slot)

            val inventory = storage.getPlayerInventory(uuid)
            for (item in inventory) {
                loadedPlayer.inventory.setItem(item.key,item.value)
            }

            user.player = loadedPlayer
            CMI.getInstance().save(loadedPlayer)
            CMI.getInstance().playerManager.saveUser(user)

            storage.removePlayerData(uuid)

            sender.sendMessage(
                VineriumLib.inst().langManager.parseLangString(VineriumPlayerSync.inst(), "command_force_sync_success",uuid.toString()))
        }
    }
}