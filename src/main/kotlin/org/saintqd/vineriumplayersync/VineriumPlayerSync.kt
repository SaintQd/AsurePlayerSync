package org.saintqd.vineriumplayersync

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.saintqd.vineriumlib.VineriumLib
import org.saintqd.vineriumlib.managers.LangManager
import org.saintqd.vineriumlib.utils.ResourceUtils
import org.saintqd.vineriumlib.utils.VinUtils
import org.saintqd.vineriumplayersync.commands.VinPlayerSyncCommands
import org.saintqd.vineriumplayersync.listeners.PlayerListener
import org.saintqd.vineriumplayersync.storage.DataStorage
import org.saintqd.vineriumplayersync.storage.MySqlStorage
import java.io.File

class VineriumPlayerSync : JavaPlugin() {

    var storage : DataStorage? = null

    var cmiEnabled = false

    companion object {
        private var plugin : VineriumPlayerSync? = null

        fun inst() : VineriumPlayerSync {
            return plugin!!
        }
    }

    override fun onLoad() {
        plugin = this

    }

    override fun onEnable() {
        ResourceUtils.fetchAllResources(this, file)

        loadData()

        when(config.getString("Storage","mysql")!!) {
            "mysql" -> storage = MySqlStorage(this)
        }

        val cmi = Bukkit.getPluginManager().getPlugin("CMI")
        if (cmi != null && cmi.isEnabled) {
            cmiEnabled = true
            VinUtils.sendDebugMessage(0, "CMI found, compatibility features enabled.")
        }

        VinPlayerSyncCommands.setupCommands(this)

        server.pluginManager.registerEvents(PlayerListener(), this)
    }

    override fun onDisable() {
        for (player in Bukkit.getOnlinePlayers()) {
            storage?.savePlayerData(player)
        }
    }

    fun loadData() {
        reloadConfig()

        val selectedLang = config.getString("Language","ru_ru")
        val langLines = VineriumLib.inst().langManager.loadLanguageFile(
            this,
            dataFolder.path + File.separator + "lang" + File.separator + selectedLang + ".yml"
        )
        LangManager.INSTANCE.registerLangLines(langLines)

    }
}