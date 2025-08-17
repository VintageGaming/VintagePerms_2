package com.VintageGaming.VintagePerms.Gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InventoryAPI {

    private static final int PAGE_ITEM_SLOTS = 45;

    public static Inventory createInventoryPage(String type, int page, String title, List<ItemStack> allItems) {
        return createInventoryPage(type, page, title, allItems, null);
    }

    public static Inventory createInventoryPage(String type, int page, String title, List<ItemStack> allItems, Map<String, String> info) {
        //Sizes can be 9, 18, 27, 36, 45, 54

        // Calculate the total number of pages needed.
        int totalPages = (int) Math.ceil((double) allItems.size() / PAGE_ITEM_SLOTS);
        if (totalPages == 0) totalPages = 1;

        // Create the inventory with a dynamic title showing the current page.
        String pagedTitle = title;
        InventoryHolder holder = new PaginatedGuiHolder(type, page, totalPages, info);

        if (allItems.size() > PAGE_ITEM_SLOTS)
            pagedTitle = String.format("%s &7(&e%d&7/&e%d&7)", title, page, totalPages);
        Inventory inv = Bukkit.createInventory(holder, 54, ChatColor.translateAlternateColorCodes('&', pagedTitle));

        // Create the filler item for the background.
        ItemStack fillerItem = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        // Fill the entire inventory with the filler item first.
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, fillerItem);
        }

        // --- Populate the page with items ---
        int startIndex = (page - 1) * PAGE_ITEM_SLOTS;
        int endIndex = Math.min(startIndex + PAGE_ITEM_SLOTS, allItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            inv.setItem(i - startIndex, allItems.get(i));
        }

        // Previous Page Button
        ItemStack prevPage = createGuiItem(Material.ARROW, "&a<- Previous Page", Collections.singletonList("&7Go to page " + (page - 1)));
        inv.setItem(45, prevPage);

        // Next Page Button
        if (page < totalPages) {
            ItemStack nextPage = createGuiItem(Material.ARROW, "&aNext Page ->", Collections.singletonList("&7Go to page " + (page + 1)));
            inv.setItem(53, nextPage);
        }

        // Back/Close Button - Might Delete this Button
        ItemStack closeButton = createGuiItem(Material.BARRIER, "&cClose Menu");
        inv.setItem(49, closeButton);


        return inv;

    }

    public static ItemStack createGuiItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> formattedLore = new ArrayList<>();
            for (String line : loreLines) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(formattedLore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createGuiItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> formattedLore = new ArrayList<>();
            formattedLore.add(ChatColor.translateAlternateColorCodes('&', lore));

            meta.setLore(formattedLore);

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createGuiItem(Material material, String name) {
        return createGuiItem(material, name, Collections.emptyList());
    }

    public static List<ItemStack> createSimpleItemStacks(Material material, List<String> items) {
        List<ItemStack> simpleItems = new ArrayList<>();
        for (String item : items) {
            ItemStack stack = new ItemStack(material);
            ItemMeta meta = stack.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', item));
                stack.setItemMeta(meta);
            }

            simpleItems.add(stack);
        }
        return simpleItems;
    }
}
