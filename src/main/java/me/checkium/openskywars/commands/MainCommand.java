package me.checkium.openskywars.commands;

import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                default:
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Utils.match(args[0], Arrays.asList("arena", "world"));
        } else if (args.length > 1) {
            switch (args[0]) {
                case "arena":
                    return new ArenaCommand().tabComplete(args);
                case "world":
                    return new WorldCommand().tabComplete(args);
            }
        }
        return null;
    }
}
