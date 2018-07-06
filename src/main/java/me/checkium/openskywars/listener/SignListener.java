package me.checkium.openskywars.listener;

import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.game.Game;
import me.checkium.openskywars.game.GameManager;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    @EventHandler
    public void signPlace(SignChangeEvent e) {
       if (e.getLine(0).equals("[OSW]")) {
           String arenaName = e.getLine(1);
           if (arenaName != null) {
               Arena arena = ArenaManager.get().forName(arenaName);
               if (arena != null) {
                   arena.signs.add(e.getBlock().getLocation());
                   e.getPlayer().sendMessage(ChatColor.GREEN + "Added sign for arena " + ChatColor.BLUE + arenaName);
               } else {
                   e.getPlayer().sendMessage(ChatColor.RED + "Couldn't find any arena named " + ChatColor.BLUE + arenaName);
                   e.getBlock().breakNaturally();
               }
           }
       }
    }

    @EventHandler
    public void breakSign(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(Material.SIGN_POST) || e.getBlock().getType().equals(Material.WALL_SIGN))  {
           Arena a;
           if ((a = Utils.getSignOwner(e.getBlock().getLocation())) != null) {
               a.signs.remove(e.getBlock().getLocation());
               e.getPlayer().sendMessage(ChatColor.GREEN + "Removed sign for arena " + ChatColor.BLUE + a.name);
           }
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getClickedBlock().getType().equals(Material.SIGN) || e.getClickedBlock().getType().equals(Material.SIGN_POST) || e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
                Arena a;
                if ((a = Utils.getSignOwner(e.getClickedBlock().getLocation())) != null) {
                    Game game = GameManager.get().getGames().stream().filter(game1 -> a.equals(game1.arena)).findAny().orElse(null);
                    if (game != null) game.join(e.getPlayer()); else e.getPlayer().sendMessage(ChatColor.RED + "Couldn't find game");
                }
            }
        }
    }

}
