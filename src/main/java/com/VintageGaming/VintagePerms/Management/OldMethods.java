package com.VintageGaming.VintagePerms.Management;

import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class OldMethods {
    /*
    //AddPerm to player - UUID Support --- MOVED TO USERS.JAVA
    public void addPlayerPerm(String player, String perm) {
        player = player.toLowerCase();
        Player p = Bukkit.getServer().getPlayer(player);
        UUID uuid = getUUIDFromName(p.getName());
        if (uuid == null) {
            System.err.println("VintagePerms: Could not find UUID for player " + player + " to add permission.");
            return;
        }

        if (p != null) {
            List<Player> pl = new ArrayList<Player>();

            pl.add(p);
            injectPlayer(pl);

            if (perm.contains("-")) attachments.get(player).setPermission(perm.replace("- ", ""), false);

            else attachments.get(player).setPermission(perm, true);
        }

        List<String> perms = getExtraPerms(player);
        perms.add(perm);
        pConfig.set(uuid.toString() + ".permissions", perms);
        Psave();
    }

    //Remove perm from player - UUID Support --- MOVED TO USERS.JAVA
    public void remPlayerPerm(String player, String perm) {
        player = player.toLowerCase();
        Player p = Bukkit.getServer().getPlayer(player);
        UUID uuid = getUUIDFromName(p.getName());
        if (uuid == null) {
            System.err.println("VintagePerms: Could not find UUID for player " + player + " to remove permission.");
            return;
        }

        List<String> perms = getExtraPerms(player);

        if (perms.contains(perm)){
            perms.remove(perm);
            pConfig.set(uuid.toString() + ".permissions", perms);
        }

        else {
            perms.add("- " + perm);
            pConfig.set(uuid.toString() + ".permissions", perms);
        }

        Psave();

        if (p != null) {

            List<Player> players = new ArrayList<Player>();
            players.add(p);

            if (perm.contains("-")){
                injectPlayer(players);
            }

            else {
                injectPlayer(players);
                attachments.get(player).setPermission(perm, false);
            }
        }
    }

    //Get Players Extra Permissions - UUID Support --- MOVED TO USERS.JAVA
    public List<String> getExtraPerms(String player) {
        UUID uuid = getUUIDFromName(player);
        if (uuid == null) {
            return new ArrayList<>(); // Return empty list if player not found
        }
        if (!pConfig.contains(uuid.toString())) {
            pConfig.set(player + ".permissions", new ArrayList<String>());
            Psave();
        }

        return pConfig.getStringList(uuid.toString() + ".permissions");
    }

    //Add Group to Player - UUID Support --- METHOD MOVED TO USERS.JAVA
    public void setPlayerGroup(String player, String group, int index) {

        UUID uuid = getUUIDFromName(player);
        List<String> groups = getPlayerGroups(player.toLowerCase());

        if (groups.contains(group.toLowerCase())) {
            if (index == 0) groups.remove(group);
            return;
        }

        groups.add(index, group.toLowerCase());

        pConfig.set(uuid.toString() + ".groups", groups);
        Psave();

        Player p = Bukkit.getServer().getPlayer(player);

        if (p != null) {
            List<Player> players = new ArrayList<Player>();
            players.add(p);
            injectPlayer(players);
        }

    }

    //remove a Player from group in game - UUID Support --- METHOD MOVED TO USERS.JAVA
    public void remPlayerGroup(String player, String group) {

        List<String> groups = getPlayerGroups(player);

        if (!groups.contains(group.toLowerCase())) return;

        groups.remove(group.toLowerCase());


        if (groups.isEmpty()) {
            groups.add(getDefaultGroup());
        }


        pConfig.set(getUUIDFromName(player).toString() + ".groups", groups);
        Psave();

        Player p = Bukkit.getServer().getPlayer(player);

        if (p != null) {
            List<Player> players = new ArrayList<Player>();
            players.add(p);
            unInjectPlayer(p);
            injectPlayer(players);
        }

    }

    //Get the Groups a player is in - UUID Support --- METHOD MOVED TO USERS.JAVA
    public List<String> getPlayerGroups(String player) {
        UUID uuid = getUUIDFromName(player);
        if (!pConfig.isConfigurationSection(uuid.toString())) {
            ArrayList<String> defaultGroup = new ArrayList<>();
            defaultGroup.add(getDefaultGroup());

            pConfig.set(uuid.toString() + ".permissions", new ArrayList<String>());
            pConfig.set(uuid.toString() + ".groups", defaultGroup);
            Psave();
            injectPlayer(PermsMain.instance.getServer().getOnlinePlayers());
        }

        return pConfig.getStringList(uuid.toString() + ".groups");
    }

    //Inject Players Permissions with Server - UUID Support - --- METHOD MOVED TO USERS.JAVA
    public void injectPlayer(Collection<? extends Player> collection) {
        for (Player p : collection) {
            unInjectPlayer(p);
            if (attachments.get(p.getName().toLowerCase()) == null) attachments.put(p.getName().toLowerCase(), p.addAttachment(getPlugin()));
            UUID uuid = getUUIDFromName(p.getName());
            //Inject Group Permissions to Player
            for (String pGroup : pConfig.getStringList(uuid.toString() + ".groups")) {

                if (getGroup(pGroup) == null) continue;
                Groups g = getGroup(pGroup);

                for (String perm : g.getPerms()) {
                    attachments.get(p.getName().toLowerCase()).setPermission(perm, true);
                }
                if (g.getInheritedPermissions().isEmpty()) continue;

                for (String perm : g.getInheritedPermissions()) {
                    attachments.get(p.getName().toLowerCase()).setPermission(perm, true);
                }
            }

            //Inject Extra Permissions to Player
            for (String perm : getExtraPerms(p.getName().toLowerCase())) {
                if (perm.contains("-")){
                    attachments.get(p.getName().toLowerCase()).unsetPermission(perm.replace("-", ""));
                }
                else
                    attachments.get(p.getName().toLowerCase()).setPermission(perm, true);
            }

            if (getPlayerGroups(p.getName().toLowerCase()).isEmpty()) {
                return;
            }

            List<String> groups = getPlayerGroups(p.getName().toLowerCase());

            if (getConfig().getString(groups.get(0) + ".options.prefix") != null) {
                PermsMain.ranks.getTeam(groups.get(0)).addEntry(p.getName());
            }

            p.setScoreboard(PermsMain.ranks);
        }
    }

    //UnInject Player Permissions with Server --- METHOD MOVED TO USERS.JAVA
    public void unInjectPlayer(Player pl) {
        String playerName = pl.getName().toLowerCase(); // Use lowercase key consistently

        // Remove the permission attachment if it exists
        if (attachments.containsKey(playerName)) {
            try {
                pl.removeAttachment(attachments.get(playerName));
            } catch (IllegalArgumentException e) {
                // This can happen if the attachment is already gone, which is fine.
            }
            attachments.remove(playerName);
        }

        // --- FIX: Prevent NullPointerException ---
        // Check if the player is actually on a team before trying to remove them.
        // This is necessary for players in groups that don't have a prefix.
        Team team = PermsMain.ranks.getEntryTeam(pl.getName());
        if (team != null) {
            team.removeEntry(pl.getName());
        }
    }

    //Get Group Permissions --- METHOD MOVED TO GROUPS.JAVA
    public List<String> getGroupPerms(String group) {
        if (!config.contains(group)) return null;

        return config.getStringList(group + ".permissions");
    }

    */
}
