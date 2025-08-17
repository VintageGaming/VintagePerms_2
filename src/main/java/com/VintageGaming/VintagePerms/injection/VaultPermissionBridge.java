package com.VintageGaming.VintagePerms.injection;

import com.VintageGaming.VintagePerms.Management.Groups;
import com.VintageGaming.VintagePerms.PermsMain;
import com.VintageGaming.VintagePerms.SettingsManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VaultPermissionBridge extends Permission {


    private final PermsMain plugin;

    public VaultPermissionBridge(PermsMain plugin) {
        this.plugin = plugin;
        // It's good practice to log that the bridge is being initialized
        this.plugin.getLogger().info("VaultPermissionBridge initialized.");
    }

    // --- Core Methods You MUST Implement ---

    @Override
    public String getName() {
        // Return the name of your permissions plugin
        return "VintagePerms";
    }

    @Override
    public boolean isEnabled() {
        // Return true if your plugin is enabled, otherwise false
        return this.plugin.isEnabled();
    }

    @Override
    public boolean has(String worldName, String playerName, String permission) {
        Player p = Bukkit.getPlayer(playerName);
        return p.hasPermission(permission);
    }

    @Override
    public boolean playerHas(String worldName, String playerName, String permission) {
        // Most of the time, this can just call the other has() method.
        return has(worldName, playerName, permission);
    }

    @Override
    public boolean playerAdd(String worldName, String playerName, String permission) {
        // TODO: Add your logic to give a player a permission node.
        SettingsManager.getInstance().getUser(playerName).addPermission(permission);
        return true; // Return true on success
    }

    @Override
    public boolean playerRemove(String worldName, String playerName, String permission) {
        // TODO: Add your logic to take a permission node from a player.
        SettingsManager.getInstance().getUser(playerName).removePermission(permission);
        return true; // Return true on success
    }

    @Override
    public String getPrimaryGroup(String worldName, String playerName) {
        // TODO: Add your logic to get the name of the player's primary group.
        return SettingsManager.getInstance().getUser(playerName).getGroups().getFirst();
    }

    @Override
    public boolean playerInGroup(String worldName, String playerName, String group) {
        // TODO: Add your logic to check if a player is in a specific group.
        return SettingsManager.getInstance().getUser(playerName).getGroups().contains(group);
    }

    @Override
    public boolean playerAddGroup(String worldName, String playerName, String group) {
        SettingsManager.getInstance().getUser(playerName).addGroup(group, SettingsManager.getInstance().getUser(playerName).getGroups().size()-1);
        return true; // Return true on success
    }

    @Override
    public boolean playerRemoveGroup(String worldName, String playerName, String group) {
        SettingsManager.getInstance().getUser(playerName).removeGroup(group);
        return true; // Return true on success
    }


    // --- Other Required Methods (Implement these as well) ---

    @Override
    public boolean groupHas(String worldName, String groupName, String permission) {
        // TODO: Add your logic to check if a group has a permission.
        List<String> perms = SettingsManager.getGroup(groupName).getPerms();
        perms.addAll(SettingsManager.getGroup(groupName).getInheritedPermissions());

        return perms.contains(permission);
    }

    @Override
    public boolean groupAdd(String worldName, String groupName, String permission) {
        SettingsManager.getGroup(groupName).addPerm(permission);
        return true;
    }

    @Override
    public boolean groupRemove(String worldName, String groupName, String permission) {
        SettingsManager.getGroup(groupName).remPerm(permission);
        return true;
    }

    @Override
    public String[] getPlayerGroups(String worldName, String playerName) {
        // TODO: Add your logic to get a list of all groups a player is in.
        return SettingsManager.getInstance().getUser(playerName).getGroups().toArray(new String[0]);
    }

    @Override
    public String[] getGroups() {
        ArrayList<String> groups = new ArrayList<>();
        for (Groups g : SettingsManager.groups.values()) {
            groups.add(g.getName());
        }
        return groups.toArray(new String[0]);
    }

    @Override
    public boolean hasSuperPermsCompat() {
        // You can generally return true here. It means your plugin handles permissions.
        return true;
    }

    @Override
    public boolean hasGroupSupport() {
        // Return true if your plugin supports groups.
        return true;
    }
}
