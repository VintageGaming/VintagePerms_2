package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Gui.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class GroupsCmd extends PermsCommand  {

    //THIS REDIRECTS TO Group.java AFTER PERMISSION CHECK

    public GroupsCmd() {
        super("groups", "");
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (sender instanceof Player && (sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.*"))) {
            Player p = (Player) sender;
            p.openInventory(createMainGUI());
        }
        else {
            sender.sendMessage(ChatColor.RED + "You Don't Have Permission!");
        }
    }

    public static Inventory createMainGUI() {
        Inventory change;
        change = Bukkit.createInventory(null, 9, "Groups");

        ItemStack Create = new ItemStack(Material.GREEN_CONCRETE);
        ItemStack Edit = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack Delete = new ItemStack(Material.LAVA_BUCKET);
        ItemStack blank = InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        ItemMeta cmeta = Create.getItemMeta();
        cmeta.setDisplayName(ChatColor.GREEN + "Create Group"); //Ask for Chat input for Group Name, bring up options page
        Create.setItemMeta(cmeta);

        ItemMeta emeta = Edit.getItemMeta();
        emeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Edit Groups"); //Open GroupsMaintenance
        Edit.setItemMeta(emeta);

        ItemMeta dmeta = Delete.getItemMeta();
        dmeta.setDisplayName(ChatColor.RED + "Delete Groups"); //List All Groups in an inventory, then open a confirmation page to delete
        Delete.setItemMeta(dmeta);

        change.setItem(0, InventoryAPI.createGuiItem(Material.ARROW, "Go Back"));
        change.setItem(1, blank);
        change.setItem(2, Create);
        change.setItem(3, blank);
        change.setItem(4, Edit);
        change.setItem(5, blank);
        change.setItem(6, Delete);
        change.setItem(7, blank);
        change.setItem(8, blank);

        return change;
    }
}
