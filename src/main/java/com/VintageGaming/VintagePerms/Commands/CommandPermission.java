package com.VintageGaming.VintagePerms.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public enum CommandPermission {

    // --- General Group Commands ---
    GROUP_CREATE("vperms.groups.create"),
    GROUP_LIST("vperms.groups.listgroups"),
    GROUP_LIST_PERMS("vperms.groups.listperms"),
    GROUP_SET_PREFIX("vperms.groups.setprefix"),

    // --- General User Commands ---
    USER_ADD("vperms.user.addperm"),
    USER_REMOVE("vperms.user.removeperm"),
    USER_LIST("vperms.user.listperms"),

    // --- Per-Group Permissions (using %s as a placeholder for the group name) ---
    GROUP_DELETE("vperms.groups.%s.delete"),
    GROUP_ADD_PERM("vperms.groups.%s.addperm"),
    GROUP_REMOVE_PERM("vperms.groups.%s.removeperm"),
    GROUP_ADD_PLAYERS("vperms.groups.%s.addplayers"),
    GROUP_REMOVE_PLAYERS("vperms.groups.%s.removeplayers"),
    GROUP_PARENTS_MODIFY("vperms.groups.%s.parents"), // For adding/removing parents
    GROUP_PARENTS_LIST("vperms.groups.%s.parents.list"),

    // --- Wildcard/Admin Permissions ---
    USER_WILDCARD("vperms.user.*"),
    GROUP_WILDCARD("vperms.groups.*"),
    ADMIN_WILDCARD("vperms.*");

    private final String node;

    CommandPermission(String node) {
        this.node = node;
    }


    public boolean has(CommandSender sender) {
        // 1. Console always has permission.
        if (!(sender instanceof Player)) {
            return true;
        }
        // 2. Check for the specific node, the general group wildcard, and the admin wildcard.
        return sender.hasPermission(this.node)
                || sender.hasPermission(GROUP_WILDCARD.node)
                || sender.hasPermission(ADMIN_WILDCARD.node);
    }

    public boolean hasForGroup(CommandSender sender, String groupName) {
        // 1. Console always has permission.
        if (!(sender instanceof Player)) {
            return true;
        }
        String specificNode = String.format(this.node, groupName.toLowerCase());
        String groupSpecificWildcard = String.format("vperms.groups.%s.*", groupName.toLowerCase());

        // 2. Check for the specific node, the group-specific wildcard, the general group wildcard, and the admin wildcard.
        return sender.hasPermission(specificNode)
                || sender.hasPermission(groupSpecificWildcard)
                || sender.hasPermission(GROUP_WILDCARD.node)
                || sender.hasPermission(ADMIN_WILDCARD.node);
    }
}