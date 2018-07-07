package me.checkium.openskywars.arena.setup;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.utils.Cuboid;
import me.checkium.openskywars.utils.ItemUtils;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class BorderSetup implements Listener {

    public static List<BorderSetup> setups = new ArrayList<>();
    public Player player;
    private Arena arena;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private BukkitTask task;
    private List<Location> l = new ArrayList<>();
    private Location l1;
    private Location l2;


    public BorderSetup(Arena a, Player p) {
        this.player = p;
        this.arena = a;
        setups.add(this);
    }

    public void init() {
        contents = player.getInventory().getContents().clone();
        armorContents = player.getInventory().getArmorContents().clone();
        player.getInventory().clear();
        player.getInventory().setItem(0, ItemUtils.named(Material.BLAZE_ROD, 1, ChatColor.GREEN + "Selection wand"));
        player.getInventory().setItem(8, ItemUtils.named(Material.REDSTONE, 1, ChatColor.GREEN + "Exit"));
        Bukkit.getServer().getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
        task = Bukkit.getScheduler().runTaskTimer(OpenSkyWars.getInstance(), () -> l.forEach(location -> player.spigot().playEffect(location, Effect.COLOURED_DUST, 0, 1, (float) Math.random(), (float) Math.random(), (float) Math.random(), 1, 0, 64)), 0L, 5L);
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getPlayer().getItemInHand().getType().equals(Material.BLAZE_ROD) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Selection wand")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    l1 = e.getClickedBlock().getLocation();
                    player.sendMessage(ChatColor.GREEN + "Set first corner to " + ChatColor.BLUE + Utils.locationToString(l1));
                    updateBorders();
                } else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    l2 = e.getClickedBlock().getLocation();
                    player.sendMessage(ChatColor.GREEN + "Set second corner to " + ChatColor.BLUE + Utils.locationToString(l2));
                    updateBorders();
                }
                e.setCancelled(true);
            } else if (e.getPlayer().getItemInHand().getType().equals(Material.REDSTONE) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Exit")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                    exit();
                }
            }
        }
    }

    private void exit() {
        HandlerList.unregisterAll(this);
        task.cancel();
        player.getInventory().clear();
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();
        setups.remove(this);
        player.sendMessage(ChatColor.GREEN + "Processing new arena region, this can take a while...");
        Bukkit.getServer().getScheduler().runTaskAsynchronously(OpenSkyWars.getInstance(), () -> {
            arena.chests.clear();
            List<Location> blocks = arena.cuboid.getAll();
            int size = blocks.size();
            int done = 0;
            long last = 0;
            for (Location l : blocks) {
                Material b = l.getBlock().getType();
                if (b.equals(Material.CHEST)) {
                    arena.chests.put(l, "normal");
                }
                done++;
                if (last < System.currentTimeMillis() - 3000) {
                    player.sendMessage(ChatColor.GREEN + "ChestSearcher: " + ((done / size) * 100) + "% done..");
                    last = System.currentTimeMillis();
                }
            }
            player.sendMessage(ChatColor.GREEN + "Finished chest searching, found " + ChatColor.BLUE + arena.chests.size() + ChatColor.GREEN + " chests.");
        });
        long start = System.currentTimeMillis();
        player.sendMessage(ChatColor.GREEN + "Saving arena region to file cache...");
        new BukkitRunnable() {
            @Override
            public void run() {
                ArenaManager.get().saveArenaBlocks(arena);
                player.sendMessage(ChatColor.GREEN + "Saved arena region to file cache in " + ChatColor.BLUE + (System.currentTimeMillis() - start) + ChatColor.GREEN + "ms.");
            }
        }.runTaskAsynchronously(OpenSkyWars.getInstance());
    }


    private void updateBorders() {
        if (l1 != null && l2 != null) {
            arena.cuboid = new Cuboid(l1, l2);
            l = arena.cuboid.getBorders();
        }
    }

}
