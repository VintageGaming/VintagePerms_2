package com.VintageGaming.VintagePerms.Commands;

import java.util.*;
import java.io.File;

import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.VintageGaming.VintagePerms.SettingsManager;

public class ConvertCmd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player || sender.hasPermission("vperms.*") || sender.hasPermission("vperms.convert")) {
            if (args.length == 0) {
                FileConfiguration config = SettingsManager.getInstance().getConfig();
                for (String group : SettingsManager.getInstance().getConfig().getKeys(false)) {
                    if (config.get(group + ".default") == null && config.get(group + ".prefix") == null) {
                        sender.sendMessage(ChatColor.RED + "Already Converted!");
                        return true;
                    }
                    List<String> permissions = config.getStringList(group + ".permissions");
                    config.set(group + ".permissions", null);
                    if (config.get(group + ".default") != null) {
                        config.set(group + ".options.default", config.getBoolean(group + ".default"));
                        config.set(group + ".default", null);
                    }
                    if (config.get(group + ".prefix") != null) {
                        config.set(group + ".options.prefix", config.getString(group + ".prefix"));
                        config.set(group + ".prefix", null);
                    }
                    config.set(group + ".permissions", permissions);
                }
                SettingsManager.getInstance().save();
                for (String user : SettingsManager.getInstance().getPConfig().getKeys(false)) {
                    if (SettingsManager.getInstance().getPConfig().isConfigurationSection(SettingsManager.getPlayerNameFromUUID(SettingsManager.getUUIDFromName(user).toString()) + ".permissions"))
                        continue;

                    List<String> permissions = SettingsManager.getInstance().getPConfig().getStringList(user + ".permissions");
                    List<String> groups = SettingsManager.getInstance().getPConfig().getStringList(user + ".groups");

                    SettingsManager.getInstance().getPConfig().set(user, null);

                    SettingsManager.getInstance().getPConfig().set(SettingsManager.getUUIDFromName(user).toString() + ".display_name", user);
                    SettingsManager.getInstance().getPConfig().set(SettingsManager.getUUIDFromName(user).toString() + ".permissions", permissions);
                    SettingsManager.getInstance().getPConfig().set(SettingsManager.getUUIDFromName(user).toString() + ".groups", groups);

                }
                SettingsManager.getInstance().Psave();
                sender.sendMessage(ChatColor.GREEN + "Complete!");
                return true;
            }
            else if (args[0].equalsIgnoreCase("pex") || args[0].equalsIgnoreCase("permissionsex")) {
                convertPexFile(sender);
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
        return true;
    }

    private void convertPexFile(CommandSender sender) {
        // Locate the PEX permissions file relative to the plugins folder
        File pexFile = new File(SettingsManager.getInstance().getPlugin().getDataFolder().getParentFile(), "PermissionsEx/permissions.yml");

        if (!pexFile.exists()) {
            sender.sendMessage(ChatColor.RED + "PermissionsEx file not found at: " + pexFile.getPath());
            sender.sendMessage(ChatColor.RED + "Please make sure PermissionsEx is installed and has a permissions.yml file.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "Found PermissionsEx file. Starting conversion...");

        FileConfiguration pexConfig = YamlConfiguration.loadConfiguration(pexFile);
        FileConfiguration vintageGroupsConfig = SettingsManager.getInstance().getConfig();
        FileConfiguration vintagePlayersConfig = SettingsManager.getInstance().getPConfig();

        // --- Convert Groups ---
        if (pexConfig.isConfigurationSection("groups")) {
            ConfigurationSection pexGroups = pexConfig.getConfigurationSection("groups");
            int groupCount = 0;
            for (String groupName : pexGroups.getKeys(false)) {
                String pexGroupPath = "groups." + groupName;
                String vintageGroupPath = groupName.toLowerCase(); // VintagePerms uses lowercase group names

                // Read all relevant data from the PEX group
                boolean isDefault = pexConfig.getBoolean(pexGroupPath + ".options.default", false);
                String prefix = pexConfig.getString(pexGroupPath + ".options.prefix");
                List<String> permissions = pexConfig.getStringList(pexGroupPath + ".permissions");
                List<String> inheritance = pexConfig.getStringList(pexGroupPath + ".inheritance");
                if (vintageGroupsConfig.isConfigurationSection(groupName.toLowerCase())) {
                    List<String> vperms = vintageGroupsConfig.getStringList(groupName.toLowerCase() + ".permissions");
                    List<String> vinheritance = vintageGroupsConfig.getStringList(groupName.toLowerCase() + ".inheritance");

                    Set<String> combinedPerms = new LinkedHashSet<>(vperms);
                    Set<String> combinedInherit = new LinkedHashSet<>(vinheritance);
                    combinedPerms.addAll(permissions);
                    combinedInherit.addAll(inheritance);

                    permissions = new ArrayList<>(combinedPerms);
                    inheritance = new ArrayList<>(combinedInherit);

                    if (vintageGroupsConfig.isConfigurationSection(groupName.toLowerCase() + ".options.prefix"))
                        prefix = vintageGroupsConfig.getString(groupName.toLowerCase() + ".options.prefix");
                }
                // Write the data to your groups.yml in the correct format
                vintageGroupsConfig.set(vintageGroupPath + ".options.default", isDefault);
                if (prefix != null) {
                    vintageGroupsConfig.set(vintageGroupPath + ".options.prefix", prefix);
                }
                vintageGroupsConfig.set(vintageGroupPath + ".permissions", permissions);
                if (inheritance != null && !inheritance.isEmpty()) {
                    vintageGroupsConfig.set(vintageGroupPath + ".inheritance", inheritance);
                }
                groupCount++;
            }
            sender.sendMessage(ChatColor.GREEN + "Successfully converted " + groupCount + " groups.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No 'groups' section found in PEX config.");
        }

        // --- Convert Users ---
        if (pexConfig.isConfigurationSection("users")) {
            ConfigurationSection pexUsers = pexConfig.getConfigurationSection("users");
            int userCount = 0;
            int skippedCount = 0;
            for (String pexUserKey : pexUsers.getKeys(false)) {
                String playerName;
                UUID playerUuid;

                // PEX can store users by name or UUID. We need to find the UUID regardless.
                try {
                    playerUuid = UUID.fromString(pexUserKey);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
                    playerName = offlinePlayer.hasPlayedBefore() ? offlinePlayer.getName() : null;
                } catch (IllegalArgumentException e) {
                    // Key is a name, so we find the UUID from the name.
                    playerName = pexUserKey;
                    playerUuid = SettingsManager.getUUIDFromName(playerName);
                }

                if (playerUuid == null || playerName == null) {
                    sender.sendMessage(ChatColor.RED + "Could not find UUID/name for user '" + pexUserKey + "'. Skipping.");
                    skippedCount++;
                    continue;
                }

                String pexUserPath = "users." + pexUserKey;
                String vintageUserPath = playerUuid.toString();

                // Read user data from PEX
                List<String> groups = pexConfig.getStringList(pexUserPath + ".group");
                List<String> permissions = pexConfig.getStringList(pexUserPath + ".permissions");

                //VintagePerms Data
                List<String> vgroups = SettingsManager.getInstance().getUser(playerUuid).getGroups();
                List<String> vperms = SettingsManager.getInstance().getUser(playerUuid).getPermissions();

                //Combine Data
                Set<String> combinedGroups = new LinkedHashSet<>(vgroups);
                Set<String> combinedPermissions = new LinkedHashSet<>(vperms);
                combinedGroups.addAll(groups);
                combinedPermissions.addAll(permissions);
                groups = new ArrayList<>(combinedGroups);
                permissions = new ArrayList<>(combinedPermissions);



                // Write to your player.yml using the UUID as the key
                vintagePlayersConfig.set(vintageUserPath + ".display_name", playerName);
                vintagePlayersConfig.set(vintageUserPath + ".permissions", permissions);
                vintagePlayersConfig.set(vintageUserPath + ".groups", groups);

                userCount++;
            }
            sender.sendMessage(ChatColor.GREEN + "Successfully converted " + userCount + " users.");
            if (skippedCount > 0) {
                sender.sendMessage(ChatColor.YELLOW + "Skipped " + skippedCount + " users due to missing data. Check console for details.");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "No 'users' section found in PEX config.");
        }

        // --- Save and Finalize ---
        SettingsManager.getInstance().save(); // Saves groups.yml
        SettingsManager.getInstance().Psave(); // Saves player.yml
        PermsMain.reloadWholePlugin();
        sender.sendMessage(ChatColor.AQUA + "Conversion from PermissionsEx complete!");
    }
}
