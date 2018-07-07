package me.checkium.openskywars.game.regen;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.game.Game;
import me.checkium.openskywars.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author InstanceLabs, Checkium
 */
public class GameReset implements Runnable {

    HashMap<Location, ResetBlock> changed = new HashMap<>();

    Game game;
    Arena arena;
    long time = 0L;
    Logger arenaLogger = Logger.getLogger("ResetLogger");
    private ArrayList<ResetBlock> failedblocks = new ArrayList<>();

    public GameReset(Game a) {
        this.game = a;
    }

    public GameReset(Arena a) {
        this.arena = a;
    }

    public ResetBlock addChanged(Block b) {
        if (!changed.containsKey(b.getLocation())) {
            ResetBlock sablock = new ResetBlock(b, b.getType().equals(Material.CHEST), b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST));
            changed.put(b.getLocation(), sablock);
            return sablock;
        }
        return null;
    }

    public ResetBlock addChanged(Block b, boolean c) {
        if (!changed.containsKey(b.getLocation())) {
            ResetBlock sablock = new ResetBlock(b, c, b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST));
            changed.put(b.getLocation(), sablock);
            return sablock;
        }
        return null;
    }

    @Deprecated
    public void addChanged(Location l) {
        if (!changed.containsKey(l)) {
            changed.put(l, new ResetBlock(l, Material.AIR, (byte) 0));
        }
    }

    public void addChanged(Location l, Material m, byte data) {
        if (!changed.containsKey(l)) {
            changed.put(l, new ResetBlock(l, m, data));
        }
    }

    public void run() {
        int rolledBack = 0;

        // Rollback 70 blocks at game time
        Iterator<Map.Entry<Location, ResetBlock>> it = changed.entrySet().iterator();
        while (it.hasNext() && rolledBack <= 70) {
            ResetBlock ablock = it.next().getValue();

            try {
                resetBlock(ablock);
                it.remove();
            } catch (Exception e) {
                failedblocks.add(ablock);
            }

            rolledBack++;
        }

        if (changed.size() != 0) {
            Bukkit.getScheduler().runTaskLater(OpenSkyWars.getInstance(), this, 2L);
            return;
        }

        arenaLogger.debug(failedblocks.size() + " to redo.");

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OpenSkyWars.getInstance(), () -> {
            changed.clear();
            for (ResetBlock ablock : failedblocks) {
                Block b_ = ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation());
                if (!b_.getType().toString().equalsIgnoreCase(ablock.getMaterial().toString())) {
                    b_.setType(ablock.getMaterial());
                    b_.setData(ablock.getData());
                }
                if (b_.getType() == Material.CHEST) {
                    b_.setType(ablock.getMaterial());
                    b_.setData(ablock.getData());
                    ((Chest) b_.getState()).getInventory().setContents(ablock.getInventory());
                    b_.getState().update();
                }
            }
        }, 30L);
        if (game != null) game.state = Game.GameState.WAITING;
        arenaLogger.debug("Reset time: " + (System.currentTimeMillis() - time) + "ms");
    }

    /**
     * Resets all changed blocks in tasks each 70 blocks
     */
    public void reset() {
        time = System.currentTimeMillis();
        arenaLogger.debug(changed.size() + " to reset for arena " + ((game != null) ? game.arena.name : arena.name) + ".");
        Bukkit.getScheduler().runTask(OpenSkyWars.getInstance(), this);
    }

    /**
     * Resets the raw changed blocks on the main thread
     */
    public void resetRaw() {
        for (final ResetBlock ablock : changed.values()) {
            try {
                resetBlock(ablock);
            } catch (Exception e) {
                if (game != null) game.state = Game.GameState.WAITING;
            }
        }

        changed.clear();
        if (game != null) game.state = Game.GameState.WAITING;
    }

    private void resetBlock(ResetBlock ablock) {
        final Block b_ = ablock.getBlock().getWorld().getBlockAt(ablock.getBlock().getLocation());
        System.out.println(b_.getType());
        if (b_.getType() == Material.FURNACE) {
            ((Furnace) b_.getState()).getInventory().clear();
            b_.getState().update();
        }
        if (b_.getType() == Material.CHEST) {
            ((Chest) b_.getState()).getBlockInventory().clear();
            b_.getState().update();
        }
        if (b_.getType() == Material.DISPENSER) {
            ((Dispenser) b_.getState()).getInventory().clear();
            b_.getState().update();
        }
        if (b_.getType() == Material.DROPPER) {
            ((Dropper) b_.getState()).getInventory().clear();
            b_.getState().update();
        }
        if (!b_.getType().equals(ablock.getMaterial()) || b_.getData() != ablock.getData()) {
            b_.setType(ablock.getMaterial());
            b_.setData(ablock.getData());
        }
        if (b_.getType() == Material.CHEST) {
            ((Chest) b_.getState()).getBlockInventory().clear();
            b_.getState().update();
            HashMap<Integer, ItemStack> chestinv = ablock.getNewInventory();
            for (Integer i : chestinv.keySet()) {
                ItemStack item = chestinv.get(i);
                if (item != null) {
                    if (i < 27) {
                        ((Chest) b_.getState()).getBlockInventory().setItem(i, item);
                    }
                }
            }
            b_.getState().update();
        }
        if (b_.getType() == Material.DISPENSER) {
            Dispenser d = (Dispenser) b_.getState();
            d.getInventory().clear();
            resetInv(d, ablock.getNewInventory());
            d.getInventory().setContents(ablock.getInventory());
            d.update();
        }
        if (b_.getType() == Material.DROPPER) {
            Dropper d = (Dropper) b_.getState();
            d.getInventory().clear();
            resetInv(d, ablock.getNewInventory());
            d.update();
        }
        if (b_.getType() == Material.WALL_SIGN || b_.getType() == Material.SIGN_POST) {
            Sign sign = (Sign) b_.getState();
            if (sign != null) {
                int i = 0;
                for (String line : ablock.getSignLines()) {
                    sign.setLine(i, line);
                    i++;
                    if (i > 3) {
                        break;
                    }
                }
                sign.update();
            }
        }
        if (b_.getType() == Material.SKULL) {
            b_.setData((byte) 0x1);
            b_.getState().setType(Material.SKULL);
            if (b_.getState() instanceof Skull) {
                Skull s = (Skull) b_.getState();
                s.setSkullType(SkullType.PLAYER);
                s.setOwner(ablock.getSkullOwner());
                s.setRotation(ablock.getSkullORotation());
                s.update();
            }
        }
    }

    private void resetInv(InventoryHolder d, HashMap<Integer, ItemStack> chestinv) {
        for (Integer i : chestinv.keySet()) {
            ItemStack item = chestinv.get(i);
            if (item != null) {
                if (i < 9) {
                    d.getInventory().setItem(i, item);
                }
            }
        }
    }

    public void saveBlocksToFile(File f) {
        FileOutputStream fos;
        ObjectOutputStream oos = null;
        GZIPOutputStream gzip = null;
        try {
            fos = new FileOutputStream(f);
            gzip = new GZIPOutputStream(fos);
            oos = new BukkitObjectOutputStream(gzip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ResetBlock bl : changed.values()) {
            try {
                oos.writeObject(bl);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            oos.close();
            gzip.flush();
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        arenaLogger.debug("Saved blocks of " + ((game != null) ? game.arena.name : arena.name));
    }

    public void loadBlocksFromFile(File f, boolean delete) {
        if (!f.exists()) {
            return;
        }
        FileInputStream fis;
        BukkitObjectInputStream ois = null;
        GZIPInputStream gzip;
        try {
            fis = new FileInputStream(f);
            gzip = new GZIPInputStream(fis);
            ois = new BukkitObjectInputStream(gzip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                Object b = null;
                try {
                    b = ois.readObject();
                } catch (EOFException e) {
                    arenaLogger.debug("Finished restoring blocks for " + ((game != null) ? game.arena.name : arena.name) + ".");
                } catch (ClosedChannelException e) {
                    System.out.println("Something is wrong with your blocks file and the reset might not be successful.");
                }

                if (b != null) {
                    ResetBlock ablock = (ResetBlock) b;
                    resetBlock(ablock);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (f.exists() && delete) {
            f.delete();
        }
    }

}


