package me.checkium.openskywars.lobby;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.config.TeamsConfig;
import me.checkium.openskywars.game.GameManager;
import me.checkium.openskywars.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyListener implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (OpenSkyWars.getInstance().getMainConfig().lobbyEnabled && OpenSkyWars.getInstance().getMainConfig().autoLobby) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    OpenSkyWars.getInstance().getLobby().join(e.getPlayer());
                }
            }.runTaskLater(OpenSkyWars.getInstance(), 20L);
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent e) {
        if (OpenSkyWars.getInstance().getMainConfig().lobbyEnabled && OpenSkyWars.getInstance().getLobby().isInLobby(e.getPlayer())) {
            OpenSkyWars.getInstance().getLobby().leave(e.getPlayer());
        }
    }

    @EventHandler
    public void breakk(BlockBreakEvent e) {
        if (OpenSkyWars.getInstance().getLobby().isInLobby(e.getPlayer()) && OpenSkyWars.getInstance().getMainConfig().lobbyEnabled) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (OpenSkyWars.getInstance().getLobby().isInLobby(e.getPlayer()) && GameManager.get().getGame(e.getPlayer()) == null) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (e.getPlayer().getItemInHand().getType().equals(Material.REDSTONE)) {
                    if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Exit")) {
                        OpenSkyWars.getInstance().getLobby().leave(e.getPlayer());
                    }
                }
            }
        }
    }
}
