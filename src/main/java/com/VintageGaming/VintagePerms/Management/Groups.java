package com.VintageGaming.VintagePerms.Management;

import java.util.*;

import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.VintageGaming.VintagePerms.SettingsManager;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.units.qual.A;

public class Groups {

    private final String name;
    private final Map<String, ArrayList<String>> worldPerms = new HashMap<>();
    private final ArrayList<String> perms;
    private final ArrayList<String> inherited;
    private final boolean defaultGroup;

    private final ConfigurationSection groupSection;

    public Groups(String name) {
        this.name = name.toLowerCase();

        this.groupSection = SettingsManager.getInstance().getGroupSection(name.toLowerCase());

        if (!groupSection.contains("permissions")) groupSection.set("permissions", new ArrayList<String>());

        this.perms = new ArrayList<String>(groupSection.getStringList("permissions"));
        this.inherited = new ArrayList<String>(groupSection.getStringList("inheritance"));
        this.defaultGroup = groupSection.getBoolean("default");

        //Per World Permissions -------------------------------------------------------------------------
        if (groupSection.isConfigurationSection("worlds")) {
            ConfigurationSection worldsSection = groupSection.getConfigurationSection("worlds");

            for (String worldName : worldsSection.getKeys(false)) {
                if (Bukkit.getWorld(worldName) == null) {
                    continue;
                }

                List<String> worldPermissions = worldsSection.getStringList(worldName + ".permissions");

                if (!worldPermissions.isEmpty()) {
                    this.worldPerms.put(worldName, new ArrayList<>(worldPermissions));
                }
            }
        }
        //----------------------------------------------------------------------------------------------

    }

    public String getName() {
        return name;
    }

    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    public boolean hasPerm(String perm) {
        return perms.contains(perm);
    }

    public void addPerm(String perm) {
        perm = perm.toLowerCase();
        if (perms.contains(perm)) return;

        perms.add(perm.toLowerCase());
        groupSection.set("permissions", perms);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void remPerm(String perm) {
        if (!perms.remove(perm.toLowerCase())) return;

        groupSection.set("permissions", perms);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public ArrayList<String> getPerms() {
        return new ArrayList<>(perms);
    }

    public ArrayList<String> getWorldPerms(String world) {
        if (!worldPerms.containsKey(world)) return new ArrayList<>();
        return new ArrayList<>(worldPerms.get(world));
    }

    public ArrayList<String> getInheritance() {
        return new ArrayList<>(inherited);
    }

    public ArrayList<String> getInheritedPermissions() {
        Set<String> collectedPerms = new HashSet<>();
        Set<String> visitedGroups = new HashSet<>();

        visitedGroups.add(this.name);

        for (String parentGroupName : this.inherited) {
            Groups parentGroup = SettingsManager.getGroup(parentGroupName);
            if (parentGroup != null) {
                parentGroup.collectInheritedPermissions(collectedPerms, visitedGroups);
            }
        }

        return new ArrayList<>(collectedPerms);
    }

    private void collectInheritedPermissions(Set<String> collectedPerms, Set<String> visitedGroups) {
        if (visitedGroups.contains(this.name)) return;
        visitedGroups.add(this.name);

        collectedPerms.addAll(this.perms);

        for (String parentGroupName : this.inherited) {
            if (visitedGroups.contains(parentGroupName)) {
                continue;
            }

            Groups parentGroup = SettingsManager.getGroup(parentGroupName);
            if (parentGroup != null) {
                parentGroup.collectInheritedPermissions(collectedPerms, visitedGroups);
            }
        }
    }

    public void addInheritance(String group) {
        group = group.toLowerCase();
        if (!SettingsManager.groups.containsKey(group)) return;
        if (inherited.contains(group)) return;

        inherited.add(group.toLowerCase());
        groupSection.set("inheritance", inherited);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void removeInheritance(String group) {
        if (!SettingsManager.groups.containsKey(group.toLowerCase())) return;
        if (!inherited.remove(group.toLowerCase())) return;

        groupSection.set("inheritance", inherited);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void deleteGroup() {

        List<String> permSuffixes = List.of(".addplayers", ".removeplayers", ".*", ".addperm", ".removeperm");
        String basePermNode = "vperms.groups." + this.getName().toLowerCase();

        for (String suffix : permSuffixes) {
            PermsMain.unregisterPermission(new Permission(basePermNode + suffix));
        }

        if (PermsMain.ranks.getTeam(this.getName()) != null) PermsMain.ranks.getTeam(this.getName()).unregister();

        SettingsManager.getInstance().getConfig().set(this.getName(), null);
        SettingsManager.getInstance().save();

        SettingsManager.groups.remove(this.getName());
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void setPrefix(String prefix) {
        groupSection.set("options.prefix", prefix);
        SettingsManager.getInstance().save();

        Team team = PermsMain.ranks.getTeam(this.getName());
        if (team ==null) {
            team = PermsMain.ranks.registerNewTeam(this.getName());
        }

        team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix) + " ");

        SettingsManager.getInstance().injectOnlinePlayers();
    }
}
