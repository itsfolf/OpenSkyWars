package me.checkium.openskywars.game;

import me.checkium.openskywars.config.TeamsConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class GameScoreboard {
    private Game game;
    public HashMap<Player, Scoreboard> scoreboards = new HashMap<>();

    public GameScoreboard(Game g) {
        this.game = g;
    }

    public void add(Player p) {
        if (!scoreboards.containsKey(p)) scoreboards.put(p, Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void update() {
        scoreboards.forEach((player, scoreboard) -> {
            scoreboard.getTeams().forEach(Team::unregister);
            scoreboard.getObjectives().forEach(Objective::unregister);
            Objective arena = scoreboard.registerNewObjective("OpenSkyWars", "dummy");
            game.players.forEach((player1, s) -> {
                Team t = scoreboard.registerNewTeam(TeamsConfig.getTeams().get(s));
                t.setPrefix(ChatColor.valueOf(s) + "");
                t.addEntry(player1.getDisplayName());
                if (player1 == player) {
                    arena.getScore(ChatColor.GREEN + "Team: " + ChatColor.BLUE + TeamsConfig.getTeams().get(s)).setScore(0);
                }
            });
            arena.setDisplaySlot(DisplaySlot.SIDEBAR);
            if (game.state == Game.GameState.INGAME) {
                arena.getScore(ChatColor.GREEN + "Arena: " + ChatColor.BLUE + game.arena.prettyName).setScore(3);
                arena.getScore(ChatColor.GREEN + "Kills: " + ChatColor.BLUE + game.listener.kills.get(player)).setScore(2);
                arena.getScore(ChatColor.GREEN + "Assists: " + ChatColor.BLUE + game.listener.assists.get(player)).setScore(1);
            } else if (game.state.equals(Game.GameState.STARTING)) {
                arena.getScore(ChatColor.GREEN + "Starting in: ").setScore(2);
                arena.getScore(ChatColor.GOLD + "" + game.secLeft + " seconds...").setScore(1);
            }
            player.setScoreboard(scoreboard);
        });
    }
}
