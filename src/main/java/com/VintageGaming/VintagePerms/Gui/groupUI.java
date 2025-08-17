package com.VintageGaming.VintagePerms.Gui;

import com.VintageGaming.VintagePerms.Commands.GroupsCmd;
import com.VintageGaming.VintagePerms.Commands.GuiMainCommand;
import com.VintageGaming.VintagePerms.PermsMain;
import com.VintageGaming.VintagePerms.SettingsManager;
import com.VintageGaming.VintagePerms.Management.Groups;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class groupUI implements Listener {

    public static List<Player> groupCreationWaitList = new ArrayList<>();
    public static Map<Player, String> prefixChangeWaitList = new HashMap<>();

    @EventHandler
    public static void groupSelectorClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        InventoryHolder holder = e.getClickedInventory().getHolder();
        if (!(holder instanceof PaginatedGuiHolder) && !e.getView().getTitle().equals("Groups")) return;

        e.setCancelled(true);

        PaginatedGuiHolder paginatedGuiHolder = (PaginatedGuiHolder) holder;
        Map<String, String> info = new HashMap<>();

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (e.getView().getTitle().equals("Groups")) {
            //Create New Group via chat
            if (ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).equals("Create Group")) {
                //Ask in Chat ~ Implement Still ~~ I believe this already works?
                p.closeInventory();
                if (!groupCreationWaitList.contains(p))
                    groupCreationWaitList.add(p);
                p.sendMessage(ChatColor.GREEN + "Enter Name of New Group: ");
            }
            //Open List of Groups to Edit
            else if (ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).equals("Edit Groups")) {
                info.put("action", "list_groups");
                p.openInventory(InventoryAPI.createInventoryPage("group", 1, "Groups > Edit", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
            }
            //Open List of Groups to Delete
            else if (ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).equals("Delete Groups")) {
                info.put("action", "list_groups");
                p.openInventory(InventoryAPI.createInventoryPage("group", 1, "Groups > Delete", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
            }
            else if (clicked.getType().equals(Material.ARROW)) {
                p.openInventory(GuiMainCommand.createMainGUI());
            }
        }
        //This is where you Choose a group to edit
        else if (e.getView().getTitle().equalsIgnoreCase("Groups > Edit")) {
            //Handle ARROW Clicks -- Go Back a Page
            if (clicked.getType().equals(Material.ARROW)) {
                //Go to Main Menu
                if (paginatedGuiHolder.getPage() == 1)
                    p.openInventory(GroupsCmd.createMainGUI());
                //Go Back a Page of Listed Groups
                else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Back")) {
                    info.put("action", "list_groups");
                    p.openInventory(InventoryAPI.createInventoryPage("group", paginatedGuiHolder.getPage()-1, "Groups > Edit", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
                }
                //Go to Next Page of Listed Groups
                else if (clicked.getItemMeta().getDisplayName().contains("Next")){
                    info.put("action", "list_groups");
                    p.openInventory(InventoryAPI.createInventoryPage("group", paginatedGuiHolder.getPage()+1, "Groups > Edit", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
                }
            }
            //Open Group Maintenence Menu for <Group_Name>
            else if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                p.openInventory(groupMaintenanceMenu(clicked.getItemMeta().getDisplayName().toLowerCase()));

            }
            //Close Inventory
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }
        //This is where you Choose a group to delete
        else if (e.getView().getTitle().equalsIgnoreCase("Groups > Delete")) {
            //Selected Group to Delete
            if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                SettingsManager.getGroup(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase()).deleteGroup();
                p.closeInventory();
                p.sendMessage(ChatColor.GREEN + "Group Deleted: " + ChatColor.WHITE + clicked.getItemMeta().getDisplayName());
                info.put("action", "list_groups");
                p.openInventory(InventoryAPI.createInventoryPage("group", 1, "Groups > Delete", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
            }
            //Go Back or Forward a Page of Listed Groups
            else if (clicked.getType().equals(Material.ARROW)) {
                if (paginatedGuiHolder.getPage() == 1)
                    p.openInventory(GroupsCmd.createMainGUI());
                else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Back")){
                    info.put("action", "list_groups");
                    p.openInventory(InventoryAPI.createInventoryPage("group", paginatedGuiHolder.getPage()-1, "Groups > Delete", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
                }
                else if (clicked.getItemMeta().getDisplayName().contains("Next")){
                    info.put("action", "list_groups");
                    p.openInventory(InventoryAPI.createInventoryPage("group", paginatedGuiHolder.getPage()+1, "Groups > Delete", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
                }
            }
            //Close Inventory
            else if (clicked.getType().equals(Material.BARRIER)) {
                p.closeInventory();
            }
        }

        //Handle Group Maintenance GUI Clicks
        else if (e.getView().getTitle().contains("Group >") && paginatedGuiHolder.getType().equalsIgnoreCase("group") && paginatedGuiHolder.getInfo().containsKey("group_name")) {

            //Open one of many Group GUI's (add_permission, remove_permission, inheritance)
            if (paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("maintenance")) {

                //Open GUI Listing Every Plugin
                if (clicked.getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
                    p.openInventory(listPluginsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), 1));
                }

                //Group Inheritance Editor
                else if (clicked.getType().equals(Material.PURPLE_STAINED_GLASS_PANE)) {
                    p.openInventory(groupListInheritanceMenu(paginatedGuiHolder.getInfo().get("group_name"), 1));
                }

                //Group Permission Remover
                else if (clicked.getType().equals(Material.YELLOW_STAINED_GLASS_PANE)) {
                    info.put("action", "remove_permissions");
                    info.put("group_name", paginatedGuiHolder.getInfo().get("group_name"));

                    p.openInventory(InventoryAPI.createInventoryPage("group",
                            1,
                            "Group > " + paginatedGuiHolder.getInfo().get("group_name") + " > Remove Permissions",
                            InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name").toLowerCase()).getPerms()),
                            info));
                }

                //Prefix Changer
                else if (clicked.getType().equals(Material.LIGHT_BLUE_STAINED_GLASS_PANE)) {
                    if (!prefixChangeWaitList.containsKey(p))
                        prefixChangeWaitList.put(p, paginatedGuiHolder.getInfo().get("group_name"));

                    p.sendMessage(ChatColor.GREEN + "Enter New Preifx for " + paginatedGuiHolder.getInfo().get("group_name") + ":");
                    p.closeInventory();
                }
                //Go to Group List
                else if (clicked.getType().equals(Material.ARROW)) {
                    info.put("action", "list_groups");
                    p.openInventory(InventoryAPI.createInventoryPage("group", 1, "Groups > Edit", InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroupNames()), info));
                }

            }

            //Handle Listed Plugins GUI Clicks
            else if (paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("list_plugins")) {

                //Clicked on a Plugin
                if (clicked.getType().equals(Material.PURPLE_STAINED_GLASS_PANE)) {
                    p.openInventory(listPluginPermsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), ChatColor.stripColor(clicked.getItemMeta().getDisplayName()), 1));
                }
                //Clicked on Back/Forward Button in Listed Plugins GUI
                else if (clicked.getType().equals(Material.ARROW)) {
                    //Go to Maintenance Page
                    if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                        p.openInventory(groupMaintenanceMenu(paginatedGuiHolder.getInfo().get("group_name")));
                    }
                    //Go Back a Page of Listed Plugins (Damn that's a lot of plugins)
                    else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")){
                        p.openInventory(listPluginsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage()-1));
                    }
                    //Go to Next Page of Listed Plugins
                    else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                        p.openInventory(listPluginsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage()+1));
                    }
                }
                //Close Inventory
                else if (clicked.getType().equals(Material.BARRIER)) {
                    p.closeInventory();
                }
            }

            //Handle Group Permissions List GUI Clicks - Done?
            else if (paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("remove_permissions")) {
                //Remove Permission from Group
                if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).remPerm(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Re-Add Permission to Group
                else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).addPerm(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Handle Forward/Back Button
                else if (clicked.getType().equals(Material.ARROW)) {
                    if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                        p.openInventory(groupMaintenanceMenu(paginatedGuiHolder.getInfo().get("group_name")));
                    }
                    else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")){
                        p.openInventory(groupListPermsMenu(paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage()-1));
                    }
                    else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                        p.openInventory(groupListPermsMenu(paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage()+1));
                    }
                }
                //Close Inventory
                else if (clicked.getType().equals(Material.BARRIER)) {
                    p.closeInventory();
                }
            }

            //Handle Inheritance GUI Clicks
            else if (paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("inheritance")) {
                //Remove Group From Inheritance
                if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).removeInheritance(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Add Group to Inheritance
                else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).addInheritance(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Handle Forward/Back Button
                else if (clicked.getType().equals(Material.ARROW)) {
                    if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                        p.openInventory(groupMaintenanceMenu(paginatedGuiHolder.getInfo().get("group_name")));
                    }
                    else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                        p.openInventory(groupListInheritanceMenu(paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage() - 1));
                    }
                    else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                        p.openInventory(groupListInheritanceMenu(paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getPage() + 1));
                    }
                }
                //Close Inventory
                else if (clicked.getType().equals(Material.BARRIER)) {
                    p.closeInventory();
                }
            }
        }
        else if (paginatedGuiHolder.getType().equalsIgnoreCase("plugin") && paginatedGuiHolder.getInfo().containsKey("group_name") && paginatedGuiHolder.getInfo().containsKey("plugin_name")) {
            if (paginatedGuiHolder.getInfo().get("action").equalsIgnoreCase("list_permissions") && paginatedGuiHolder.getType().equalsIgnoreCase("plugin")) {

                //Clicked on Forward/Back Button on Listed Permissions
                if (clicked.getType().equals(Material.ARROW)) {
                    //Go Back to Plugins List Page
                    if (paginatedGuiHolder.getPage() == 1 && clicked.getItemMeta().getDisplayName().contains("Previous")) {
                        p.openInventory(listPluginsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), 1));
                    }
                    //Go Back a Page of Listed Plugin Permissions
                    else if (paginatedGuiHolder.getPage() > 1 && clicked.getItemMeta().getDisplayName().contains("Previous")){
                        p.openInventory(listPluginPermsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getInfo().get("plugin_name"), paginatedGuiHolder.getPage()-1));
                    }
                    //Go to Next Page of Listed Plugins
                    else if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                        p.openInventory(listPluginPermsMenu("group", paginatedGuiHolder.getInfo().get("group_name"), paginatedGuiHolder.getInfo().get("plugin_name"), paginatedGuiHolder.getPage()+1));
                    }
                }
                //Remove Permission from Group
                else if (clicked.getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).remPerm(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Add Permission to Group
                else if (clicked.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                    SettingsManager.getGroup(paginatedGuiHolder.getInfo().get("group_name")).addPerm(ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
                    e.getClickedInventory().setItem(e.getSlot(), InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, clicked.getItemMeta().getDisplayName()));
                }
                //Close Inventory
                else if (clicked.getType().equals(Material.BARRIER)) {
                    p.closeInventory();
                }

            }
        }
    }

    //Create New Group Chat Event
    @EventHandler
    public static void onChatEvent(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        if (groupCreationWaitList.contains(p)) {
            if (e.getMessage().contains(" ")) {
                p.sendMessage(ChatColor.RED + "Spaces are Not Allow! Try Another Name:");
            }
            else  {
                try {
                    SettingsManager.getInstance().createGroup(e.getMessage());
                    p.sendMessage(ChatColor.GREEN + "New Group Created: " + ChatColor.WHITE + e.getMessage());
                }
                catch (Exception exception) {
                    groupCreationWaitList.remove(p);
                    p.sendMessage(ChatColor.RED + "Error: " + exception);

                }

            }

            e.setCancelled(true);
        }
        else if (prefixChangeWaitList.containsKey(p)) {
            e.setCancelled(true);
            SettingsManager.getGroup(prefixChangeWaitList.get(p)).setPrefix(e.getMessage());
            prefixChangeWaitList.remove(p);
            p.sendMessage(ChatColor.GREEN + "Prefix Changed to: " + ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }
    }

    public static Inventory groupListPermsMenu(String group, int page) {
        return InventoryAPI.createInventoryPage("group",
                page,
                "Group > " + group + " > Permissions",
                InventoryAPI.createSimpleItemStacks(Material.LIME_STAINED_GLASS_PANE, SettingsManager.getGroup(group.toLowerCase()).getPerms()));
    }

    public static Inventory groupMaintenanceMenu(String group) {
        Inventory change = null;
        InventoryHolder holder = new PaginatedGuiHolder("group", 1, 1, Map.of("group_name", group, "action", "maintenance"));
        change = Bukkit.createInventory(holder, 9, "Group > " + group);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        ItemStack addPerm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta addMeta = addPerm.getItemMeta();

        ItemStack inheritance = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta inherMeta = inheritance.getItemMeta();

        ItemStack removePerm = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta removeMeta = removePerm.getItemMeta();

        ItemStack prefix = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta prefixMeta = prefix.getItemMeta();

        ItemStack black = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta blackMeta = black.getItemMeta();

        backMeta.setDisplayName("Back");
        addMeta.setDisplayName("Add Permissions");
        inherMeta.setDisplayName("Change Inheritance");
        removeMeta.setDisplayName("Remove Permissions");
        prefixMeta.setDisplayName("Edit Prefix");
        blackMeta.setDisplayName(" ");

        back.setItemMeta(backMeta);
        addPerm.setItemMeta(addMeta);
        inheritance.setItemMeta(inherMeta);
        removePerm.setItemMeta(removeMeta);
        prefix.setItemMeta(prefixMeta);
        black.setItemMeta(blackMeta);

        change.setItem(0, back); //Go Back a Page

        change.setItem(1, black);

        change.setItem(2, addPerm); //Add Permission Button

        change.setItem(3, black);

        change.setItem(4, removePerm); //Remove Permission Button

        change.setItem(5, black);

        change.setItem(6, inheritance); //Change Inheritance

        change.setItem(7, black);

        change.setItem(8, prefix); //Change Prefix

        return change;
    }

    public static Inventory listPluginPermsMenu(String type, String groupPlayer, String plugin, int page) {
        Map<String, String> info = new HashMap<>();

        if (type.equalsIgnoreCase("group"))
            info.put("group_name", groupPlayer);
        else if (type.equalsIgnoreCase("player"))
            info.put("player_name", groupPlayer);
        info.put("plugin_name", plugin);
        info.put("action", "list_permissions");

        List<ItemStack> permsStack = new ArrayList<>();
        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            if (perm.getName().startsWith(plugin.toLowerCase() + ".") && !perm.getName().contains("hat.prevent-type") && !Bukkit.getPluginManager().getPlugin(ChatColor.stripColor(plugin)).getDescription().getPermissions().contains(perm)) {
                Material permItem;
                if (type.equalsIgnoreCase("group")) permItem = SettingsManager.getGroup(groupPlayer).hasPerm(perm.getName()) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                else permItem = Bukkit.getPlayer(groupPlayer).hasPermission(perm.getName()) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

                permsStack.add(InventoryAPI.createGuiItem(permItem, perm.getName()));
            }
        }

        //Used in case they shortended Plugin name for permissions
        for (Permission perm : Bukkit.getPluginManager().getPlugin(ChatColor.stripColor(plugin)).getDescription().getPermissions()) {
            Material permItem;
            if (type.equalsIgnoreCase("group")) permItem = SettingsManager.getGroup(groupPlayer).hasPerm(perm.getName()) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            else permItem = Bukkit.getPlayer(groupPlayer).hasPermission(perm.getName()) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            permsStack.add(InventoryAPI.createGuiItem(permItem, perm.getName()));
        }

        permsStack.sort(Comparator.comparing(item -> item.getItemMeta().getDisplayName()));

        return InventoryAPI.createInventoryPage("plugin", page, plugin + " > Permissions", permsStack, info);
    }

    public static Inventory listPluginsMenu(String type, String groupPlayer, int page) {
        Map<String, String> info = new HashMap<>();
        info.put("action", "list_plugins");

        if (type.equalsIgnoreCase("group")) {
            info.put("group_name", groupPlayer);
            return InventoryAPI.createInventoryPage("group",
                    page,
                    "Group > " + groupPlayer + " > Add Permissions",
                    InventoryAPI.createSimpleItemStacks(Material.PURPLE_STAINED_GLASS_PANE, getPluginNames()),
                    info);
        }
        else {
            info.put("player_name", groupPlayer);
            return InventoryAPI.createInventoryPage("player",
                    page,
                    "Player > " + groupPlayer + " > Add Permissions",
                    InventoryAPI.createSimpleItemStacks(Material.PURPLE_STAINED_GLASS_PANE, getPluginNames()),
                    info);
        }

    }

    public static Inventory groupListInheritanceMenu(String g, int page) {
        Map<String, String> info = new HashMap<>();
        info.put("action", "inheritance");
        info.put("group_name", g);
        List<ItemStack> groupStacks = new ArrayList<>();

        for (Groups group : SettingsManager.groups.values()) {
            if (group.getName().equalsIgnoreCase(g)) continue;
            if (SettingsManager.getGroup(g.toLowerCase()) == null || SettingsManager.getGroup(g.toLowerCase()).getInheritance() == null) continue;

            ItemStack groupItem = (SettingsManager.getGroup(g.toLowerCase()).getInheritance().contains(group.getName())) ?
                    InventoryAPI.createGuiItem(Material.LIME_STAINED_GLASS_PANE, group.getName()) : //Is Already Inherited
                    InventoryAPI.createGuiItem(Material.RED_STAINED_GLASS_PANE, group.getName()); //Not Inherited
            groupStacks.add(groupItem);
        }

        return InventoryAPI.createInventoryPage("group",
                page,
                "Group > " + g + " > Inheritance",
                groupStacks,
                info);
    }

    public static List<Plugin> getPlugins(int start) {
        List<Plugin> plugins = new ArrayList<>();

        for (int i = start; i< PermsMain.getPm().getPlugins().length; i++) {
            plugins.add(PermsMain.getPm().getPlugins()[i]);
        }

        return plugins;
    }

    public static List<String> getPluginNames() {
        List<String> pluginStrings = new ArrayList<>();
        for (Plugin pl : getPlugins(0)) {
            pluginStrings.add(pl.getName());
        }
        return pluginStrings;
    }
}
