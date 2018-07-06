package me.checkium.openskywars.game;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.config.ChestsConfig;
import me.checkium.openskywars.config.TeamsConfig;
import me.checkium.openskywars.game.regen.GameReset;
import me.checkium.openskywars.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Game {

    public Arena arena;
    public GameState state;
    public HashMap<Player, String> players = new HashMap<>();
    public GameReset reset = new GameReset(this);
    String worldName;
    HashMap<Player, ItemStack[]> invContents = new HashMap<>();
    HashMap<Player, ItemStack[]> invArmorContents = new HashMap<>();
    GameScoreboard board = new GameScoreboard(this);
    GameListener listener = new GameListener(this);
    int secLeft;

    public Game(Arena arena) {
        this.arena = arena;
        this.state = GameState.LOADING;
        loadWorld();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!players.isEmpty()) {
                    board.update();
                }
            }
        }.runTaskTimerAsynchronously(OpenSkyWars.getInstance(), 0L, 20L);
    }

    public void loadWorld() {
        worldName = arena.cuboid.worldName;
        if (Bukkit.getWorld(worldName) != null) {
            this.state = GameState.WAITING;
            return;
        }
        if (!WorldUtils.worldExists(worldName)) {
            throw new RuntimeException("Tried to create game for not existing world.");
        }
        WorldUtils.getWorld(worldName);
        this.state = GameState.WAITING;
    }

    public void join(Player p) {
        if (GameManager.get().getGame(p) != null) {
            p.sendMessage(ChatColor.RED + "You are already in-game");
            return;
        }
        players.put(p, getFreeTeam());
        invContents.put(p, p.getInventory().getContents());
        invArmorContents.put(p, p.getInventory().getArmorContents());
        p.getInventory().clear();
        p.teleport(arena.lobby);
        listener.init(p);
        board.add(p);
        if (players.size() >= (arena.minTeams * arena.teamSize)) {
            if (!started) startLobbyCountdown();
        }
    }
   boolean started = false;
    public void startLobbyCountdown() {
        started = true;
        secLeft = arena.lobbyCountdown;
        state = GameState.STARTING;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (secLeft > 0) {
                    secLeft--;
                } else {
                    startGame();
                    cancel();
                }
            }
        }.runTaskTimer(OpenSkyWars.getInstance(), 0L, 20L);
    }

    public void startGame() {
        players.forEach((player, s) -> player.sendMessage("Starting game"));
        generateCages();
        populateChests();
        final int[] left = {arena.gameLength + 10};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (left[0] > 0) {
                    left[0]--;
                } else {
                    endGame();
                    cancel();
                }
            }
        }.runTaskTimer(OpenSkyWars.getInstance(), 0L, 20L);
    }

    public void populateChests() {
        arena.chests.forEach((location, s) -> {
           WorldUtils.fillChest(location.getBlock().getState(), ChestsConfig.getItems().get("basic"));
        });
    }

    public void generateCages() {
        List<Block> blocks = new ArrayList<>();
        arena.teams.forEach((s, location) -> {
            blocks.addAll(WorldUtils.generateCage(this, location, Arrays.asList(Material.WOOL, Material.GLASS, Material.STAINED_GLASS, Material.BRICK, Material.REDSTONE_BLOCK)));
        });
        players.forEach((player, s) -> {
            player.teleport(arena.teams.get(s).getBlock().getLocation().add(0.5, 0, 0.5));
        });
        final int[] secLeft = {10};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (secLeft[0] > 0) {
                    players.forEach((player, s) -> player.sendMessage(ChatColor.YELLOW + "Dropping from cages in " + ChatColor.RED + secLeft[0] + ChatColor.YELLOW + " seconds."));
                    secLeft[0]--;
                } else {
                    state = GameState.INGAME;
                    blocks.forEach(block -> block.setType(Material.AIR));
                    cancel();
                }
            }
        }.runTaskTimer(OpenSkyWars.getInstance(), 0L, 20L);
    }
    BukkitTask endGameTask;
    public void endGame() {
        final int[] left = {5};
        state = GameState.ENDED;
        endGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (left[0] > 0) {
                    left[0]--;
                } else {
                    resetGame(true);
                    cancel();
                }
            }
        }.runTaskTimer(OpenSkyWars.getInstance(), 0L, 20L);
    }

    public void removePlayer(Player player, boolean map) {
        if (map) players.remove(player);
        player.getInventory().clear();
        player.teleport(arena.lobby);
        player.getInventory().setArmorContents(invArmorContents.get(player));
        player.getInventory().setContents(invContents.get(player));
        player.updateInventory();
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void checkWin() {
        final boolean[] win = {true};
        final String[] team = {null};
        players.forEach((player, s) -> {
            if (team[0] != null && !team[0].equals(s)) {
                win[0] = false;
            }
            team[0] = s;
        });
        if (win[0]) {
            if (endGameTask != null) endGameTask.cancel();
            players.forEach((player, s) -> player.sendMessage(ChatColor.GREEN + "Team " + TeamsConfig.getTeams().get(s) + ChatColor.GREEN + " won the game!"));
            resetGame(true);
        }
    }

    public void resetGame(boolean now) {
        players.keySet().forEach(player -> {
          removePlayer(player,false);
        });
        players.clear();
        state = GameState.LOADING;
        listener.destroy();
        World w;
        (w = Bukkit.getWorld(worldName)).getNearbyEntities(new Location(w, arena.cuboid.x1, arena.cuboid.y1, arena.cuboid.z1), arena.cuboid.x2 - arena.cuboid.x1, arena.cuboid.y2 - arena.cuboid.y1, arena.cuboid.z2 - arena.cuboid.z1).forEach(entity -> {
            if (entity.getType().equals(EntityType.PRIMED_TNT)) {
                entity.remove();
            }
        });
        if (now) {
            reset.reset();
        } else {
            File file = new File(OpenSkyWars.getInstance().getDataFolder() + "/cache");
            if (!file.exists()) file.mkdirs();
            reset.saveBlocksToFile(new File(file, arena.name + "_cache"));
        }
    }

    private String getFreeTeam() {
        final String[] team = {null};
        arena.teams.forEach((s, location) -> {
            if (team[0] != null) return;
            int num = (int) players.values().stream().filter(s1 -> s1.equals(s)).count();
            if (num < arena.teamSize) {
                team[0] = s;
            }
        });
        return team[0];
    }

    public enum GameState {
        LOADING, WAITING, STARTING, INGAME, ENDED
    }
}
