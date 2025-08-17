package com.VintageGaming.VintagePerms.injection;

import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class VaultHookListener implements Listener {
    private final PermsMain plugin;

    public VaultHookListener(PermsMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        // This is called whenever any plugin is enabled
        if (event.getPlugin().getName().equals("Vault")) {
            // Vault just got enabled, so now is the time to hook in.
            plugin.hookIntoVault();
        }
    }
}
