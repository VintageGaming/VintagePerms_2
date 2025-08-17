package com.VintageGaming.VintagePerms.Gui;

import com.VintageGaming.VintagePerms.Commands.GroupsCmd;
import com.VintageGaming.VintagePerms.Commands.UsersCmd;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MainMenuUI implements Listener {

    @EventHandler
    public static void mainMenuInteractions(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Player p = (Player) e.getWhoClicked();

        if (e.getView().getTitle().equalsIgnoreCase("Vintage Perms > Main Menu")) {

            e.setCancelled(true);
            if (e.getCurrentItem().getType().equals(Material.DIAMOND_PICKAXE)) {
                p.openInventory(GroupsCmd.createMainGUI());
            }
            else if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                p.openInventory(UsersCmd.createMainGUI(1));
            }
        }
    }
}
