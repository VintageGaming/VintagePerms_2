package com.VintageGaming.VintagePerms.Commands;

import org.bukkit.command.CommandSender;

public abstract class PermsCommand {

    private String name, args;

    public PermsCommand(String name, String args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public String getArgs() {
        return args;
    }

    public abstract void run(CommandSender sender, String[] args);
}
