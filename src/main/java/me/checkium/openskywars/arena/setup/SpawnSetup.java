package me.checkium.openskywars.arena.setup;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.config.TeamsConfig;
import me.checkium.openskywars.utils.ItemUtils;
import me.checkium.openskywars.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class SpawnSetup implements Listener {
    public static List<SpawnSetup> setups = new ArrayList<>();

    private Arena arena;
    public Player player;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    public SpawnSetup(Arena a, Player p) {
        this.player = p;
        this.arena = a;
        setups.add(this);
    }
    private BukkitTask task;
    public void init() {
        contents = player.getInventory().getContents().clone();
        armorContents = player.getInventory().getArmorContents().clone();
        player.getInventory().clear();
        player.getInventory().setItem(4, ItemUtils.named(Material.BEACON, 1, ChatColor.GREEN + "Spawn Setter"));
        player.getInventory().setItem(8, ItemUtils.named(Material.REDSTONE, 1, ChatColor.GREEN + "Exit"));
        Bukkit.getServer().getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
         task = Bukkit.getScheduler().runTaskTimer(OpenSkyWars.getInstance(), () -> {
            int amount = 20;

            double increment = (2 * Math.PI) / amount;
            double radius = 0.5;
            arena.teams.forEach((s, location) -> {
                Location center = location.getBlock().getLocation().clone().add(0.5,0,0.5);
                for(int i = 0; i < 20; i++) {
                    double angle = i * increment;
                    double x = center.getX() + (radius * Math.cos(angle));
                    double z = center.getZ() + (radius * Math.sin(angle));
                    Color c = Utils.translateChatColorToColor(ChatColor.valueOf(s));
                    int red = c.getRed();
                    int green = c.getGreen();
                    int blue = c.getBlue();
                    Utils.spawnParticle(player, new Location(location.getWorld(), x, location.getY(), z), red, green, blue);
                }
            });
        }, 0L, 5L);
    }

    private void exit() {
        HandlerList.unregisterAll(this);
        task.cancel();
        player.getInventory().clear();
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents);
        setups.remove(this);
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getBlockPlaced().getType().equals(Material.BEACON)) {
                if (e.getItemInHand().getItemMeta().getDisplayName().contains("Spawn Setter")) {
                  if (!setSpawn(e.getBlockPlaced().getLocation())) e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void breakk(BlockBreakEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getBlock().getType().equals(Material.BEACON)) {
               removeSpawn(e.getBlock().getLocation());
            }
        }
    }

    private boolean setSpawn(Location l, String team) {
        if (!arena.cuboid.contains(l)) {
            player.sendMessage(ChatColor.RED + "Spawns need to be inside the arena area.");
            return false;
        }
        if (!arena.teams.containsKey(team)) {
            arena.teams.put(team, l.clone().add(0,1,0));
            player.sendMessage(ChatColor.GREEN + "Set spawn for the team " + TeamsConfig.getTeams().get(team));
        } else {
            player.sendMessage(ChatColor.RED + "There's already a spawn for the team " + TeamsConfig.getTeams().get(team));
        }
        return true;
    }

    private boolean setSpawn(Location l) {
        String a =  getFreeTeam();
        if (a!= null) {
            return setSpawn(l, a);
        } else {
            player.sendMessage(ChatColor.RED + "There are no more teams available, you can add more on teams.yml");
            return false;
        }
    }

    private void removeSpawn(Location l) {
        List<String> torem = new ArrayList<>();
        arena.teams.forEach((s, location) -> {
            if (l.clone().add(0,1,0).equals(location)) {
                torem.add(s);
            }
        });
        torem.forEach(s -> {
            arena.teams.remove(s);
            player.sendMessage(ChatColor.GREEN + "Removed spawn for the team " + TeamsConfig.getTeams().get(s));
        });
    }

    private String getFreeTeam() {
        for (String s : TeamsConfig.getTeams().keySet()) {
            if (!arena.teams.containsKey(s)) {
                return s;
            }
        }
        return null;
    }

    private Location current;
    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (e.getPlayer().equals(player)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (e.getClickedBlock().getType().equals(Material.BEACON)) {
                    Inventory inv = Bukkit.createInventory(null, (int) Math.ceil(TeamsConfig.getTeams().size() / 8.0) * 9, ChatColor.GREEN + "Select Team");
                    TeamsConfig.getTeams().forEach((s, s2) -> inv.addItem(ItemUtils.named(Material.WOOL, 1, s2)));
                    player.openInventory(inv);
                    current = e.getClickedBlock().getLocation();
                    e.setCancelled(true);
                }
            } else if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (e.getPlayer().getItemInHand().getType().equals(Material.REDSTONE)) {
                    if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Exit")) {
                         exit();
                    }
                }
            }
        }
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory().getName().contains("Select Team")) {
            if (e.getWhoClicked().equals(player)) {
                if (current != null) {
                    if (e.getCurrentItem() != null) {
                        ItemStack stack = e.getCurrentItem();
                        TeamsConfig.getTeams().forEach((s, s2) -> {
                            if (s2.equals(stack.getItemMeta().getDisplayName())) {
                                removeSpawn(current);
                                setSpawn(current, s);
                                player.closeInventory();
                                current = null;
                            }
                        });
                    }
                }
            }
        }
    }


}
