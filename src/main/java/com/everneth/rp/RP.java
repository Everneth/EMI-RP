package com.everneth.rp;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.rp.commands.GuildCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RP extends JavaPlugin
{
    private static RP plugin;
    private static BukkitCommandManager commandManager;
    FileConfiguration config = getConfig();
    String configPath = getDataFolder() + System.getProperty("file.separator") + "config.yml";
    File configFile = new File(configPath);

    @Override
    public void onEnable()
    {
        plugin = this;
        getLogger().info("Roleplay System started.");
        if(!configFile.exists())
        {
            loadConfig();
        }

        DatabaseOptions options = DatabaseOptions.builder().mysql(config.getString("dbuser"), config.getString("dbpass"), config.getString("dbname"), config.getString("dbhost")).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        registerCommands();
    }
    @Override
    public void onDisable()
    {
        DB.close();
    }

    private void loadConfig()
    {
        config.addDefault("dbhost", "localhost:3306");
        config.addDefault("dbname", "emi");
        config.addDefault("dbuser", "admin_emi");
        config.addDefault("dbpass", "secret");
        config.addDefault("dbprefix", "ev_");
        config.options().copyDefaults(true);
        this.saveConfig();
    }

    private void registerCommands()
    {
        commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new GuildCommand());
    }

    public static RP getPlugin()
    {
        return plugin;
    }
}
