package com.VintageGaming.VintagePerms.Gui;

import com.VintageGaming.VintagePerms.Commands.GuiMainCommand;
import com.VintageGaming.VintagePerms.Commands.UsersCmd;
import com.VintageGaming.VintagePerms.Management.Users;
import com.VintageGaming.VintagePerms.SettingsManager;
import com.VintageGaming.VintagePerms.Management.Groups;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class UserUI implements Listener {

    @EventHandler
    public static void usersInterfaceInteractions(InventoryClickEvent e) {

        if (e.getClickedInventory() == null || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        InventoryHolder holder = e.getClickedInventory().getHolder();
        if (!(holder instanceof PaginatedGuiHolder)) return;

        e.setCancelled(true);

        PaginatedGuiHolder paginatedGuiHolder = (PaginatedGuiHolder) holder;

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        SettingsManager manager = SettingsManager.getInstance();
        Map<String, String> infoMap = paginatedGuiHolder.getInfo();
        Users user = null;

        if (infoMap != null && infoMap.containsKey("player_name"))
            user = manager.getUser(paginatedGuiHolder.getInfo().get("player_name"));

        if (e.getView().getTitle().equalsIgnoreCase("Vintage Perms > Users")) {
            //Users Gui Options: Main Menu will Display All Online Players. Then you choose to Change User's Group, add Plugin Permissions
            if (clicked.getType().equals(Material.PLAYER_HEAD)) { //Change to Player Head Type
                p.openInventory(playerMaintenenceMenu(ChatColor.stripColor(clicked.getItemMeta().getDisplayName())));
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() == 1) {
                p.openInventory(GuiMainCommand.createMainGUI());
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() > 1) {
                p.openInventory(UsersCmd.createMainGUI(paginatedGuiHolder.getPage() - 1));
            }
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Next")) {
                p.openInventory(UsersCmd.createMainGUI(paginatedGuiHolder.getPage() + 1));
            }
        }
        else if (e.getView().getTitle().contains("Vintage Perms > User > ") && paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("main_menu")) {
            if (clicked.getType().equals(Material.DIAMOND_PICKAXE)) {

                p.openInventory(listGroupsMenu(1, paginatedGuiHolder.getInfo().get("player_name")));
            }
            else if (clicked.getType().equals(Material.LIME_DYE)) {
                p.openInventory(groupUI.listPluginsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), 1));
            }
            else if (clicked.getType().equals(Material.RED_DYE)) {

                p.openInventory(playerPermsMenu(1, paginatedGuiHolder.getInfo().get("player_name")));
            }
            else if (clicked.getType().equals(Material.ARROW)) {
                p.openInventory(UsersCmd.createMainGUI(1));
            }
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
        else if (e.getView().getTitle().contains("User > ") && paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("change group")) {
            if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                user.removeGroup(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase());
                e.setCurrentItem(InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                user.addGroup(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase(), user.getGroups().size()-1);
                e.setCurrentItem(InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            else if (clicked.getType().equals(Material.ARROW)) {
                if (clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() == 1) {
                    p.openInventory(playerMaintenenceMenu(paginatedGuiHolder.getInfo().get("player_name")));
                }
                else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() > 1) {
                    p.openInventory(listGroupsMenu(paginatedGuiHolder.getPage() - 1, paginatedGuiHolder.getInfo().get("player_name")));
                }
                else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                    p.openInventory(listGroupsMenu(paginatedGuiHolder.getPage() + 1, paginatedGuiHolder.getInfo().get("player_name")));
                }
            }
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
        else if (e.getView().getTitle().contains("Player > ") && paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("list_plugins")) {
            //Clicked on a Plugin
            if (clicked.getType().equals(Material.PURPLE_STAINED_GLASS_PANE)) {
                p.openInventory(groupUI.listPluginPermsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), ChatColor.stripColor(clicked.getItemMeta().getDisplayName()), 1));
            }
            //Clicked on Back/Forward Button in Listed Plugins GUI
            else if (clicked.getType().equals(Material.ARROW)) {
                //Go to Maintenance Page
                if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                    p.openInventory(playerMaintenenceMenu(paginatedGuiHolder.getInfo().get("player_name")));
                }
                //Go Back a Page of Listed Plugins (Damn that's a lot of plugins)
                else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")){
                    p.openInventory(groupUI.listPluginsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), paginatedGuiHolder.getPage()-1));
                }
                //Go to Next Page of Listed Plugins
                else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                    p.openInventory(groupUI.listPluginsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), paginatedGuiHolder.getPage()+1));
                }
            }
            //Close Inventory
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
        else if (e.getView().getTitle().contains(" > Permissions") && paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("list_permissions") && paginatedGuiHolder.getInfo().containsKey("player_name")) {
            //Clicked on Forward/Back Button on Listed Permissions
            if (clicked.getType().equals(Material.ARROW)) {
                //Go Back to Plugins List Page
                if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                    p.openInventory(groupUI.listPluginsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), 1));
                }
                //Go Back a Page of Listed Plugin Permissions
                else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")){
                    p.openInventory(groupUI.listPluginPermsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), paginatedGuiHolder.getInfo().get("plugin_name"), paginatedGuiHolder.getPage()-1));
                }
                //Go to Next Page of Listed Plugins
                else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                    p.openInventory(groupUI.listPluginPermsMenu("player", paginatedGuiHolder.getInfo().get("player_name"), paginatedGuiHolder.getInfo().get("plugin_name"), paginatedGuiHolder.getPage()+1));
                }
            }
            //Remove Permission from Player
            else if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                user.removePermission(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            //Add Permission to Player
            else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                user.addPermission(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            //Close Inventory
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
        else if (e.getView().getTitle().contains("User > ") && paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("remove perms")) {
            if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                user.removePermission(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                e.setCurrentItem(InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                user.addPermission(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                e.setCurrentItem(InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() > 1) {
                p.openInventory(playerPermsMenu(paginatedGuiHolder.getPage() - 1, paginatedGuiHolder.getInfo().get("player_name")));
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Previous") && paginatedGuiHolder.getPage() == 1) {
                p.openInventory(playerMaintenenceMenu(paginatedGuiHolder.getInfo().get("player_name")));
            }
            else if (clicked.getType().equals(Material.ARROW) && clicked.getItemMeta().getDisplayName().contains("Next")) {
                p.openInventory(playerPermsMenu(paginatedGuiHolder.getPage() + 1, paginatedGuiHolder.getInfo().get("player_name")));
            }
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
    }

    public static Inventory playerMaintenenceMenu(String player) {
        //Change Users Group
        //Remove Extra Permissions
        //Add Extra Permissions
        //Back Button
        Map<String, String> info = new HashMap<>();
        info.put("player_name", player);
        info.put("action", "main_menu");

        InventoryHolder holder = new PaginatedGuiHolder("player", 1, 1, info);
        Inventory userModification = Bukkit.createInventory(holder, 9, "Vintage Perms > User > " + player);
        ItemStack blank = InventoryAPI.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        userModification.setItem(0, InventoryAPI.createGuiItem(Material.ARROW, "Back"));
        userModification.setItem(1, blank);
        userModification.setItem(2, InventoryAPI.createGuiItem(Material.DIAMOND_PICKAXE, "Change Groups", ChatColor.DARK_AQUA + "Total Current Groups: " + SettingsManager.getInstance().getUser(player).getGroups().size()));
        userModification.setItem(3, blank);
        userModification.setItem(4, InventoryAPI.createGuiItem(Material.LIME_DYE, "Add Extra Permissions"));
        userModification.setItem(5,InventoryAPI.createGuiItem(Material.RED_DYE, "Remove Extra Permissions", ChatColor.DARK_AQUA + "Total Extra Permissions: " + SettingsManager.getInstance().getUser(player).getPermissions().size()));
        userModification.setItem(6, blank);
        userModification.setItem(7, blank);
        userModification.setItem(8, blank);
        return userModification;

    }

    public static Inventory playerPermsMenu(int page, String player) {
        Map<String, String> info = new HashMap<>();
        info.put("action", "remove perms");
        info.put("player_name", player);

        return InventoryAPI.createInventoryPage("player", page, "User > " + player + " > Remove Extra Permissions", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getInstance().getUser(player).getPermissions()), info);
    }

    public static Inventory listGroupsMenu(int page, String editedPlayer) {
        List<ItemStack> playerGroups = new ArrayList<>();
        Map<String, String> info = new HashMap<>();

        for (Groups group : SettingsManager.groups.values()) {
            if (SettingsManager.getInstance().getUser(editedPlayer).getGroups().contains(group.getName()))
                playerGroups.add(InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, group.getName()));

            else
                playerGroups.add(InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, group.getName()));
        }
        info.put("action", "change group");
        info.put("player_name", editedPlayer);

        return InventoryAPI.createInventoryPage("player", page, "User > " + editedPlayer + " > Change Groups", playerGroups, info);
    }
}
