package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Gui.InventoryAPI;
import com.VintageGaming.VintagePerms.Gui.PaginatedGuiHolder;
import com.VintageGaming.VintagePerms.Management.Users;
import com.VintageGaming.VintagePerms.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UsersCmd extends PermsCommand {

    //THIS REDIRECTS TO Group.java AFTER PERMISSION CHECK

    public UsersCmd() {
        super("users", "");
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (sender instanceof Player && (sender.hasPermission("vperms.users.*") || sender.hasPermission("vperms.*"))) {
            Player p = (Player) sender;
            p.openInventory(createMainGUI(1));
        }
        else {
            sender.sendMessage(ChatColor.RED + "You Don't Have Permission!");
        }
    }

    public static Inventory createMainGUI(int page) {
        List<ItemStack> players = new ArrayList<>();

        for (Users user : SettingsManager.getInstance().getUsers()) {
            ItemStack playerHead = createPlayerHead(Bukkit.getOfflinePlayer(UUID.fromString(user.getUuid())));
            players.add(playerHead);
        }

        return InventoryAPI.createInventoryPage("players", page, "Vintage Perms > Users", players);
    }

    public static ItemStack createPlayerHead(OfflinePlayer player) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = playerHead.getItemMeta();

        // We must check if the meta is a SkullMeta before we can use it.
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;

            skullMeta.setOwningPlayer(player);

            skullMeta.setDisplayName(player.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to manage this player.");
            if (player.isOnline()) lore.add(ChatColor.GREEN + "Online");
            else lore.add(ChatColor.RED + "Offline");
            skullMeta.setLore(lore);

            playerHead.setItemMeta(skullMeta);
        }

        return playerHead;
    }
}
