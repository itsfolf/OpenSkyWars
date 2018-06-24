package me.checkium.openskywars.arena.setup;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.utils.Cuboid;
import me.checkium.openskywars.utils.ItemUtils;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class BorderSetup implements Listener {

    public static List<BorderSetup> setups = new ArrayList<>();

    Arena arena;
    public Player player;
    ItemStack[] contents;
    ItemStack[] armorContents;
    ItemStack[] extraContents;
    public BorderSetup(Arena a, Player p) {
        this.player = p;
        this.arena = a;
        setups.add(this);
    }
BukkitTask task;
    List<Location> l = new ArrayList<>();
    public void init() {
        contents = player.getInventory().getContents().clone();
        armorContents = player.getInventory().getArmorContents().clone();
        //  extraContents = player.getInventory().getExtraContents().clone();
        player.getInventory().clear();
        player.getInventory().setItem(0, ItemUtils.named(Material.BLAZE_ROD, 1, ChatColor.GREEN + "Selection wand"));
        player.getInventory().setItem(8, ItemUtils.named(Material.REDSTONE, 1, ChatColor.GREEN + "Exit"));
        Bukkit.getServer().getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
        task = Bukkit.getScheduler().runTaskTimer(OpenSkyWars.getInstance(), () -> {
          l.forEach(location -> {
              player.spigot().playEffect(location, Effect.COLOURED_DUST, 0, 1, (float) Math.random(),(float) Math.random(),(float) Math.random(), 1, 0, 64);
          });
        }, 0L, 5L);
    }

    Location l1;
    Location l2;
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getPlayer().getItemInHand().getType().equals(Material.BLAZE_ROD) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Selection wand")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    l1 = e.getClickedBlock().getLocation();
                    player.sendMessage(ChatColor.GREEN + "Set first corner to " + ChatColor.BLUE + Utils.locationToString(l1));
                    player.sendBlockChange(l1, Material.GOLD_BLOCK, (byte) 0);
                    sendBorderPacket();
                } else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    l2 = e.getClickedBlock().getLocation();
                    player.sendMessage(ChatColor.GREEN + "Set second corner to " + ChatColor.BLUE + Utils.locationToString(l2));
                    player.sendBlockChange(l2, Material.GOLD_BLOCK, (byte) 0);
                    sendBorderPacket();
                }
                e.setCancelled(true);
            } else if (e.getPlayer().getItemInHand().getType().equals(Material.REDSTONE) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Exit")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                    exit();
                }
            }
        }
    }

    public void exit() {
        HandlerList.unregisterAll(this);
        task.cancel();
        player.getInventory().clear();
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents);
        setups.remove(this);
        player.sendMessage(ChatColor.GREEN + "Processing new arena region, this can take a while...");
        Bukkit.getServer().getScheduler().runTaskAsynchronously(OpenSkyWars.getInstance(), () -> {
              arena.chests.clear();
              List<Location> blocks = arena.cuboid.getAll();
              int size = blocks.size();
              int done = 0;
              long last = 0;
              Iterator<Location> it = blocks.iterator();
              while (it.hasNext()) {
                  Location l = it.next();
                  Material b = l.getBlock().getType();
                  if (b.equals(Material.CHEST)) {
                     arena.chests.put(l, "normal");
                  }
                  done++;
                  if (last < System.currentTimeMillis() - 3000) {
                      player.sendMessage(ChatColor.GREEN + "" + ((done / size) * 100) + "% done..");
                      last = System.currentTimeMillis();
                  }
              }
              player.sendMessage(ChatColor.GREEN + "Finished processing arena region, found " + ChatColor.BLUE + arena.chests.size() + ChatColor.GREEN + " chests.");
        });
    }


    public void sendBorderPacket() {
        if (l1 != null && l2 != null) {
            arena.cuboid = new Cuboid(l1, l2);
            l =  arena.cuboid.getBorders();
        }
    }

}
