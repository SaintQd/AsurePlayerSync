package org.saintqd.vineriumplayersync.storage

import com.zaxxer.hikari.HikariDataSource
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.saintqd.vineriumlib.utils.SQLUtils
import org.saintqd.vineriumplayersync.VineriumPlayerSync
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.Base64
import java.util.UUID
import kotlin.use

class MySqlStorage(val plugin: Plugin) : DataStorage {

    val dataSource = HikariDataSource()
    val connection: Connection
    //val databasePropertiesTable : String
    val playersTable : String
    val playerInventoriesTable : String
    val playerEnderChestsTable : String

    companion object {

        const val DATABASE_PROPERTIES_TABLE_KEY_NAME = "property_key"
        const val DATABASE_PROPERTIES_TABLE_VALUE_NAME = "property_value"

        const val PLAYERS_TABLE_COLUMN_ID_NAME = "id"
        const val PLAYERS_TABLE_COLUMN_UUID_NAME = "uuid"
        const val PLAYERS_TABLE_COLUMN_NICKNAME_NAME = "nickname"
        const val PLAYERS_TABLE_COLUMN_LEVEL_NAME = "level"
        const val PLAYERS_TABLE_COLUMN_EXPERIENCE_NAME = "experience"

        const val PLAYER_INVENTORIES_TABLE_COLUMN_ID_NAME = "id"
        const val PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME = "player_id"
        const val PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME = "slot"
        const val PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME = "itemstack_base64"

        const val PLAYER_ENDER_CHESTS_TABLE_COLUMN_ID_NAME = "id"
        const val PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME = "player_id"
        const val PLAYER_ENDER_CHESTS_TABLE_COLUMN_SLOT_NAME = "slot"
        const val PLAYER_ENDER_CHESTS_TABLE_COLUMN_ITEMSTACK_BASE64_NAME = "itemstack_base64"

        const val DB_VERSION = 2

    }

    fun setupDataSource(plugin: Plugin) {
        dataSource.jdbcUrl = plugin.config.getString("Database.Url","")
        dataSource.username = plugin.config.getString("Database.Username","")
        dataSource.password = plugin.config.getString("Database.Password","")
    }

    init {
        setupDataSource(plugin)
        connection = dataSource.connection
        var tablePrefix = plugin.config.getString("Database.TablePrefix","")!!
        tablePrefix = if (tablePrefix.isBlank()) "" else tablePrefix + "_"
        //databasePropertiesTable = tablePrefix + "database_properties"
        playersTable = tablePrefix + "players"
        playerInventoriesTable = tablePrefix + "player_inventories"
        playerEnderChestsTable = tablePrefix + "player_ender_chests"

        /*
        var databaseVersion = 1

        if (SQLUtils.checkIfTableExists(connection,databasePropertiesTable)) {
            val expected = hashMapOf(
                Pair(DATABASE_PROPERTIES_TABLE_KEY_NAME,"varchar(100)"),
                Pair(DATABASE_PROPERTIES_TABLE_VALUE_NAME,"varchar(100)")
            )
            if (!SQLUtils.checkIfTableMatchesStructure(connection,databasePropertiesTable,expected)) {
                throw SQLException("JDBC table $databasePropertiesTable does not match expected structure")
            }

            try {
                connection.prepareStatement("select $DATABASE_PROPERTIES_TABLE_VALUE_NAME from $databasePropertiesTable where $DATABASE_PROPERTIES_TABLE_KEY_NAME = ? limit 1").use {
                        statement ->
                    statement.setString(1, "version")

                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            databaseVersion = resultSet.getInt(1)
                        }
                    }
                }
            }
            catch (ex : SQLException) {
                databaseVersion = 1
            }
        }
        else {
            connection.prepareStatement(
                "create table $databasePropertiesTable\n" +
                        "(\n" +
                        "    $DATABASE_PROPERTIES_TABLE_KEY_NAME    varchar(100) primary key,\n" +
                        "    $DATABASE_PROPERTIES_TABLE_VALUE_NAME    varchar(100) not null\n" +
                        ");")
                .use { statement -> statement.executeUpdate() }
            databaseVersion = 1
        }*/

        if (SQLUtils.checkIfTableExists(connection,playersTable)) {

            /*if (databaseVersion < 2) {
                connection.prepareStatement("alter table $playersTable add column $PLAYERS_TABLE_COLUMN_NICKNAME_NAME varchar(36) after $PLAYERS_TABLE_COLUMN_UUID_NAME")
                    .use { statement ->
                        statement.executeUpdate()
                    }
                VineriumPlayerSync.inst().logger.info { "Updated database to version 2: added $PLAYERS_TABLE_COLUMN_NICKNAME_NAME column to table $playersTable." }
            }*/

            val expected = hashMapOf(
                Pair(PLAYERS_TABLE_COLUMN_UUID_NAME,"varchar(36)"),
                Pair(PLAYERS_TABLE_COLUMN_NICKNAME_NAME,"varchar(36)"),
                Pair(PLAYERS_TABLE_COLUMN_LEVEL_NAME,"int unsigned"),
                Pair(PLAYERS_TABLE_COLUMN_EXPERIENCE_NAME,"double unsigned")
            )
            if (!SQLUtils.checkIfTableMatchesStructure(connection,playersTable,expected)) {
                throw SQLException("JDBC table $playersTable does not match expected structure")
            }
        }
        else {
            connection.prepareStatement(
                "create table $playersTable\n" +
                        "(\n" +
                        "    $PLAYERS_TABLE_COLUMN_ID_NAME    int unsigned auto_increment primary key,\n" +
                        "    $PLAYERS_TABLE_COLUMN_UUID_NAME    varchar(36) not null,\n" +
                        "    $PLAYERS_TABLE_COLUMN_NICKNAME_NAME    varchar(36) not null,\n" +
                        "    $PLAYERS_TABLE_COLUMN_LEVEL_NAME    int unsigned not null,\n" +
                        "    $PLAYERS_TABLE_COLUMN_EXPERIENCE_NAME    double unsigned not null,\n" +
                        "    constraint players_uuid_uindex unique ($PLAYERS_TABLE_COLUMN_UUID_NAME)\n" +
                        ");")
                .use { statement -> statement.executeUpdate() }
        }

        if (SQLUtils.checkIfTableExists(connection,playerInventoriesTable)) {
            val expected = hashMapOf(
                Pair(PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME,"int unsigned"),
                Pair(PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME,"int unsigned"),
                Pair(PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME,"text"),
            )
            if (!SQLUtils.checkIfTableMatchesStructure(connection,playerInventoriesTable,expected)) {
                throw SQLException("JDBC table $playerInventoriesTable does not match expected structure")
            }
        }
        else {
            connection.prepareStatement(
                "create table $playerInventoriesTable\n" +
                        "(\n" +
                        "    $PLAYER_INVENTORIES_TABLE_COLUMN_ID_NAME    bigint unsigned auto_increment primary key,\n" +
                        "    $PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME    int unsigned,\n" +
                        "    $PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME    int unsigned,\n" +
                        "    $PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME    text not null,\n" +
                        "    foreign key    ($PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME) references $playersTable($PLAYERS_TABLE_COLUMN_ID_NAME) on delete cascade on update cascade\n" +
                        ");")
                .use { statement -> statement.executeUpdate() }
        }

        if (SQLUtils.checkIfTableExists(connection,playerEnderChestsTable)) {
            val expected = hashMapOf(
                Pair(PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME,"int unsigned"),
                Pair(PLAYER_ENDER_CHESTS_TABLE_COLUMN_SLOT_NAME,"int unsigned"),
                Pair(PLAYER_ENDER_CHESTS_TABLE_COLUMN_ITEMSTACK_BASE64_NAME,"text"),
            )
            if (!SQLUtils.checkIfTableMatchesStructure(connection,playerEnderChestsTable,expected)) {
                throw SQLException("JDBC table $playerEnderChestsTable does not match expected structure")
            }
        }
        else {
            connection.prepareStatement(
                "create table $playerEnderChestsTable\n" +
                        "(\n" +
                        "    $PLAYER_ENDER_CHESTS_TABLE_COLUMN_ID_NAME    bigint unsigned auto_increment primary key,\n" +
                        "    $PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME    int unsigned,\n" +
                        "    $PLAYER_ENDER_CHESTS_TABLE_COLUMN_SLOT_NAME    int unsigned,\n" +
                        "    $PLAYER_ENDER_CHESTS_TABLE_COLUMN_ITEMSTACK_BASE64_NAME    text not null,\n" +
                        "    foreign key    ($PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME) references $playersTable($PLAYERS_TABLE_COLUMN_ID_NAME) on delete cascade on update cascade\n" +
                        ");")
                .use { statement -> statement.executeUpdate() }
        }

        /*connection.prepareStatement(
            "insert into $databasePropertiesTable ($DATABASE_PROPERTIES_TABLE_KEY_NAME, $DATABASE_PROPERTIES_TABLE_VALUE_NAME) values (?, ?) as new_data on duplicate key update\n " +
                    "$DATABASE_PROPERTIES_TABLE_VALUE_NAME = new_data.$DATABASE_PROPERTIES_TABLE_VALUE_NAME")
            .use { statement ->
                statement.setString(1, "version")
                statement.setString(2, DB_VERSION.toString())

                statement.executeUpdate()
            }
        */
    }

    override fun save() {
        if (!connection.autoCommit)
            connection.commit()
    }

    private fun getPlayerId(uuid : UUID) : Long {
        var ownerId = -1L
        connection.prepareStatement(
            "select $PLAYERS_TABLE_COLUMN_ID_NAME from $playersTable where `$PLAYERS_TABLE_COLUMN_UUID_NAME` = ?")
            .use { statement ->
                statement.setString(1, uuid.toString())
                val result = statement.executeQuery()
                if (result.next()) {
                    ownerId = result.getLong(1)
                }
            }
        return ownerId
    }

    override fun savePlayerData(player: Player) {
        connection.autoCommit = false
        var ownerId = -1L
        connection.prepareStatement(
            "select $PLAYERS_TABLE_COLUMN_ID_NAME from $playersTable where `$PLAYERS_TABLE_COLUMN_UUID_NAME` = ?")
            .use { statement ->
                statement.setString(1, player.uniqueId.toString())
                val result = statement.executeQuery()
                if (!result.next()) {
                    connection.prepareStatement(
                        "insert into $playersTable ($PLAYERS_TABLE_COLUMN_UUID_NAME,$PLAYERS_TABLE_COLUMN_NICKNAME_NAME,$PLAYERS_TABLE_COLUMN_LEVEL_NAME,$PLAYERS_TABLE_COLUMN_EXPERIENCE_NAME) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)
                        .use { statement ->
                            statement.setString(1, player.uniqueId.toString())
                            statement.setString(2, player.name)
                            statement.setString(3, player.level.toString())
                            statement.setFloat(4, player.exp)
                            val affectedRows = statement.executeUpdate()
                            if (affectedRows > 0) {
                                val newResult = statement.generatedKeys
                                newResult.next()
                                ownerId = newResult.getLong(1)
                            }
                        }
                }
                else {
                    ownerId = result.getLong(1)
                }
            }
        if (ownerId != -1L) {

            if (!player.inventory.isEmpty) {
                connection.prepareStatement(
                    "insert into $playerInventoriesTable ($PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME,$PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME,$PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME) VALUES (?, ?, ?)")
                    .use { statement ->

                        for (slot in 0..40) {
                            player.inventory.getItem(slot)?.let { itemStack ->

                                val byteArray = itemStack.serializeAsBytes()
                                val base64String = Base64.getEncoder().encodeToString(byteArray)

                                statement.setString(1, ownerId.toString())
                                statement.setString(2, slot.toString())
                                statement.setString(3, base64String)
                                statement.addBatch()
                            }
                        }
                        statement.executeBatch()
                    }
            }

            if (!player.enderChest.isEmpty) {
                connection.prepareStatement(
                    "insert into $playerEnderChestsTable ($PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME,$PLAYER_ENDER_CHESTS_TABLE_COLUMN_SLOT_NAME,$PLAYER_ENDER_CHESTS_TABLE_COLUMN_ITEMSTACK_BASE64_NAME) VALUES (?, ?, ?)")
                    .use { statement ->

                        for (slot in 0..26) {
                            player.enderChest.getItem(slot)?.let { itemStack ->

                                val byteArray = itemStack.serializeAsBytes()
                                val base64String = Base64.getEncoder().encodeToString(byteArray)

                                statement.setString(1, ownerId.toString())
                                statement.setString(2, slot.toString())
                                statement.setString(3, base64String)
                                statement.addBatch()
                            }
                        }
                        statement.executeBatch()
                    }
            }
        }
        connection.commit()
        connection.autoCommit = true
    }

    override fun removePlayerData(player: Player) {
        removePlayerData(player.uniqueId)
    }

    override fun removePlayerData(uuid : UUID) {
        connection.prepareStatement(
            "delete from $playersTable where `$PLAYERS_TABLE_COLUMN_UUID_NAME` = ?")
            .use { statement ->
                statement.setString(1, uuid.toString())
                statement.executeUpdate()
            }
    }

    override fun loadPlayerData(player: Player) {
        val ownerId = getPlayerId(player.uniqueId)
        if (ownerId != -1L) {

            connection.prepareStatement(
                "select $PLAYERS_TABLE_COLUMN_LEVEL_NAME, $PLAYERS_TABLE_COLUMN_EXPERIENCE_NAME from $playersTable where `$PLAYERS_TABLE_COLUMN_ID_NAME` = ?")
                .use { statement ->
                    statement.setString(1, ownerId.toString())
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val level = resultSet.getInt(1)
                            player.level = level
                            val experience = resultSet.getFloat(2)
                            player.exp = experience
                        }
                    }
                }

            connection.prepareStatement(
                "select $PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME, $PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME from $playerInventoriesTable where `$PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME` = ?")
                .use { statement ->
                    statement.setString(1, ownerId.toString())
                    statement.executeQuery().use { resultSet ->
                        val items = hashMapOf<Int, ItemStack>()
                        while (resultSet.next()) {
                            val slot = resultSet.getInt(1)
                            val base64String = resultSet.getString(2)

                            try {

                                val byteArray = Base64.getDecoder().decode(base64String)
                                val itemStack = ItemStack.deserializeBytes(byteArray)

                                items[slot] = itemStack
                            }
                            catch (ex : Exception) {
                                ex.message
                                VineriumPlayerSync.inst().logger.warning { "Could not deserialize item in player slot $slot. Cause: ${ex.message}" }
                            }
                        }
                        player.inventory.clear()
                        for (itemData in items) {
                            player.inventory.setItem(itemData.key, itemData.value)
                        }
                    }
                }

            connection.prepareStatement(
                "select $PLAYER_ENDER_CHESTS_TABLE_COLUMN_SLOT_NAME, $PLAYER_ENDER_CHESTS_TABLE_COLUMN_ITEMSTACK_BASE64_NAME from $playerEnderChestsTable where `$PLAYER_ENDER_CHESTS_TABLE_COLUMN_PLAYER_ID_NAME` = ?")
                .use { statement ->
                    statement.setString(1, ownerId.toString())
                    statement.executeQuery().use { resultSet ->
                        val items = hashMapOf<Int, ItemStack>()
                        while (resultSet.next()) {
                            val slot = resultSet.getInt(1)
                            val base64String = resultSet.getString(2)

                            try {

                                val byteArray = Base64.getDecoder().decode(base64String)
                                val itemStack = ItemStack.deserializeBytes(byteArray)

                                items[slot] = itemStack
                            }
                            catch (ex : Exception) {
                                ex.message
                                VineriumPlayerSync.inst().logger.warning { "Could not deserialize item in player ender chest slot $slot. Cause: ${ex.message}" }
                            }
                        }
                        player.enderChest.clear()
                        for (itemData in items) {
                            player.enderChest.setItem(itemData.key, itemData.value)
                        }
                    }
                }
        }
    }

    override fun hasPlayerData(player: Player) : Boolean {
        return hasPlayerData(player.uniqueId)
    }

    override fun hasPlayerData(uuid: UUID) : Boolean {
        val ownerId = getPlayerId(uuid)
        return ownerId != -1L
    }

    override fun getPlayerInventory(player: Player) : HashMap<Int, ItemStack> {
        return getPlayerInventory(player.uniqueId)
    }

    override fun getPlayerInventory(uuid: UUID) : HashMap<Int, ItemStack> {
        val ownerId = getPlayerId(uuid)
        val inventory = HashMap<Int, ItemStack>()
        if (ownerId != -1L) {
            connection.prepareStatement(
                "select $PLAYER_INVENTORIES_TABLE_COLUMN_SLOT_NAME, $PLAYER_INVENTORIES_TABLE_COLUMN_ITEMSTACK_BASE64_NAME from $playerInventoriesTable where `$PLAYER_INVENTORIES_TABLE_COLUMN_PLAYER_ID_NAME` = ?")
                .use { statement ->
                    statement.setString(1, ownerId.toString())
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val slot = resultSet.getInt(1)
                            val base64String = resultSet.getString(2)

                            val byteArray = Base64.getDecoder().decode(base64String)
                            val itemStack = ItemStack.deserializeBytes(byteArray)

                            inventory[slot] = itemStack
                        }
                    }
                }
        }
        return inventory
    }

}