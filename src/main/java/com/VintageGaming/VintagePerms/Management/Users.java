package com.VintageGaming.VintagePerms.Management;

import com.VintageGaming.VintagePerms.PermsMain;
import com.VintageGaming.VintagePerms.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class Users {

    //All Users from players.yml are Stored in a HashMap in SettingsManager

    private final String name;
    private final String uuid;
    private final List<String> permissions;
    private final List<String> groups;
    private final OfflinePlayer pl;

    private final SettingsManager manager = SettingsManager.getInstance();
    private final FileConfiguration config = SettingsManager.getInstance().getPConfig();

    public Users(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.permissions = config.getStringList(uuid + ".permissions");
        this.groups = config.getStringList(uuid + ".groups");
        this.pl = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
    }

    public void save() {
        SettingsManager.getInstance().Psave();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void addPermission(String permission) {
        if (permissions.contains(permission)) return;

        if (permission.contains("-")) {
            if (!permissions.contains(permission.replace("-", "")))
                permissions.add(permission);
            else permissions.remove(permission.replace("-", ""));
        }

        config.set(uuid + ".permissions", permissions);
        save();
        inject();
    }

    public void removePermission(String permission) {

        if (permissions.contains(permission)) permissions.remove(permission);
        else if (permission.contains("-")) permissions.add(permission);
        else permissions.add("-" + permission);

        config.set(uuid + ".permissions", permissions);
        save();
        inject();
    }

    public void addGroup(Groups group, int index) {
        if (SettingsManager.getGroup(group.getName()) == null) return;

        if (groups.contains(group.getName()) && index != 0) return;
        if (groups.contains(group.getName()) && index == 0) groups.remove(group.getName());

        groups.add(index, group.getName());
        config.set(uuid + ".groups", groups);
        save();
        inject();
    }

    public void addGroup(String group, int index) {
        if (SettingsManager.getGroup(group) == null)
            addGroup(SettingsManager.getGroup(group), index);
    }

    public void setGroup(String group) {
        addGroup(group, 0);
    }

    public void removeGroup(Groups group) {
        groups.remove(group.getName());
        config.set(uuid + ".groups", groups);
        save();
        inject();
    }

    public void removeGroup(String group) {
        removeGroup(SettingsManager.getGroup(group));
    }

    public void removeInvalidGroup(String group) {
        groups.remove(group);
        config.set(uuid + ".groups", groups);
        save();
        inject();
    }

    public void inject() {
        if (pl.isOnline()) return;

        unInject();

        Player p = Bukkit.getServer().getPlayer(UUID.fromString(uuid));

        if (manager.attachments.get(name.toLowerCase()) == null)
            manager.attachments.put(name.toLowerCase(), p.addAttachment(manager.getPlugin()));

        for (String group : groups) {
            if (SettingsManager.getGroup(group) == null) removeInvalidGroup(group);

            Groups g = SettingsManager.getGroup(group);

            for (String perm : g.getPerms()) {
                if (!perm.contains("-"))
                    manager.attachments.get(name.toLowerCase()).setPermission(perm, true);
                else manager.attachments.get(name.toLowerCase()).unsetPermission(perm.replace("-", ""));
            }
            if (g.getInheritedPermissions().isEmpty()) continue;

            for (String perm : g.getInheritedPermissions()) {
                if (!perm.contains("-"))
                    manager.attachments.get(name.toLowerCase()).setPermission(perm, true);
                else manager.attachments.get(name.toLowerCase()).unsetPermission(perm.replace("-", ""));
            }
        }

        //Inject Extra Permissions to Player
        for (String perm : getPermissions()) {
            if (perm.contains("-")) {
                manager.attachments.get(name.toLowerCase()).unsetPermission(perm.replace("-", ""));
            } else
                manager.attachments.get(name.toLowerCase()).setPermission(perm, true);
        }

        if (getGroups().isEmpty()) {
            return;
        }

        List<String> groups = getGroups();

        if (manager.getConfig().getString(groups.get(0) + ".options.prefix") != null) {
            PermsMain.ranks.getTeam(groups.get(0)).addEntry(name);
        }

        p.setScoreboard(PermsMain.ranks);
    }

    public void unInject() {
        if (!pl.isOnline()) return;
        if (manager.attachments.containsKey(name.toLowerCase())) {
            try {
                pl.getPlayer().removeAttachment(manager.attachments.get(name.toLowerCase()));
            } catch (IllegalArgumentException e) {
                // This can happen if the attachment is already gone, which is fine.
            }
            manager.attachments.remove(name.toLowerCase());
        }

        // --- FIX: Prevent NullPointerException ---
        // Check if the player is actually on a team before trying to remove them.
        // This is necessary for players in groups that don't have a prefix.
        Team team = PermsMain.ranks.getEntryTeam(pl.getName());
        if (team != null) {
            team.removeEntry(pl.getName());
        }
    }

}
