package me.checkium.openskywars.arena.setup;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.config.ChestsConfig;
import me.checkium.openskywars.utils.ColorConverter;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChestSetup implements Listener {
    public static List<ChestSetup> setups = new ArrayList<>();
    public Player player;
    private Arena arena;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private BukkitTask task;

    public ChestSetup(Arena a, Player p) {
        this.player = p;
        this.arena = a;
        setups.add(this);
    }

    private HashMap<String, DyeColor> colorMap = new HashMap<>();

    public void init() {
        contents = player.getInventory().getContents().clone();
        armorContents = player.getInventory().getArmorContents().clone();
        player.getInventory().clear();
        List<DyeColor> colors = new ArrayList<>(Arrays.asList(DyeColor.values()));
        ChestsConfig.getItems().keySet().forEach(s -> {
            DyeColor color = colors.get((int) (Math.random() * colors.size()));
            ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) color.getDyeData());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ColorConverter.dyeToChat(color) + "Chest: " + s);
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
            colorMap.put(s, color);
            colors.remove(color);
        });
        player.getInventory().setItem(8, ItemUtils.named(Material.REDSTONE, 1, ChatColor.GREEN + "Exit"));
        Bukkit.getServer().getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
        task = Bukkit.getScheduler().runTaskTimer(OpenSkyWars.getInstance(), () -> {
            arena.chests.forEach((location, s) -> {
                Color c = ColorConverter.hexToColor(ColorConverter.dyeToHex(colorMap.get(s)));
                int red = c.getRed();
                int green = c.getGreen();
                int blue = c.getBlue();
                for (int i = 0; i < 5; i++) {
                    Utils.spawnParticle(player, location.getBlock().getLocation().clone().subtract(0.5, 0.5, 0.5).add(Math.random(), Math.random(), Math.random()), red, green, blue);
                }
            });
        }, 0L, 5L);
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getPlayer().getItemInHand().getType().equals(Material.INK_SACK) && e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Chest: ")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                  if (e.getClickedBlock().getType().equals(Material.CHEST)) {
                      String chestType = e.getPlayer().getItemInHand().getItemMeta().getDisplayName().split("Chest: ")[1];
                      String s = arena.chests.get(e.getClickedBlock().getLocation());
                      if (s == null) {
                          arena.chests.put(e.getClickedBlock().getLocation(), chestType);
                          e.getPlayer().sendMessage(ChatColor.GREEN + "Set chest type to " + ColorConverter.dyeToChat(colorMap.get(chestType)) + chestType);
                      } else {
                          if (s.equals(chestType)) {
                              e.getPlayer().sendMessage(ChatColor.RED + "That chest's type is already " + ColorConverter.dyeToChat(colorMap.get(chestType)) + chestType);
                          } else {
                              arena.chests.remove(e.getClickedBlock().getLocation());
                              arena.chests.put(e.getClickedBlock().getLocation(), chestType);
                              e.getPlayer().sendMessage(ChatColor.GREEN + "Set chest type to " + ColorConverter.dyeToChat(colorMap.get(chestType)) + chestType);
                          }
                      }
                  }
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
    }

}
