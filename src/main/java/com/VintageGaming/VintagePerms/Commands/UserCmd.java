package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Gui.UserUI;
import org.bukkit.ChatColor;
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

        if (args.length < 2) {
            if (sender.hasPermission("vperms.user.listperms") || sender.hasPermission("vperms.*") || !(sender instanceof Player)) {
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
                    sender.sendMessage(String.valueOf(perm) + ") "+ ChatColor.YELLOW + perms.get(perm));
                }

                return;

            }

            sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");

        }

        else if (args.length == 3){
            if (args[1].equalsIgnoreCase("add")) {
                if (sender.hasPermission("vperms.user.addperm") || sender.hasPermission("vperms.*") || !(sender instanceof Player) || sender.hasPermission("vperms.user.*")) {

                    manager.getUser(p).addPermission(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Added " + args[2] + " to " + p);

                    return;
                }

                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;

            }

            else if (args[1].equalsIgnoreCase("remove")) {
                if (sender.hasPermission("vperms.user.removeperm") || sender.hasPermission("vperms.*") || !(sender instanceof Player) || sender.hasPermission("vperms.user.*")) {

                    manager.getUser(p).removePermission(args[2]);
                    sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from " + p);


                    return;

                }

                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                return;

            }
            else if (args[1].equalsIgnoreCase("gui") && sender instanceof Player && (sender.hasPermission("vperms.user.*") || sender.hasPermission("vperms.*"))) {
                Player player = (Player) sender;
                player.openInventory(UserUI.playerMaintenenceMenu(args[0]));
            }
        }
        else if (args.length == 4 && args[1].equalsIgnoreCase("group")) {
            if (args[2].equalsIgnoreCase("add") && (sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".addplayers") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                manager.getUser(p).addGroup(args[3], manager.getUser(p).getGroups().size());
                sender.sendMessage(ChatColor.GREEN + "Added " + p + " to group " + args[3] + ".");
            }

            else if (args[2].equalsIgnoreCase("remove") && (sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".removeplayers") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                manager.getUser(p).removeGroup(args[3]);
                sender.sendMessage(ChatColor.GREEN + "Removed " + p + " from group " + args[3].toLowerCase() + ".");
            }
            else if (args[2].equalsIgnoreCase("set") && (sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".addplayers") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + args[3].toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                manager.getUser(p).setGroup(args[3]);
                sender.sendMessage(ChatColor.GREEN + "Set " + p + " Primary Group to " + args[3] + ".");
            }
        }
    }

    public UserCmd() {
        super("user", "<username> [<add | remove | group | gui> <permission | add | remove | set> <group>]");
    }
}
