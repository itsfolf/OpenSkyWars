package me.checkium.openskywars.utils;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.game.Game;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class WorldUtils {

    public static World createWorld(String name) {
        World world = Bukkit.createWorld(new WorldCreator(name).generator(new EmptyWorldGenerator()));
        Chunk c = world.getChunkAt(0,0);
        c.load();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
              c.getBlock(i, 0, j).setType(Material.STONE);
            }
        }
        Block block = c.getBlock(7,1,7);
        block.setType(Material.SIGN_POST);
        Sign s = (Sign) block.getState();
        s.setLine(0, "World created by");
        s.setLine(1, "OpenSkyWars:");
        s.setLine(3, name);
        s.update();
        return world;
    }

    public static World getWorld(String name) {
        World w;
        if ((w = Bukkit.getWorld(name)) == null) {
            if (worldExists(name)) {
                w = Bukkit.createWorld(new WorldCreator(name).generator(new EmptyWorldGenerator()));
            }
        }
        return w;
    }

    public static boolean worldExists(String name) {
        return getWorldFile(name).exists();
    }

    public static File getWorldFile(String name) {
        return new File(OpenSkyWars.getInstance().getDataFolder().getParentFile().getParentFile(), name);
    }

    public static void deleteWorld(String name) {
        Bukkit.unloadWorld(getWorld(name), false);
        for (File file : WorldUtils.getWorldFile(name).listFiles()) {
            if (file.isDirectory()) {
                for (File file1 : file.listFiles()) {
                    file1.delete();
                }
            }
            file.delete();
        }
        WorldUtils.getWorldFile(name).delete();
    }

    public static List<Block> generateCage(Game g, Location l, List<Material> block) {
        if (block.size() != 5) return new ArrayList<>();
        HashMap<Block, Integer> blocks = new HashMap<>();
        l.add(0, 1,0);
        blocks.put(l.getBlock().getRelative(BlockFace.DOWN), 4);
        blocks.put(l.getBlock().getRelative(BlockFace.NORTH), 3);
        blocks.put(l.getBlock().getRelative(BlockFace.SOUTH), 3);
        blocks.put(l.getBlock().getRelative(BlockFace.EAST), 3);
        blocks.put(l.getBlock().getRelative(BlockFace.WEST), 3);
        Block secondLayer = l.getBlock().getRelative(BlockFace.UP);
        blocks.put(secondLayer.getRelative(BlockFace.NORTH), 2);
        blocks.put(secondLayer.getRelative(BlockFace.SOUTH), 2);
        blocks.put(secondLayer.getRelative(BlockFace.EAST), 2);
        blocks.put(secondLayer.getRelative(BlockFace.WEST), 2);
        Block thirdLayer = secondLayer.getRelative(BlockFace.UP);
        blocks.put(thirdLayer.getRelative(BlockFace.NORTH), 2);
        blocks.put(thirdLayer.getRelative(BlockFace.SOUTH), 2);
        blocks.put(thirdLayer.getRelative(BlockFace.EAST), 2);
        blocks.put(thirdLayer.getRelative(BlockFace.WEST), 2);
        blocks.put(thirdLayer.getRelative(BlockFace.UP),0 );
        blocks.forEach((block1, integer) -> {
            g.reset.addChanged(block1);
            block1.setType(block.get(integer));
        });
        return new ArrayList<>(blocks.keySet());
    }



    private static List<Integer> randomLoc = new ArrayList<>();
    private static List<Integer> randomDLoc = new ArrayList<>();
    static {
        for (int i = 0; i < 27; i++) {
            randomLoc.add(i);
        }
        for (int i = 0; i < 54; i++) {
            randomDLoc.add(i);
        }
    }
    /**
     * @author walrusone, Checkium
     */
    public static void fillChest(Object chest, HashMap<ItemStack, Integer> fill) {
        Inventory inventory = null;
        if (chest instanceof Chest) {
            inventory = ((Chest) chest).getInventory();
        } else if (chest instanceof DoubleChest) {
            inventory = ((DoubleChest) chest).getInventory();
        }
        if (inventory != null) {
            inventory.clear();
            final int[] added = {0};
            Collections.shuffle(randomLoc);
            Collections.shuffle(randomDLoc);

            Inventory finalInventory = inventory;
            fill.forEach((itemStack, chance) -> {
                if (added[0] >= finalInventory.getSize() - 1) {
                    return;
                }
                if (new Random().nextInt(100) + 1 <= chance) {
                    finalInventory.setItem(randomLoc.get(added[0]), itemStack);
                    added[0]++;
                }
            });
        }
    }


}
