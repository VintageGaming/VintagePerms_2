package com.VintageGaming.VintagePerms.injection;

import java.util.ArrayList;
import java.util.List;

import com.VintageGaming.VintagePerms.Gui.groupUI;
import com.VintageGaming.VintagePerms.Management.Groups;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.VintageGaming.VintagePerms.SettingsManager;

public class InjectEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (SettingsManager.getInstance().getUser(p.getName()) == null) {
            SettingsManager.getInstance().registerUser(p);
        }

        if (SettingsManager.getInstance().getUser(p).getGroups().isEmpty()) {

            String group = !SettingsManager.getInstance().getDefaultGroup().isBlank() ? SettingsManager.getInstance().getDefaultGroup() : "";

            //End If Player Has No Group
            if (!group.isBlank()){
                SettingsManager.getInstance().getUser(p).addGroup(group, 0); //checked
            }
        }

        SettingsManager.getInstance().getUser(p).inject();
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        SettingsManager.getInstance().getUser(e.getPlayer());
        if (groupUI.groupCreationWaitList.contains(e.getPlayer())) groupUI.groupCreationWaitList.remove(e.getPlayer());
        if (groupUI.prefixChangeWaitList.containsKey(e.getPlayer())) groupUI.prefixChangeWaitList.remove(e.getPlayer());
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {

        Player p = event.getPlayer();

        if (SettingsManager.getInstance().getUser(p).getGroups().isEmpty()) {
            return;
        }

        List<String> groups = SettingsManager.getInstance().getUser(p).getGroups();
        String GPrefix;

        if (SettingsManager.getInstance().getConfig().isConfigurationSection(groups.getFirst() + ".options.prefix")) {
            GPrefix = SettingsManager.getInstance().getConfig().getString(groups.getFirst() + ".options.prefix");
            event.setFormat(ChatColor.translateAlternateColorCodes('&', GPrefix) + " " + "%s" + ChatColor.RESET + ": %s");
        }
    }

}
