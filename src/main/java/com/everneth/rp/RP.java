package com.everneth.rp;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.idb.DB;
import co.aikar.idb.Database;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.everneth.rp.commands.GuildCommand;
import com.everneth.rp.commands.RoleplayCommand;
import com.everneth.rp.events.JoinEvent;
import com.everneth.rp.events.LeaveEvent;
import com.everneth.rp.models.EMIPlayer;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RP extends JavaPlugin {

    private static RP plugin;
    private static BukkitCommandManager commandManager;
    FileConfiguration config = getConfig();
    String configPath = getDataFolder() + System.getProperty("file.separator") + "config.yml";
    File configFile = new File(configPath);
    private static LuckPerms LP;
    private static List<EMIPlayer> onlinePlayers;


    @Override
    public void onEnable()
    {
        plugin = this;
        onlinePlayers = new ArrayList<>();

        getLogger().info("Roleplay System started.");
        if(!configFile.exists())
        {
            loadConfig();
        }

        DatabaseOptions options = DatabaseOptions.builder().mysql(config.getString("dbuser"), config.getString("dbpass"), config.getString("dbname"), config.getString("dbhost")).build();
        Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LP = provider.getProvider();
        }

        registerCommands();
        registerListeners();
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
        commandManager.registerCommand(new RoleplayCommand());
    }

    private void registerListeners()
    {
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(),this);
    }

    public static RP getPlugin()
    {
        return plugin;
    }

    public static List<EMIPlayer> getOnlinePlayers()
    {
        return onlinePlayers;
    }

    public static LuckPerms getPermsApi() { return LP; }
}
