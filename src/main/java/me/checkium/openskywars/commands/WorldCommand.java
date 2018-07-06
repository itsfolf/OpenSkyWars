package me.checkium.openskywars.commands;

import me.checkium.openskywars.utils.Utils;
import me.checkium.openskywars.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WorldCommand {

    public void process(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String worldName = args[1];
            World world = WorldUtils.getWorld(worldName);
            if (world == null && (args.length < 3 || !args[2].equals("create"))) {
                sender.sendMessage(ChatColor.RED + "There's no world with the name " + ChatColor.BLUE + worldName + ChatColor.RED + ", create one with " + ChatColor.BLUE + "/osw world " + worldName + " create");
                return;
            } else if (args.length > 2 && args[2].equals("create")) {
                if (world == null) {
                    world = WorldUtils.createWorld(worldName);
                    sender.sendMessage(ChatColor.GREEN + "Created world " + ChatColor.BLUE + worldName + ChatColor.GREEN + "! Go to it with " + ChatColor.BLUE + "/osw arena " + worldName + " tp");
                } else {
                    sender.sendMessage(ChatColor.RED + "There's already a world called " + ChatColor.BLUE + worldName + ChatColor.RED + ", go to it with " + ChatColor.BLUE + "/osw arena " + worldName + " tp");
                }
                return;
            }
            if (args.length > 2 && worldName != null) {
                switch (args[2].toLowerCase()) {
                    case "tp":
                        world.getChunkAt(0, 0).load();
                        ((Player) sender).teleport(world.getChunkAt(0, 0).getBlock(8, 1, 8).getLocation());
                        break;
                    case "delete":
                        WorldUtils.getWorld(worldName).getPlayers().forEach(p -> p.teleport(Bukkit.getWorld("world").getSpawnLocation()));
                        WorldUtils.deleteWorld(worldName);
                        break;
                }
            }
        }
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 3) {
            return Utils.match(args[2], Arrays.asList("tp", "create", "delete"));
        } else if (args.length == 2) {
            return Utils.match(args[1], Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }
}
