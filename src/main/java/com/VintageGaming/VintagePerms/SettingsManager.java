package com.VintageGaming.VintagePerms;

import java.io.File;
import java.util.*;

import com.VintageGaming.VintagePerms.Management.Users;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import com.VintageGaming.VintagePerms.Management.Groups;

public class SettingsManager {

    private SettingsManager() { }
    private static SettingsManager instance = new SettingsManager();

    public static SettingsManager getInstance() {
        return instance;
    }

    private Plugin p;
    private FileConfiguration config;
    private FileConfiguration pConfig;
    private File cFile;
    private File pFile;

    public HashMap<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();

    public static HashMap<String, Groups> groups = new HashMap<String, Groups>(); //<group_name, Groups_Instance> group_name is lowercase

    private HashMap<String, Users> users = new HashMap<String, Users>(); //<player_name, Users_Instance> player_name is lowercase

    public void makeConfig(Plugin p) {
        this.p = p;

        p.saveResource("groups.yml", false);
        p.saveResource("player.yml", false);

        cFile = new File(p.getDataFolder(), "groups.yml");
        pFile = new File(p.getDataFolder(), "player.yml");

        config = YamlConfiguration.loadConfiguration(cFile);
        pConfig = YamlConfiguration.loadConfiguration(pFile);

    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getPConfig() {
        return pConfig;
    }

    public void save() {
        try { config.save(cFile); }

        catch(Exception e) { e.printStackTrace(); }
    }

    public void Psave() {
        try { pConfig.save(pFile); }

        catch(Exception e) { e.printStackTrace(); }
    }


    public void reload() {
        // Reload the configuration files from disk
        config = YamlConfiguration.loadConfiguration(cFile);
        pConfig = YamlConfiguration.loadConfiguration(pFile);

        // Re-register all groups from the newly loaded config.
        // This method already handles fixing group case and clearing the old list.
        registerGroups();
    }

    public Plugin getPlugin() {
        return p;
    }

    //Inject All Online Players with their permissions/groups
    public void injectOnlinePlayers() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            getUser(p).inject();
        }
    }

    //Add new group in game
    public void createGroup(String group) {

        if (groups.keySet().contains(group.toLowerCase())) return;

        config.set(group.toLowerCase() + ".options.default", false);
        config.set(group.toLowerCase() + ".permissions", new ArrayList<String>());
        save();

        groups.put(group.toLowerCase(), new Groups(group.toLowerCase()));
        Groups g = getGroup(group);

        Permission perm = new Permission("vperms.groups." + g.getName().toLowerCase() + ".addplayers", PermissionDefault.FALSE);
        PermsMain.registerPermission(perm);
        perm = new Permission("vperms.groups." + g.getName().toLowerCase() + ".removeplayers", PermissionDefault.FALSE);
        PermsMain.registerPermission(perm);
        perm = new Permission("vperms.groups." + g.getName().toLowerCase() + ".*", PermissionDefault.FALSE);
        PermsMain.registerPermission(perm);
        perm = new Permission("vperms.groups." + g.getName().toLowerCase() + ".addperm", PermissionDefault.FALSE);
        PermsMain.registerPermission(perm);
        perm = new Permission("vperms.groups." + g.getName().toLowerCase() + ".removeperm", PermissionDefault.FALSE);
        PermsMain.registerPermission(perm);
        //FINISH PERMISSIONS -- (Few months later)Don't remember what is needed?
    }

    //Returns Users Reference
    public Users getUser(String name) {
        return users.get(name.toLowerCase());
    }

    public Users getUser(UUID uuid) {
        return users.get(getPlayerNameFromUUID(uuid.toString()).toLowerCase());
    }

    public Users getUser(Player player) {
        return users.get(player.getName().toLowerCase());
    }

    public List<Users> getUsers() {
        return new ArrayList<>(users.values());
    }

    //Returns Group reference from String
    public static Groups getGroup(String name) {
        if (!groups.containsKey(name.toLowerCase())) return null;

        return groups.get(name.toLowerCase());
    }

    //Returns List of Group Names
    public static List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }

    //Returned the config section of a group
    public ConfigurationSection getGroupSection(String name) {
        return config.getConfigurationSection(name.toLowerCase());
    }

    //Returns All Users in player.yml
    public void registerUsers() {
        for (String playerUUID : pConfig.getKeys(false)) {
            if (!getPlayerNameFromUUID(playerUUID).isBlank())
                users.put(getPlayerNameFromUUID(playerUUID).toLowerCase(), new Users(getPlayerNameFromUUID(playerUUID), playerUUID));
        }
    }

    //Creates a new User in player.yml (First time joining)
    public void registerUser(Player player) {
        if (users.containsKey(player.getName().toLowerCase())) return;

        if (!pConfig.contains(player.getUniqueId().toString())) {
            pConfig.set(player.getUniqueId().toString() + ".display_name", player.getName());
            pConfig.set(player.getUniqueId().toString() + ".permissions", new ArrayList<String>());
            if (!getDefaultGroup().isBlank()) pConfig.set(player.getUniqueId().toString() + ".groups", new ArrayList<String>(Arrays.asList(getDefaultGroup())));
            else pConfig.set(player.getUniqueId().toString() + ".groups", new ArrayList<String>());
            Psave();
        }

        users.put(player.getName().toLowerCase(), new Users(player.getName(), player.getUniqueId().toString()));
    }

    //Load all groups from groups.yml for faster access
    public void registerGroups() {
        fixGroupCase(); //Make sure no groups have uppercase letters

        if (!groups.isEmpty()) {
            for (Groups oldGroup : groups.values()) {
                String groupName = oldGroup.getName().toLowerCase();
                PermsMain.unregisterPermission(new Permission("vperms.groups." + groupName + ".addplayers"));
                PermsMain.unregisterPermission(new Permission("vperms.groups." + groupName + ".removeplayers"));
                PermsMain.unregisterPermission(new Permission("vperms.groups." + groupName + ".*"));
                PermsMain.unregisterPermission(new Permission("vperms.groups." + groupName + ".addperm"));
                PermsMain.unregisterPermission(new Permission("vperms.groups." + groupName + ".removeperm"));
            }

            groups.clear();
        }

        for (String group : config.getKeys(false)) {
            Groups g = new Groups(group.toLowerCase());

            groups.put(g.getName(), g);

            Permission perm = new Permission("vperms.groups." + g.getName() + ".addplayers", PermissionDefault.FALSE);

            //Register permissions per group for admin usage
            PermsMain.registerPermission(perm);
            perm = new Permission("vperms.groups." + g.getName() + ".removeplayers", PermissionDefault.FALSE);
            PermsMain.registerPermission(perm);
            perm = new Permission("vperms.groups." + g.getName() + ".*", PermissionDefault.FALSE);
            PermsMain.registerPermission(perm);
            perm = new Permission("vperms.groups." + g.getName() + ".addperm", PermissionDefault.FALSE);
            PermsMain.registerPermission(perm);
            perm = new Permission("vperms.groups." + g.getName() + ".removeperm", PermissionDefault.FALSE);
            PermsMain.registerPermission(perm);

        }
    }

    //Returns the Default group listed in groups.yml
    public String getDefaultGroup() {
        for (Groups g : groups.values()) {
            if (g.isDefaultGroup()) return g.getName();
        }
    return "";
    }

    //Returns Player Name from UUID
    public static String getPlayerNameFromUUID(String uuid) {
        UUID uuidObj = UUID.fromString(uuid);
        return Bukkit.getOfflinePlayer(uuidObj).getName() != null ? Bukkit.getOfflinePlayer(uuidObj).getName() : "";
    }

    //Returns UUID from Player Name
    public static UUID getUUIDFromName(String name) {
        // 1. Check online players (fastest and most accurate)
        Player onlinePlayer = Bukkit.getPlayerExact(name);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // 2. Check Bukkit's offline player data (standard method)
        @SuppressWarnings("deprecation") // This is the intended use case for the deprecated method
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer.getUniqueId();
        }

        // 3. Fallback to usercache.json (useful for players who have changed names)
        Path userCachePath = Paths.get("usercache.json");
        if (!userCachePath.toFile().exists()) {
            return null; // No cache to check
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(userCachePath.toFile())) {
            Type listType = new TypeToken<List<UserCacheEntry>>() {}.getType();
            List<UserCacheEntry> userCache = gson.fromJson(reader, listType);

            if (userCache != null) {
                for (UserCacheEntry entry : userCache) {
                    if (entry.getName().equalsIgnoreCase(name)) {
                        return UUID.fromString(entry.getUuid());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("VintagePerms: Could not read usercache.json.");
            e.printStackTrace();
        }

        return null; // Player not found in any source
    }

    //Used to Retrieve Player Info from usercache.json
    public static class UserCacheEntry {
        private String name;
        private String uuid;
        private String expiresOn;

        public String getName() {
            return name;
        }

        public String getUuid() {
            return uuid;
        }

        public String getExpiresOn() {
            return expiresOn;
        }
    }

    //Fixes Group names in groups.yml to lowercase
    private void fixGroupCase() {
        FileConfiguration config = SettingsManager.getInstance().getConfig();

        // We use a copy of the keys to avoid errors while modifying the config during the loop.
        Set<String> groupKeys = Set.copyOf(config.getKeys(false));

        for (String groupName : groupKeys) {
            if (groupName.equals(groupName.toLowerCase())) {
                // This group is already lowercase, so we can skip it.
                continue;
            }

            String newGroupName = groupName.toLowerCase();

            // CRITICAL: Check for conflicts to prevent data loss.
            // If a group named 'admin' already exists, we don't want to overwrite it with 'Admin'.
            if (config.isConfigurationSection(newGroupName)) {
                PermsMain.instance.getLogger().info(ChatColor.RED + "Conflict! A group named '" + newGroupName + "' already exists. Skipping '" + groupName + "' to prevent data loss.");
                continue;
            }

            // Copy the entire section to the new lowercase key.
            config.set(newGroupName, config.getConfigurationSection(groupName));
            // Remove the old, mixed-case key.
            config.set(groupName, null);

            PermsMain.instance.getLogger().info(ChatColor.GRAY + "  - Fixed '" + groupName + "' -> '" + newGroupName + "'");
        }
    }

    /*     Config Example for groups.yml --- Might Include Future Features
     *
     * example:
     *   options:
     *     default: false
     *   permissions:
     *   - essentials.kick
     *
     * default:
     *   options:
     *     default: true
     *     prefix: <Default>
     *   permissions:
     *   - essentials.kill
     *   worlds:
     *     world:
     *       permissions:
     *       - essentials.back
     *     world-nether:
     *       permissions:
     *       - essentials.enderchest
     *     world-end:
     *       permissions:
     *       - essentials.enderchest
     *
     * owner:
     *   options:
     *     prefix: '[&4Owner&f]'
     *   permissions:
     *   - essentials.ban
     *   inheritance:
     *   - example
     *   - default
     */
}
