package com.VintageGaming.VintagePerms.Management;

import java.util.ArrayList;

import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.VintageGaming.VintagePerms.SettingsManager;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Groups {

    private final String name;
    private final ArrayList<String> perms;
    private final ArrayList<String> inherited;
    private final boolean defaultGroup;

    private final ConfigurationSection groupSection;

    public Groups(String name) {
        this.name = name.toLowerCase();

        this.groupSection = SettingsManager.getInstance().getGroupSection(name);

        if (!groupSection.contains("permissions")) groupSection.set("permissions", new ArrayList<String>());

        this.perms = new ArrayList<String>(groupSection.getStringList("permissions"));
        this.inherited = new ArrayList<String>(groupSection.getStringList("inheritance"));
        this.defaultGroup = groupSection.getBoolean("default");
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
        perms.add(perm.toLowerCase());
        groupSection.set("permissions", perms);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void remPerm(String perm) {
        perms.remove(perm.toLowerCase());
        groupSection.set("permissions", perms);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public ArrayList<String> getPerms() {
        return perms;
    }

    public ArrayList<String> getInheritance() {
        return inherited.isEmpty() ? new ArrayList<String>() : inherited;
    }

    public ArrayList<String> getInheritedPermissions() {
        ArrayList<String> perms = new ArrayList<String>();
        if (inherited.isEmpty()) return perms;
        for (String group : inherited) {
            if (!SettingsManager.getInstance().getConfig().isConfigurationSection(group)) continue;
            if (!SettingsManager.getGroup(group).getPerms().isEmpty())
                perms.addAll(SettingsManager.getGroup(group).getPerms());
        }
        return perms;
    }

    public void addInheritance(String group) {
        if (!SettingsManager.groups.containsKey(group.toLowerCase())) return;
        inherited.add(group.toLowerCase());
        groupSection.set("inheritance", inherited);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void removeInheritance(String group) {
        if (!SettingsManager.groups.containsKey(group.toLowerCase())) return;

        inherited.remove(group.toLowerCase());
        groupSection.set("inheritance", inherited);
        SettingsManager.getInstance().save();
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void deleteGroup() {

        Permission perm = new Permission("vperms.groups." + this.getName().toLowerCase() + ".addplayers", PermissionDefault.FALSE);
        PermsMain.unregisterPermission(perm);
        perm = new Permission("vperms.groups." + this.getName().toLowerCase() + ".removeplayers", PermissionDefault.FALSE);
        PermsMain.unregisterPermission(perm);
        perm = new Permission("vperms.groups." + this.getName().toLowerCase() + ".*", PermissionDefault.FALSE);
        PermsMain.unregisterPermission(perm);
        perm = new Permission("vperms.groups." + this.getName().toLowerCase() + ".addperm", PermissionDefault.FALSE);
        PermsMain.unregisterPermission(perm);
        perm = new Permission("vperms.groups." + this.getName().toLowerCase() + ".removeperm", PermissionDefault.FALSE);
        PermsMain.unregisterPermission(perm);

        if (PermsMain.ranks.getTeam(this.getName()) != null) PermsMain.ranks.getTeam(this.getName()).unregister();
        SettingsManager.getInstance().getConfig().set(this.getName(), null);
        SettingsManager.getInstance().save();
        SettingsManager.groups.remove(this);
        SettingsManager.getInstance().injectOnlinePlayers();
    }

    public void setPrefix(String prefix) {
        SettingsManager.getInstance().getConfig().set(this.getName() + ".options.prefix", prefix);
        SettingsManager.getInstance().save();
        PermsMain.ranks.getTeam(this.getName()).setPrefix(ChatColor.translateAlternateColorCodes('&', prefix) + " ");
    }
}
