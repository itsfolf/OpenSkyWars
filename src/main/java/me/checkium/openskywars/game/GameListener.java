package me.checkium.openskywars.game;

import me.checkium.openskywars.OpenSkyWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.HashMap;

public class GameListener implements Listener {
    public HashMap<Player, Integer> kills = new HashMap<>();
    public HashMap<Player, Integer> assists = new HashMap<>();
    HashMap<Player, HashMap<Player, Long>> assisters = new HashMap<>();
    private Game game;

    public GameListener(Game game) {
        this.game = game;
        Bukkit.getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
    }

    void init(Player p) {
        kills.put(p, 0);
        assists.put(p, 0);
        assisters.put(p, new HashMap<>());
    }

    void destroy() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void kill(PlayerDeathEvent e) {
        if (game.players.containsKey(e.getEntity())) {
            Player p = e.getEntity();
            Player killer = p.getKiller();
            if (killer != null) kills.put(killer, kills.get(killer) + 1);
            assisters.get(p).forEach((player, aLong) -> {
                if (player != killer && aLong > System.currentTimeMillis() - 15000) {
                    assists.put(player, assists.get(player) + 1);
                }
            });
            String message = e.getDeathMessage();
            e.setDeathMessage(null);
            game.players.forEach((player, s) -> player.sendMessage(message));
            p.spigot().respawn();
            game.removePlayer(p, true);
            game.checkWin();
        }
    }


    @EventHandler
    public void playerDamageBy(EntityDamageByEntityEvent e) {
        if (game.players.containsKey(e.getEntity()) && game.players.containsKey(e.getDamager())) {
            assisters.get(e.getEntity()).put((Player) e.getDamager(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            if (game.arena.cuboid.contains(b.getLocation())) {
                if (!game.state.equals(Game.GameState.ENDED)) {
                    game.reset.addChanged(b);
                } else {
                    event.blockList().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.INGAME)) {
                game.reset.addChanged(event.getToBlock());
            } else if (game.state.equals(Game.GameState.LOADING) || game.state.equals(Game.GameState.ENDED)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.INGAME)) {
                game.reset.addChanged(event.getBlock());
            } else if (game.state.equals(Game.GameState.ENDED)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.ENDED)) {
                event.setCancelled(true);
                return;
            }
            game.reset.addChanged(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.INGAME)) {
                if (event.getChangedType() == Material.CARPET || event.getChangedType() == Material.BED_BLOCK) {
                    return;
                }
                game.reset.addChanged(event.getBlock());
            } else if (game.state.equals(Game.GameState.LOADING) || game.state.equals(Game.GameState.ENDED)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.INGAME)) {
                event.getBlock().setData((byte) 0);
                game.reset.addChanged(event.getBlock());
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            if (game.state.equals(Game.GameState.INGAME)) {
                game.reset.addChanged(event.getBlock().getLocation(), Material.AIR, (byte) 0);
            } else if (game.state.equals(Game.GameState.LOADING) || game.state.equals(Game.GameState.ENDED)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            if (game.arena.cuboid.contains(event.getEntity().getLocation())) {
                game.reset.addChanged(event.getBlock());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (game.players.containsKey(p)) {
            if (!game.state.equals(Game.GameState.INGAME)) {
                event.setCancelled(true);
                return;
            }
            game.reset.addChanged(event.getBlock());
            if (event.getBlock().getType() == Material.DOUBLE_PLANT) {
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, -1D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +1D, 0D).getBlock());
            }
            if (event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK) {
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +3D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +2D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +1D, 0D).getBlock());
            }
            if (event.getBlock().getType() == Material.CARPET) {
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +3D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +2D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +1D, 0D).getBlock());
            }
            if (event.getBlock().getType() == Material.CACTUS) {
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +4D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +3D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +2D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, +1D, 0D).getBlock());
            }
            if (event.getBlock().getType() == Material.BED_BLOCK) {
                game.reset.addChanged(event.getBlock().getLocation().clone().add(1D, 0D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(-1D, 0D, 0D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, 0D, 1D).getBlock());
                game.reset.addChanged(event.getBlock().getLocation().clone().add(0D, 0D, -1D).getBlock());
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (game.arena.cuboid.contains(event.getBlock().getLocation())) {
            game.reset.addChanged(event.getBlock());
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (game.players.containsKey(event.getPlayer())) {
            Block start = event.getBlockClicked();
            if (!game.arena.cuboid.contains(start.getLocation())) {
                event.setCancelled(true);
                return;
            }
            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        Block b = start.getLocation().clone().add(x, y, z).getBlock();
                        game.reset.addChanged(b);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (game.state.equals(Game.GameState.INGAME)) {
            Location start = event.getLocation();
            if (game.arena.cuboid.contains(start)) {
                game.reset.addChanged(start.getBlock(), false);
                for (BlockState bs : event.getBlocks()) {
                    Block b = bs.getBlock();
                    game.reset.addChanged(b.getLocation(), Material.AIR, (byte) 0);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (game.players.containsKey(p)) {
            if (!game.state.equals(Game.GameState.INGAME)) {
                event.setCancelled(true);
                return;
            }
            if (event.getBlockReplacedState().getType() != Material.AIR) {
                game.reset.addChanged(event.getBlock().getLocation(), event.getBlockReplacedState().getType(), event.getBlockReplacedState().getData().getData());
            } else {
                game.reset.addChanged(event.getBlock().getLocation(), Material.AIR, (byte) 0);
            }
        }
    }

    @EventHandler
    public void onSignUse(PlayerInteractEvent event) {
        if (game.players.containsKey(event.getPlayer())) {
            if (event.hasBlock()) {
                if (event.getClickedBlock().getType() == Material.CHEST) {
                    if (game.state.equals(Game.GameState.INGAME)) game.reset.addChanged(event.getClickedBlock());
                } else if (event.getClickedBlock().getType() == Material.TNT) {
                    if (game.state.equals(Game.GameState.INGAME)) game.reset.addChanged(event.getClickedBlock());
                } else if (event.getPlayer().getItemInHand().getType() == Material.WATER_BUCKET || event.getPlayer().getItemInHand().getType() == Material.WATER || event.getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET || event.getPlayer().getItemInHand().getType() == Material.LAVA) {
                    if (!game.arena.cuboid.contains(event.getClickedBlock().getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                    if (game.state.equals(Game.GameState.INGAME)) game.reset.addChanged(event.getClickedBlock());
                } else if (event.getClickedBlock().getType() == Material.DISPENSER || event.getClickedBlock().getType() == Material.DROPPER) {
                    if (game.state.equals(Game.GameState.INGAME)) game.reset.addChanged(event.getClickedBlock());
                }
            }
        }
    }
}



