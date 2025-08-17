package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Gui.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiMainCommand extends PermsCommand {

    public GuiMainCommand() {
        super("gui", "");
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (sender instanceof Player && (sender.hasPermission("vperms.*"))) {
            Player p = (Player) sender;
            p.openInventory(createMainGUI());
        } else {
            sender.sendMessage(ChatColor.RED + "You Don't Have Permission!");
        }
    }

    public static Inventory createMainGUI() {
        Inventory mainMenu = Bukkit.createInventory(null, 9, "Vintage Perms > Main Menu");
        mainMenu.setItem(0, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(1, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(2, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(3, InventoryAPI.createGuiItem(Material.DIAMOND_PICKAXE, "Groups Maintenance"));
        mainMenu.setItem(4, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(5, InventoryAPI.createGuiItem(Material.PLAYER_HEAD, "Users Maintenance"));
        mainMenu.setItem(6, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(7, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        mainMenu.setItem(8, InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        return mainMenu;
    }

}
