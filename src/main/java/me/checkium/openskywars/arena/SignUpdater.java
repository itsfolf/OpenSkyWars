package me.checkium.openskywars.arena;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.game.Game;
import me.checkium.openskywars.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

public class SignUpdater {

    public SignUpdater() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(OpenSkyWars.getInstance(), () -> {
            for (Game game : GameManager.get().getGames()) {
                for (Location sign : game.arena.signs) {
                    if (sign.getBlock().getType().equals(Material.SIGN_POST) || sign.getBlock().getType().equals(Material.WALL_SIGN)) {
                        Sign block = (Sign) sign.getBlock().getState();
                        block.setLine(0, ChatColor.GREEN + "OpenSkyWars");
                        block.setLine(1, ChatColor.GREEN + game.arena.prettyName);
                        block.setLine(2,ChatColor.GREEN +game.state.toString());
                        block.setLine(3, ChatColor.GREEN + "" + game.players.size() + "/" + (game.arena.maxTeams * game.arena.teamSize));
                        block.update();
                    }
                }
            }
        }, 5, 100L);
    }
}
