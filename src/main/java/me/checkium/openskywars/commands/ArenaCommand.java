package me.checkium.openskywars.commands;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.arena.setup.BorderSetup;
import me.checkium.openskywars.arena.setup.SpawnSetup;
import me.checkium.openskywars.game.regen.GameReset;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class ArenaCommand {

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
                    sender.sendMessage(ChatColor.GREEN + "Created arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "! Edit it with " + ChatColor.BLUE + "/osw arena " + arenaName);
                } else {
                    sender.sendMessage(ChatColor.RED + "There's already an arena called " + ChatColor.BLUE + arenaName + ChatColor.RED + ", edit it with " + ChatColor.BLUE + "/osw arena " + arenaName);
                }
                return;
            }
            if (args.length > 3 && arena != null) {
                switch (args[2].toLowerCase()) {
                    case "enabled":
                    case "enable":
                        if (arena.enabled && args[3].equals("false")) {
                            arena.enabled = false;
                            sender.sendMessage(ChatColor.GREEN + "The arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " is now " + ChatColor.RED + "disabled" + ChatColor.GREEN + "!");
                            return;
                        } else if (!arena.enabled && args[3].equals("true")) {
                            sender.sendMessage(arena.enable());
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
                                        sender.sendMessage(ChatColor.RED + "The arena " + ChatColor.BLUE + arenaName + ChatColor.RED + " already has the refill time " + ChatColor.BLUE + number);
                                    } else {
                                        arena.refillTimes.add(number);
                                        sender.sendMessage(ChatColor.GREEN + "Added the refill time " + ChatColor.BLUE + number + ChatColor.GREEN + " to the arena " + ChatColor.BLUE + arenaName);
                                    }
                                    break;
                                case "remove":
                                    if (!arena.refillTimes.contains(number)) {
                                        sender.sendMessage(ChatColor.RED + "The arena " + ChatColor.BLUE + arenaName + ChatColor.RED + " has no refill time " + ChatColor.BLUE + number);
                                    } else {
                                        arena.refillTimes.remove(Integer.valueOf(number));
                                        sender.sendMessage(ChatColor.GREEN + "Remove the refill time " + ChatColor.BLUE + number + ChatColor.GREEN + " from the arena " + ChatColor.BLUE + arenaName);
                                    }
                                    break;
                                case "clear":
                                    arena.refillTimes.clear();
                                    sender.sendMessage(ChatColor.GREEN + "Cleared all the refill times from the arena " + ChatColor.BLUE + arenaName);
                                    break;
                                default:
                                    sender.sendMessage(ChatColor.RED + "Invalid usage, please use " + ChatColor.BLUE + "/osw arena " + arenaName + " refilltimes <add/remove> <number>");
                                    break;
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Invalid usage, please use " + ChatColor.BLUE + "/osw arena " + arenaName + " refilltimes <add/remove> <number>");
                        }
                        break;
                    case "name":
                        String[] namea = Arrays.copyOfRange(args, 3, args.length - 1);
                        String name = Arrays.stream(namea).collect(Collectors.joining(" "));
                        arena.prettyName = name;
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.BLUE + arenaName + ChatColor.GREEN + "'s name to " + name);
                        break;
                    case "reset":
                        if (args[3].equals("confirm")) {
                            sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Resetting arena...");
                            File arenaFolder = new File(OpenSkyWars.getInstance().getDataFolder() + "/arenas");
                            if (!arenaFolder.exists()) arenaFolder.mkdirs();
                            File blocksFile = new File(arenaFolder, arena.name + "_blocks");
                            if (blocksFile.exists()) {
                                GameReset reset = new GameReset(arena);
                                reset.loadBlocksFromFile(blocksFile, false);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Couldn't find blocks file for arena" + arena.name);
                            }
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Canceled arena reset.");
                        }
                        break;
                }
            } else if (args.length > 2 && arena != null) {
                switch (args[2].toLowerCase()) {
                    case "spawnsetup":
                        if (SpawnSetup.setups.stream().anyMatch(spawnSetup -> spawnSetup.player.equals(sender))) {
                            sender.sendMessage(ChatColor.RED + "You are already on setup mode.");
                        } else {
                            SpawnSetup s = new SpawnSetup(arena, (Player) sender);
                            s.init();
                        }
                        break;
                    case "chestsetup":
                        break;
                    case "bordersetup":
                        if (BorderSetup.setups.stream().filter(spawnSetup -> spawnSetup.player.equals(sender)).count() > 0) {
                            sender.sendMessage(ChatColor.RED + "You are already on setup mode.");
                        } else {
                            BorderSetup s = new BorderSetup(arena, (Player) sender);
                            s.init();
                        }
                        break;
                    case "save":
                        long a = System.currentTimeMillis();
                        ArenaManager.get().saveArena(arena);
                        long num = System.currentTimeMillis() - a;
                        sender.sendMessage(ChatColor.GREEN + "Saved arena " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " in " + ChatColor.BLUE + num + ChatColor.GREEN + "ms.");
                        break;
                    case "lobby":
                        arena.lobby = ((Player) sender).getLocation();
                        sender.sendMessage(ChatColor.GREEN + "Set the lobby for " + ChatColor.BLUE + arenaName + ChatColor.GREEN + " to " + ChatColor.BLUE + Utils.locationToString(arena.lobby));
                        break;
                    case "reset":
                        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "WARNING!" + ChatColor.RESET + "" + ChatColor.RED + " This command will PERMANENTLY rollback the arena to the last time it was saved, " +
                                "this is intended for server crashes and cannot be undone, use" + ChatColor.BLUE + " /osw arena " + arenaName + " reset confirm" + ChatColor.RED + " to continue.");
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

    boolean isInSetup(Player p) {
        return (SpawnSetup.setups.stream().filter(spawnSetup -> spawnSetup.player.equals(p)).count() > 0);
    }

    private boolean isInt(CommandSender s, String arg) {
        try {
            Integer.valueOf(arg);
            return true;
        } catch (NumberFormatException ignored) {
            s.sendMessage(ChatColor.RED + arg + " is not a valid number!");
        }
        return false;
    }

    public List<String> tabComplete(String[] args) {
        if (args.length > 2) {
            if (args.length == 3) {
                return Utils.match(args[2], Arrays.asList("lobby", "enabled", "teamsize", "minteams", "maxteams", "lobbycountdown", "gamelength", "refilltimes", "name", "spawnsetup", "chestsetup", "bordersetup", "save"));
            } else {
                switch (args[2]) {
                    case "refilltimes":
                        return Utils.match(args[3], Arrays.asList("add", "remove", "clear"));
                    case "enabled":
                    case "enable":
                        return Utils.match(args[3], Arrays.asList("true", "false"));
                }
            }
        } else {
            return Utils.match(args[1], ArenaManager.get().loadedArenas.stream().map(arena -> arena.name).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }
}
