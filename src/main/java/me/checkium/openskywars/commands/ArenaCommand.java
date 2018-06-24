package me.checkium.openskywars.commands;

import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import net.minecraft.server.v1_8_R3.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class ArenaCommand {

    public void process(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String arenaName = args[1];
            Arena arena = ArenaManager.get().forName(arenaName);
            if (arena == null && (args.length < 3 || !args[2].equals("create"))) {
                sender.sendMessage(ChatColor.RED + "There's no arena with the name " + ChatColor.BLUE + arenaName + ChatColor.RED + ", create one with " + ChatColor.BLUE + "/osw arena " + arenaName + " create");
                return;
            } else if (args.length > 2 && args[2].equals("create")) {
                if (arena == null) {
                    Arena newA = new Arena(arenaName);
                    ArenaManager.get().loadedArenas.add(newA);
                    sender.sendMessage(ChatColor.GREEN + "Created arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "! Edit it with " + ChatColor.BLUE + "/osw arena " + arenaName + " edit");
                } else {
                    sender.sendMessage(ChatColor.RED + "There's already an arena called " + ChatColor.BLUE + arenaName + ChatColor.RED + ", edit it with " + ChatColor.BLUE + "/osw arena " + arenaName + " edit");
                }
                return;
            }
            if (args.length > 3 && arena != null) {
                switch (args[2]) {
                    case "enabled":
                        if (arena.enabled && args[3].equals("false")) {
                            arena.enabled = false;
                            sender.sendMessage(ChatColor.GREEN + "The arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " is now " + ChatColor.RED + "disabled" + ChatColor.GREEN + "!");
                            return;
                        } else if (!arena.enabled && args[3].equals("true")) {
                            arena.enabled = true;
                            sender.sendMessage(ChatColor.GREEN + "The arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " is now enabled" + ChatColor.GREEN + "!");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "The arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " is already " + (arena.enabled ? "enabled" : ChatColor.RED + "disabled") + "!");
                        }
                        break;
                    case "teamsize":
                        if (!isInt(sender, args[3])) return;
                        arena.teamSize = Integer.valueOf(args[3]);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s team size to " + ChatColor.BLUE + arena.teamSize + ChatColor.GREEN + "!");
                        break;
                    case "minteams":
                        if (!isInt(sender, args[3])) return;
                        arena.minTeams = Integer.valueOf(args[3]);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s min teams to " + ChatColor.BLUE + arena.minTeams + ChatColor.GREEN + "!");
                        break;
                    case "maxteams":
                        if (!isInt(sender, args[3])) return;
                        arena.maxTeams = Integer.valueOf(args[3]);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s min teams to " + ChatColor.BLUE + arena.maxTeams + ChatColor.GREEN + "!");
                        break;
                    case "lobbycountdown":
                        if (!isInt(sender, args[3])) return;
                        arena.lobbyCountdown = Integer.valueOf(args[3]);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s lobby countdown to " + ChatColor.BLUE + arena.lobbyCountdown + ChatColor.GREEN + " seconds!");
                        break;
                    case "gamelength":
                        if (!isInt(sender, args[3])) return;
                        arena.gameLength = Integer.valueOf(args[3]);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s game length to " + ChatColor.BLUE + arena.gameLength + ChatColor.GREEN + " minutes!");
                        break;
                    case "refilltimes":
                        if (args.length > 4) {
                            if (!isInt(sender, args[4])) return;
                            int number = Integer.valueOf(args[4]);
                            switch (args[3]) {
                                case "add":
                                    if (arena.refillTimes.contains(number)) {
                                        sender.sendMessage(ChatColor.RED + "The arena " + ChatColor.BLUE + arenaName + ChatColor.RED + " already has the refill time " + ChatColor.BLUE  + number);
                                    } else {
                                        arena.refillTimes.add(number);
                                        sender.sendMessage(ChatColor.GREEN + "Added the refill time " + ChatColor.BLUE + number + ChatColor.GREEN + " to the arena " + ChatColor.BLUE + arenaName);
                                    }
                                    break;
                                case "remove":
                                    if (!arena.refillTimes.contains(number)) {
                                        sender.sendMessage(ChatColor.RED + "The arena " + ChatColor.BLUE + arenaName + ChatColor.RED + " has no refill time " + ChatColor.BLUE  + number);
                                    } else {
                                        arena.refillTimes.remove(Integer.valueOf(number));
                                        sender.sendMessage(ChatColor.GREEN + "Remove the refill time " + ChatColor.BLUE + number + ChatColor.GREEN + " from the arena " + ChatColor.BLUE + arenaName);
                                    }
                                    break;
                                default:
                                    sender.sendMessage(ChatColor.RED + "Invalid usage, please use " + ChatColor.BLUE + "/osw arena " + arenaName + " refilltimes <add/remove> <number>");
                                    break;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid usage, please use " + ChatColor.BLUE + "/osw arena " + arenaName + " refilltimes <add/remove> <number>");
                        }
                        break;
                    case "spawnsetup":
                        break;
                    case "chestsetup":
                        break;
                    case "bordersetup":
                        break;
                }
            } else {
                if (arena != null) {
                    if (sender instanceof Player) {
                        ((Player) sender).spigot().sendMessage(arena.getComponent());
                    } else {
                        sender.sendMessage(arena.getComponent().toLegacyText());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "There's no arena with the name " + ChatColor.BLUE + arenaName + ChatColor.RED + ", create one with " + ChatColor.BLUE + "/osw arena " + arenaName + " create");
                }
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Arena list (enabled/" + ChatColor.RED + "disabled" + ChatColor.GREEN + "): " +
            ArenaManager.get().loadedArenas.stream().map(arena -> arena.enabled ? ChatColor.GREEN + arena.name : ChatColor.RED + arena.name).collect(Collectors.joining(", ")));
        }
    }

    public boolean isInt(CommandSender s, String arg) {
        try {
            Integer.valueOf(arg);
            return true;
        } catch (NumberFormatException ignored) {
            s.sendMessage(ChatColor.RED + arg + " is not a valid number!");
        }
        return false;
    }
}
