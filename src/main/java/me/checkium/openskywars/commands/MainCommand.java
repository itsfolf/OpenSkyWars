package me.checkium.openskywars.commands;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "arena":
                    new ArenaCommand().process(sender, args);
                    break;
                case "world":
                    new WorldCommand().process(sender, args);
                    break;
                case "lobby":
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("set")) {
                            OpenSkyWars.getInstance().getMainConfig().lobbyLocation = Utils.locationToString(((Player) sender).getLocation());
                            sender.sendMessage(ChatColor.GREEN + "Set lobby location to " + OpenSkyWars.getInstance().getMainConfig().lobbyLocation);
                        } else if (args[1].equalsIgnoreCase("join")) {
                            if (!OpenSkyWars.getInstance().getLobby().isInLobby((Player) sender)) {
                                OpenSkyWars.getInstance().getLobby().join((Player) sender);
                            }
                        } else if (args[1].equalsIgnoreCase("leave")) {
                            if (OpenSkyWars.getInstance().getMainConfig().lobbyEnabled && OpenSkyWars.getInstance().getLobby().isInLobby((Player) sender)) {
                                OpenSkyWars.getInstance().getLobby().leave((Player) sender);
                            }
                        }
                    } else {
                        if (OpenSkyWars.getInstance().getMainConfig().lobbyEnabled) {
                            if (OpenSkyWars.getInstance().getLobby().isInLobby((Player) sender)) {
                                OpenSkyWars.getInstance().getLobby().leave((Player) sender);
                            } else {
                                OpenSkyWars.getInstance().getLobby().join((Player) sender);
                            }
                        } else {
                            ((Player) sender).teleport(OpenSkyWars.getInstance().getLobby().getLocation());
                        }
                    }
                default:
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Utils.match(args[0], Arrays.asList("arena", "world", "lobby"));
        } else if (args.length > 1) {
            switch (args[0]) {
                case "arena":
                    return new ArenaCommand().tabComplete(args);
                case "world":
                    return new WorldCommand().tabComplete(args);
                case "lobby":
                    return Utils.match(args[1], Arrays.asList("set", "join", "leave"));
            }
        }
        return null;
    }
}
