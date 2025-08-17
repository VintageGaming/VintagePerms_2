package com.VintageGaming.VintagePerms.injection;

import java.util.List;

import com.VintageGaming.VintagePerms.SettingsManager;

public class VVHook {


    //Functions for Hook into my Custom Vault Plugin

    public String getName() {
        return "VintagePerms";
    }

    public boolean addPlayerPermission(String player, String permission) {
        SettingsManager.getInstance().getUser(player).addPermission(permission);
        return true;
    }

    public boolean removePlayerPermission(String player, String permission) {
        SettingsManager.getInstance().getUser(player).removePermission(permission);
        return true;
    }

    public List<String> getPlayersPermissions(String player) {
        return SettingsManager.getInstance().getUser(player).getPermissions();
    }

    public boolean setPlayerGroup(String player, String group) {
        SettingsManager.getInstance().getUser(player).addGroup(group, SettingsManager.getInstance().getUser(player).getGroups().size()-1);
        return true;
    }

    public boolean createGroup(String group) {
        SettingsManager.getInstance().createGroup(group);
        return true;
    }

    public boolean addGroupPermission(String group, String permission) {
        SettingsManager.getGroup(group.toLowerCase()).addPerm(permission);
        return true;
    }

    public boolean removeGroupPermission(String group, String permission) {
        SettingsManager.getGroup(group.toLowerCase()).remPerm(permission);
        return true;
    }


}
