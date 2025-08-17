package com.VintageGaming.VintagePerms.injection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.VintageGaming.VintagePerms.PermsMain;

public class ScoreBoardPrefixes {

    public static void updatePrefixes() {
        Bukkit.getServer().getScheduler().runTaskTimer(PermsMain.instance, new Runnable() {
            public void run() {
                if (Bukkit.getServer().getOnlinePlayers().size() == 0) return;
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    p.setScoreboard(PermsMain.ranks);
                }
            }
        }, 0, 600);
    }
}
