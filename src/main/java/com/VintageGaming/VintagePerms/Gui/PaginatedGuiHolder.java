package com.VintageGaming.VintagePerms.Gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;

public class PaginatedGuiHolder implements InventoryHolder {


    private final String type;
    private final int page;
    private final int totalPages;
    private final Map<String, String> info;

    public PaginatedGuiHolder(String type, int page, int totalPages, Map<String, String> info) {
        this.type = type; //main, group, player, players, plugin
        this.page = page; //Current Page
        this.totalPages = totalPages; //Number of total Pages
        this.info = info; // Extra info to determine actions (group_name, player_name, plugin_name)
    }

    public PaginatedGuiHolder(String type, int page, int totalPages) {
        this(type, page, totalPages, null);
    }

    public String getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Map<String, String> getInfo() {
        return info;
    }


    @Override
    public Inventory getInventory() {
        return null;
    }
}
