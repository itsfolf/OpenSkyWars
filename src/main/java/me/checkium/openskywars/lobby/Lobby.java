package me.checkium.openskywars.lobby;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.utils.ItemUtils;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Lobby {

    private Map<Player, LobbyPlayer> lobbyPlayers = new HashMap<>();
    private Location location;

    public Lobby() {
      Bukkit.getServer().getPluginManager().registerEvents(new LobbyListener(), OpenSkyWars.getInstance());
    }

    public void join(Player p) {
        if (lobbyPlayers.containsKey(p)) {
            p.sendMessage(ChatColor.RED + "You are already in the lobby");
        } else {
           LobbyPlayer lp = new LobbyPlayer();
           lp.setInvContents(p.getInventory().getContents());
           lp.setInvArmorContents(p.getInventory().getArmorContents());
           lp.setLocation(p.getLocation());
           lobbyPlayers.put(p, lp);
           p.getInventory().clear();
           if (!OpenSkyWars.getInstance().getMainConfig().autoLobby) {
               p.getInventory().setItem(8, ItemUtils.named(Material.REDSTONE, 1, ChatColor.GREEN + "Exit"));
           }
           p.teleport(OpenSkyWars.getInstance().getMainConfig().lobbyLocation == null ? Bukkit.getWorlds().get(0).getSpawnLocation() : Utils.fromString(OpenSkyWars.getInstance().getMainConfig().lobbyLocation));
           p.sendMessage(ChatColor.GREEN + "Successfully joined the lobby");
        }
    }

    public void leave(Player p) {
       if (lobbyPlayers.containsKey(p)) {
           LobbyPlayer lp = lobbyPlayers.get(p);
           lobbyPlayers.remove(p);
           p.getInventory().setContents(lp.getInvContents());
           p.getInventory().setArmorContents(lp.getInvArmorContents());
           p.teleport(lp.getLocation());
           p.updateInventory();
           p.sendMessage(ChatColor.GREEN + "Successfully left the lobby");
       } else {
           p.sendMessage(ChatColor.RED + "You are not in the lobby");
       }
    }

    public boolean isInLobby(Player p) {
       return !OpenSkyWars.getInstance().getMainConfig().lobbyEnabled || lobbyPlayers.containsKey(p);
    }

    class LobbyPlayer {
        ItemStack[] invContents;
        ItemStack[] invArmorContents;
        Location location;

        ItemStack[] getInvArmorContents() {
            return invArmorContents;
        }

        void setInvArmorContents(ItemStack[] invArmorContents) {
            this.invArmorContents = invArmorContents;
        }

        ItemStack[] getInvContents() {
            return invContents;
        }

        void setInvContents(ItemStack[] invContents) {
            this.invContents = invContents;
        }

        Location getLocation() {
            return location;
        }

        void setLocation(Location location) {
            this.location = location;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void close() {
        lobbyPlayers.forEach((player, lobbyPlayer) -> leave(player));
    }
}
