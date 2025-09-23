package com.VintageGaming.VintagePerms.Commands;

import com.VintageGaming.VintagePerms.Management.Groups;
import com.VintageGaming.VintagePerms.PermsMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import com.VintageGaming.VintagePerms.SettingsManager;

import java.util.Arrays;
import java.util.List;

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

        String groupName = args[0].toLowerCase();
        Groups group = SettingsManager.getGroup(groupName);

        //Group Doesn't Exist and Create Action isn't being Used
        if (group == null && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Group doesn't exist!");
            return;
        }

        //List Group Info
        if (args.length < 2) {
            if (CommandPermission.GROUP_LIST_PERMS.hasForGroup(sender, groupName)) {

                boolean isDefault = false;

                if (SettingsManager.getInstance().getGroupSection(groupName).getBoolean("default"))
                    isDefault = true;

                if (group.getPerms().isEmpty()) {
                    sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + groupName + ChatColor.GOLD + ">");
                    sender.sendMessage(ChatColor.GOLD + "Default: " + ChatColor.RESET + isDefault);
                    sender.sendMessage(ChatColor.YELLOW + "No permissions for " + groupName);
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + groupName + ChatColor.GOLD + ">");
                sender.sendMessage(ChatColor.GOLD + "Default: " + ChatColor.RESET + isDefault);
                sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Permissions:" + ChatColor.GOLD + ">");

                int perms = 0;

                for (String perm : group.getPerms()) {
                    perms += 1;

                    sender.sendMessage(ChatColor.GOLD + String.valueOf(perms) + ") " + ChatColor.YELLOW + perm);
                }

                perms = 0;

                return;
            }
        }

        String action = args[1].toLowerCase();

        //Group Exists - Group Actions
        switch (action) {
            case "delete":
                if (CommandPermission.GROUP_DELETE.hasForGroup(sender, groupName)) {
                    for (Player player : PermsMain.instance.getServer().getOnlinePlayers()) {
                        if (SettingsManager.getInstance().getUser(player).getGroups().contains(groupName)) {
                            SettingsManager.getInstance().getUser(player).removeGroup(groupName);
                        }
                    }

                    group.deleteGroup();

                    for (Player onlinePlayer : PermsMain.instance.getServer().getOnlinePlayers()) {
                        SettingsManager.getInstance().getUser(onlinePlayer).removeGroup(groupName);
                    }

                    sender.sendMessage(ChatColor.GREEN + "Deleted Group " + groupName + ".");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            case "create":
                if (CommandPermission.GROUP_CREATE.has(sender)) {
                    SettingsManager.getInstance().createGroup(groupName);
                    sender.sendMessage(ChatColor.GREEN + "Created group!");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            case "add":
                if (CommandPermission.GROUP_ADD_PERM.hasForGroup(sender, groupName)) {
                    group.addPerm(args[2].toLowerCase());
                    sender.sendMessage(ChatColor.GREEN + "Added " + args[2] + " to group " + groupName + ".");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            case "remove":
                if (CommandPermission.GROUP_REMOVE_PERM.hasForGroup(sender, groupName)) {
                    group.remPerm(args[2].toLowerCase());
                    sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from group " + groupName + ".");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            case "prefix":
                if (CommandPermission.GROUP_SET_PREFIX.hasForGroup(sender, groupName)) {
                    group.setPrefix(args[2]);

                    sender.sendMessage(ChatColor.GREEN + "Set Group Prefix!");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            case "parents":
                if (CommandPermission.GROUP_PARENTS_MODIFY.hasForGroup(sender, groupName) || CommandPermission.GROUP_PARENTS_LIST.hasForGroup(sender, groupName)) {
                    if (args[2].equalsIgnoreCase("add") && CommandPermission.GROUP_PARENTS_MODIFY.hasForGroup(sender, groupName)) {
                        String[] groups;

                        if (args[3].contains(",") && !args[3].contains(" ")) {
                            groups = args[3].toLowerCase().split(",");
                        }

                        else {
                            String joinedGroups = args[3].toLowerCase();
                            if (args.length > 4) {
                                String[] groupSpaced = Arrays.copyOfRange(args, 4, args.length);

                                for (String g : groupSpaced) {
                                    if (!SettingsManager.getGroupNames().contains(g.toLowerCase())) continue;

                                    if (!g.contains(",")) String.join(", ", joinedGroups, g);
                                    else String.join(", ", joinedGroups, g.replace(",", ""));
                                }
                            }
                            groups = joinedGroups.split(", ");
                        }
                        for (String g : groups) {
                            if (!SettingsManager.getGroupNames().contains(g.toLowerCase())) continue;
                            group.addInheritance(g.toLowerCase());
                        }
                    }
                    else if (args[2].equalsIgnoreCase("remove") && CommandPermission.GROUP_PARENTS_MODIFY.hasForGroup(sender, groupName)) {
                        if (!SettingsManager.getGroupNames().contains(args[3].toLowerCase())) return;
                        group.removeInheritance(args[3].toLowerCase());
                    }
                    else {
                        if (CommandPermission.GROUP_PARENTS_LIST.hasForGroup(sender, groupName)) {
                            List<String> parents = group.getInheritance();
                            if (parents.isEmpty()) {
                                sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + ChatColor.WHITE + groupName + ChatColor.AQUA + " Parents>");
                                sender.sendMessage(ChatColor.GOLD + "No Parents");
                                return;
                            }

                            sender.sendMessage(ChatColor.GOLD + "<" + ChatColor.AQUA + "Group: " + ChatColor.WHITE + groupName + ChatColor.AQUA + " Parents>");
                            for (String g : parents) {
                                sender.sendMessage(ChatColor.GOLD + g);
                            }
                        }
                        else sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Insufficient Permissions!");
                }
                return;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid Action!");

        }
    }

    public GroupCmd() {
        super("group", "<name> [<add | remove | create | delete | parents | prefix> <perm | add | remove | prefix> <parent>]");
    }
}
