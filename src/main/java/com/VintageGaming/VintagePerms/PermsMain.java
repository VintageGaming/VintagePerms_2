package com.VintageGaming.VintagePerms;

import java.util.logging.Logger;

import com.VintageGaming.VintagePerms.Gui.MainMenuUI;
import com.VintageGaming.VintagePerms.Gui.UserUI;
import com.VintageGaming.VintagePerms.Gui.groupUI;
import com.VintageGaming.VintagePerms.Management.Groups;
import com.VintageGaming.VintagePerms.injection.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.VintageGaming.VintagePerms.Commands.ConvertCmd;
import com.VintageGaming.VintagePerms.Commands.MainCommands;

public class PermsMain extends JavaPlugin {

    /* To-Do List:
       - Haven't Tested for any HUGE usages for the GUI. (A lot of plugins)

     */


    public static Scoreboard ranks;

    static PluginManager pm = Bukkit.getServer().getPluginManager();
    public static Plugin instance;
    public ServicesManager sm = null;

    public void onEnable() {
        sm = getServer().getServicesManager();

        Logger logger = Logger.getLogger("Minecraft");

        instance = this;

        // A fallback for if Vault is already loaded
        hookIntoVault();

        SettingsManager.getInstance().makeConfig(this);

        SettingsManager.getInstance().registerGroups();
        SettingsManager.getInstance().registerUsers();

        ranks = getServer().getScoreboardManager().getNewScoreboard();
        createScoreBoardRanks();
        ScoreBoardPrefixes.updatePrefixes();

        pm.registerEvents(new VaultHookListener(this), this);
        pm.registerEvents(new InjectEvents(), this);
        pm.registerEvents(new groupUI(), this);
        pm.registerEvents(new UserUI(), this);
        pm.registerEvents(new MainMenuUI(), this);

        getCommand("vperms").setExecutor(new MainCommands());
        getCommand("vconvert").setExecutor(new ConvertCmd());

        logger.info("VintagePerms Has been Enabled!");
        if (!getServer().getOnlinePlayers().isEmpty()) {
            for (Player p : getServer().getOnlinePlayers()) {
                SettingsManager.getInstance().getUser(p).inject();
            }
        }

    }
    public void onDisable() {

        for (Player p : getServer().getOnlinePlayers()) {
            SettingsManager.getInstance().getUser(p).unInject();
        }

        Logger logger = Logger.getLogger("Minecraft");
        logger.info("VintagePerms Has been Disabled!");
    }

    public static void reloadWholePlugin() {
        // 1. Tell the SettingsManager to reload all config files from disk
        SettingsManager.getInstance().reload();

        // 2. Re-create the scoreboard teams based on the newly loaded config
        createScoreBoardRanks();

        // 3. Re-inject permissions for all currently online players to apply changes immediately
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                SettingsManager.getInstance().getUser(p).inject();
            }
        }
    }

    public static void registerPermission(Permission perm) {
        pm.addPermission(perm);
    }

    public static void unregisterPermission(Permission perm) {
        pm.removePermission(perm);
    }

    public static void createScoreBoardRanks() {
        for (Groups rank : SettingsManager.groups.values()) {

            if (SettingsManager.getInstance().getConfig().getString(rank.getName() + ".options.prefix") == null || SettingsManager.getInstance().getConfig().getString(rank.getName() + ".options.prefix") == "")
                continue;

            Team CRank = ranks.getTeam(rank.getName());

            if (CRank == null) {
                CRank = ranks.registerNewTeam(rank.getName());
            }

            CRank.setPrefix(ChatColor.translateAlternateColorCodes('&', SettingsManager.getInstance().getConfig().getString(rank.getName() + ".options.prefix")) + " ");
        }
    }
    //All Permission Methods
    public VVHook VVPermission() {
        return new VVHook();
    }

    public static PluginManager getPm() {
        return pm;
    }

    public void hookIntoVault() {
        // Check if Vault is on the server
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) {
            getLogger().info("Vault not found, permission hook disabled.");
            return;
        }

        // Check if a permission service is already registered. If so, we don't need to do anything.
        if (getServer().getServicesManager().getRegistration(Permission.class) != null) {
            getLogger().info("A permission plugin is already registered with Vault. Skipping hook.");
            return;
        }

        // Create an instance of our bridge class
        VaultPermissionBridge permissionProvider = new VaultPermissionBridge(this);

        // Register our permission provider with Vault's service manager.
        // ServicePriority.Highest means other plugins should prefer our provider if multiple are available.
        getServer().getServicesManager().register(net.milkbowl.vault.permission.Permission.class, permissionProvider, this, ServicePriority.Highest);

        getLogger().info("Successfully hooked into Vault as a permission provider!");
    }

}
