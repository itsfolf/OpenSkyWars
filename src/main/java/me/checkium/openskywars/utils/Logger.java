package me.checkium.openskywars.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    private final String name;

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[OpenSkyWars-" + name + "] " + message);
    }

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[OpenSkyWars-" + name + "] " + message);
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[OpenSkyWars-" + name + "] " + message);
    }
}
