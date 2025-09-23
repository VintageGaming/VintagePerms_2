package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Gui.UserUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.VintageGaming.VintagePerms.SettingsManager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserCmd extends PermsCommand {


    /*
    THIS IS THE /vperms user COMMAND ------------ NOT CONNECTED TO USERS GUI ------ GUI IS UNDER UsersCmd.java

        Permissions for this Command:
            - vperms.user.addperm
            - vperms.user.removeperm
            - vperms.user.listperms
            - vperms.user.*
     */

    public void run(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("vperms.user.addperm") || sender.hasPermission("vperms.user.removeperm") || sender.hasPermission("vperms.user.listperms") || sender.hasPermission("vperms.*") || !(sender instanceof Player))) {
            sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You didn't enter a user.");
            return;
        }

        String p = args[0].toLowerCase();
        SettingsManager manager = SettingsManager.getInstance();

        //List User Info
        if (args.length < 2) {
            if (CommandPermission.USER_LIST.has(sender)) {
                if (manager.getUser(p).getPermissions().isEmpty() && manager.getUser(p).getGroups().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No permissions for " + p);
                    return;
                }

                sender.sendMessage(ChatColor.GOLD+"<"+ChatColor.AQUA+"Player: "+p+ChatColor.GOLD+">");

                String groups = "";
                List<String> playerGroups = manager.getUser(p).getGroups();

                for (int where=0; where < playerGroups.size(); where++) {
                    if (where == playerGroups.size()-1)
                        groups += playerGroups.get(where);

                    else
                        groups += playerGroups.get(where) + ", ";

                }

                sender.sendMessage(ChatColor.GOLD+"Groups: " + ChatColor.RESET + groups);

                List<String> perms = manager.getUser(p).getPermissions();

                for (int perm=0;perm<perms.size();perm++) {
                    sender.sendMessage(perm + ") "+ ChatColor.YELLOW + perms.get(perm));
                }

                return;

            }

            sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");

        }


        String action = args[1].toLowerCase();

        switch (action) {
            case "add":
                if (CommandPermission.USER_ADD.has(sender)) {
                    manager.getUser(p).addPermission(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Added " + args[2] + " to " + p);
                    return;
                }

                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;
            case "remove":
                if (CommandPermission.USER_REMOVE.has(sender)) {
                    manager.getUser(p).removePermission(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from " + p);
                    return;
                }

                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;
            case "gui":
                if (CommandPermission.GROUP_WILDCARD.has(sender) && CommandPermission.USER_WILDCARD.has(sender)) {
                    Player player = (Player) sender;
                    player.openInventory(UserUI.playerMaintenenceMenu(args[0]));
                    return;
                }

                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;
            case "group":
                if (args[2].equalsIgnoreCase("add") && CommandPermission.GROUP_ADD_PLAYERS.hasForGroup(sender, args[3].toLowerCase())) {
                    manager.getUser(p).addGroup(args[3], manager.getUser(p).getGroups().size());
                    sender.sendMessage(ChatColor.GREEN + "Added " + p + " to group " + args[3] + ".");
                    return;
                }
                else if (args[2].equalsIgnoreCase("remove") && CommandPermission.GROUP_REMOVE_PLAYERS.hasForGroup(sender, args[3].toLowerCase())) {
                    manager.getUser(p).removeGroup(args[3]);
                    sender.sendMessage(ChatColor.GREEN + "Removed " + p + " from group " + args[3].toLowerCase() + ".");
                    return;
                }
                else if (args[2].equalsIgnoreCase("set") && CommandPermission.GROUP_ADD_PLAYERS.has(sender)) {
                    manager.getUser(p).setGroup(args[3]);
                    sender.sendMessage(ChatColor.GREEN + "Set " + p + " Primary Group to " + args[3] + ".");
                    return;
                }
                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid Action!");
        }
    }

    public UserCmd() {
        super("user", "<username> [<add | remove | group | gui> <permission | add | remove | set> <group>]");
    }
}
