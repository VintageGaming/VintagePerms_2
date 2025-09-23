package com.VintageGaming.VintagePerms.Management;

import com.VintageGaming.VintagePerms.PermsMain;
import com.VintageGaming.VintagePerms.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

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
        permission = permission.trim().toLowerCase();
        if (permissions.contains(permission) || permission.isEmpty()) return;

        String oppositePerm;
        if (permission.startsWith("-"))
            oppositePerm = permission.substring(1);
        else
            oppositePerm = "-" + permission;

        //Remove Opposite/Negated Permissision if it exists
        permissions.remove(oppositePerm);

        permissions.add(permission);

        config.set(uuid + ".permissions", permissions);
        save();
        inject();
    }

    public void removePermission(String permission) {
        permission = permission.trim().toLowerCase();
        if (permission.isEmpty()) return;

        String basePerm = permission.replace("-", "");
        String negativePerm = "-" + basePerm;

        boolean basePermRemoved = permissions.remove(basePerm);
        permissions.remove(negativePerm);

        if (!basePermRemoved) {
            permissions.add(negativePerm);
        }

        config.set(uuid + ".permissions", permissions);
        save();
        inject();
    }

    public void addGroup(String g, int index) {
        Groups group = SettingsManager.getGroup(g);
        if (group == null) return;

        groups.remove(group.getName());
        if (index < 0 || index > groups.size()) {
            groups.add(group.getName());
        }
        else {
            groups.add(index, group.getName());
        }

        config.set(uuid + ".groups", groups);
        save();
        inject();
    }

    public void setGroup(String group) {
        addGroup(group, 0);
    }

    public void removeGroup(String group) {
        group = group.trim().toLowerCase();
        if (group.isEmpty()) return;

        boolean wasRemoved = groups.remove(group);

        if (wasRemoved) {
            config.set(uuid + ".groups", groups);
            save();
            inject();
        }
    }

    public void inject() {
        if (!pl.isOnline()) return;

        unInject();

        Player p = Bukkit.getServer().getPlayer(UUID.fromString(uuid));

        if (manager.attachments.get(name.toLowerCase()) == null)
            manager.attachments.put(name.toLowerCase(), p.addAttachment(manager.getPlugin()));


        Iterator<String> iterator = groups.iterator();
        while (iterator.hasNext()) {
            String group = iterator.next();
            if (SettingsManager.getGroup(group) == null) {
                iterator.remove();
                continue;
            }

            Groups g = SettingsManager.getGroup(group);

            //Regular  Group Permissions
            if (!g.getPerms().isEmpty())
                registerPermissions(g.getPerms());

            //Inherited Permissions
            if (!g.getInheritedPermissions().isEmpty())
                registerPermissions(g.getInheritedPermissions());

            //Per World Permissions
            if (!g.getWorldPerms(p.getWorld().getName()).isEmpty())
                registerPermissions(g.getWorldPerms(p.getWorld().getName()));
        }

        //Inject Extra Permissions to Player
        if (!getPermissions().isEmpty())
            registerPermissions(getPermissions());


        if (!getGroups().isEmpty()) {
            String primaryGroup = getGroups().get(0);
            Team team = PermsMain.ranks.getTeam(primaryGroup);

            if (team != null) {
                team.addEntry(name);
            }
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

    public void registerPermissions(List<String> perms) {
        PermissionAttachment attachment = manager.attachments.get(name.toLowerCase());
        for (String perm : perms) {
            if (!perm.startsWith("-"))
                attachment.setPermission(perm, true);
            else
                attachment.setPermission(perm.substring(1), false);
        }
    }

}
