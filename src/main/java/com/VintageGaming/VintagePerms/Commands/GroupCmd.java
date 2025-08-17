package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Management.Groups;
import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.VintageGaming.VintagePerms.SettingsManager;

import java.util.Arrays;

public class GroupCmd extends PermsCommand {


    /*
    THIS IS THE /vperms group COMMAND CLASS ------------ NOT CONNECTED TO GUI ----- GUI IS UNDER Groups.java OR /vperms groups

        Permissions for this Command:
            - vperms.groups.create
            - vperms.groups.remove
            - vperms.groups.addperm
            - vperms.groups.removeperm
            - vperms.groups.listperms
            - vperms.groups.setprefix
            - vperms.groups.listgroups

        Per Group Permissions:
            - vperms.groups.GROUP.addplayers
            - vperms.groups.GROUP.removeplayers
            - vperms.groups.GROUP.addperm
            - vperms.groups.GROUP.removeperm
            - vperms.groups.GROUP.delete
     */

    public void run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if ((!(sender instanceof Player) || sender.hasPermission("vperms.groups.listgroups") || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.*"))) {
                if (SettingsManager.groups.size() == 0)
                    sender.sendMessage(ChatColor.RED + "There are no Groups Created!");

                else {
                    int amount = SettingsManager.groups.size();

                    sender.sendMessage(ChatColor.GOLD+"<"+ChatColor.AQUA+"Groups: "+amount+ChatColor.GOLD+">");

                    for (int g=0;g<amount;g++) {
                        sender.sendMessage(ChatColor.GOLD + String.valueOf(g+1) + ChatColor.YELLOW + ". " + SettingsManager.groups.get(g).getName());
                    }

                }
            }

            else
                sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");

            return;
        }

        String g = args[0].toLowerCase();

        if (args.length < 2) {
            if (SettingsManager.getGroup(g.toLowerCase()) != null && (sender.hasPermission("vperms.groups.listperms") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.*"))) {

                if (!SettingsManager.getGroupNames().contains(g)) {
                    sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
                    return;
                }

                Boolean isDefault = false;

                if (SettingsManager.getInstance().getGroupSection(g.toLowerCase()).getBoolean("default"))
                    isDefault = true;

                if (SettingsManager.getGroup(g.toLowerCase()).getPerms().size() == 0) {
                    sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + g.toLowerCase() + ChatColor.GOLD + ">");
                    sender.sendMessage(ChatColor.GOLD + "Default: " + ChatColor.RESET + isDefault);
                    sender.sendMessage(ChatColor.YELLOW + "No permissions for " + g.toLowerCase());
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + g + ChatColor.GOLD + ">");
                sender.sendMessage(ChatColor.GOLD + "Default: " + ChatColor.RESET + isDefault);
                sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Permissions:" + ChatColor.GOLD + ">");

                int perms = 0;

                for (String perm : SettingsManager.getGroup(g).getPerms()) {
                    perms += 1;

                    sender.sendMessage(ChatColor.GOLD + String.valueOf(perms) + ") " + ChatColor.YELLOW + perm);
                }

                perms = 0;

                return;
            }
        }
        else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("delete") && (sender.hasPermission("vperms.groups." + g.toLowerCase() + ".delete") || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + g.toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {

                if (!SettingsManager.getGroupNames().contains(g)) {
                    sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
                    return;
                }

                for (Player player : PermsMain.instance.getServer().getOnlinePlayers()) {
                    if (SettingsManager.getInstance().getUser(player).getGroups().contains(g)) {
                        SettingsManager.getInstance().getUser(player).removeGroup(SettingsManager.getGroup(g));
                    }
                }

                SettingsManager.getGroup(g).deleteGroup();




                for (Player onlinePlayer : PermsMain.instance.getServer().getOnlinePlayers()) {
                    SettingsManager.getInstance().getUser(onlinePlayer).removeGroup(SettingsManager.getGroup(g));
                }

                sender.sendMessage(ChatColor.GREEN + "Deleted Group " + g.toLowerCase() + ".");
                return;
            }
            else if (args[1].equalsIgnoreCase("create")) {
                if (sender.hasPermission("vperms.groups.create") || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.*")){
                    SettingsManager.getInstance().createGroup(g);
                    sender.sendMessage(ChatColor.GREEN + "Created group!");
                    return;
                }

                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                    return;
                }
            }
        }

        else if (args.length == 3) {
            if (!SettingsManager.getGroupNames().contains(g)) {
                sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
                return;
            }

            if (args[1].equalsIgnoreCase("add") && (sender.hasPermission("vperms.groups." + g.toLowerCase() + ".addperm") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + g.toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                SettingsManager.getGroup(g).addPerm(args[2].toLowerCase());
                sender.sendMessage(ChatColor.GREEN + "Added " + args[2] + " to group " + g.toLowerCase() + ".");
                return;
            }

            else if (args[1].equalsIgnoreCase("remove") && (sender.hasPermission("vperms.groups." + g.toLowerCase() + ".removeperm") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + g.toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                SettingsManager.getGroup(g).remPerm(args[2].toLowerCase());
                sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from group " + g.toLowerCase() + ".");
                return;
            }

            else if (args[1].equalsIgnoreCase("prefix") && (sender.hasPermission("vperms.groups.setprefix") || !(sender instanceof Player) || sender.hasPermission("vperms.groups.*") || sender.hasPermission("vperms.groups." + g.toLowerCase() + ".*") || sender.hasPermission("vperms.*"))) {
                SettingsManager.getGroup(g).setPrefix(args[2]);

                sender.sendMessage(ChatColor.GREEN + "Set Group Prefix!");
                return;
            }
        }

        else if (args.length == 4 && args[1].equalsIgnoreCase("parents")) {
            if (!SettingsManager.getGroupNames().contains(g)) {
                sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
                return;
            }

            if (args[2].equalsIgnoreCase("add")) {
                String[] groups;

                if (args[3].contains(",") && !args[3].contains(" ")) {
                    groups = args[3].toLowerCase().split(",");
                }

                else {
                    String joinedGroups = args[3].toLowerCase();
                    if (args.length > 4) {
                        String[] groupSpaced = Arrays.copyOfRange(args, 4, args.length);

                        for (String group : groupSpaced) {
                            if (!SettingsManager.getGroupNames().contains(group.toLowerCase())) continue;

                            if (!group.contains(",")) String.join(", ", joinedGroups, group);
                            else String.join(", ", joinedGroups, group.replace(",", ""));
                        }
                    }
                    groups = joinedGroups.split(", ");
                }
                for (String group : groups) {
                    if (!SettingsManager.getGroupNames().contains(group.toLowerCase())) continue;
                    SettingsManager.getGroup(g).addInheritance(group.toLowerCase());
                }
            }
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!SettingsManager.getGroupNames().contains(args[3].toLowerCase())) return;
                SettingsManager.getGroup(g).removeInheritance(args[3].toLowerCase());
            }

        }
        else if (args[1].equalsIgnoreCase("parents") && args[2].equalsIgnoreCase("list")) {
            if (!SettingsManager.getGroupNames().contains(g)) {
                sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
                return;
            }
            sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + ChatColor.WHITE + g + ChatColor.AQUA + " Parents>");
            for (Groups group : SettingsManager.groups.values()) {
                sender.sendMessage(ChatColor.GOLD + group.getName());
            }
        }
    }

    public GroupCmd() {
        super("group", "<name> [<add | remove | create | delete | prefix> <perm | prefix>]");
    }
}
