package me.checkium.openskywars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Cuboid {
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    public String worldName;

    public Cuboid(Location location, Location location2) {
        worldName = location.getWorld().getName();
        x1 = Math.min(location.getBlockX(), location2.getBlockX());
        y1 = Math.min(location.getBlockY(), location2.getBlockY());
        z1 = Math.min(location.getBlockZ(), location2.getBlockZ());
        x2 = Math.max(location.getBlockX(), location2.getBlockX());
        y2 = Math.max(location.getBlockY(), location2.getBlockY());
        z2 = Math.max(location.getBlockZ(), location2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (!location.getWorld().getName().equals(worldName)) return false;
        if (location.getBlockX() < x1) return false;
        if (location.getBlockX() > x2) return false;
        if (location.getBlockY() <= y1) return false;
        if (location.getBlockY() >= y2) return false;
        if (location.getBlockZ() < z1) return false;
        if (location.getBlockZ() > z2) return false;
        return true;
    }

    public int getLowerY() {
        return y1;
    }

    public int getSize() {
        return (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
    }

    public List<Location> getBorders() {
        List<Location> locs = new ArrayList<>();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        for (int i = minX; i < maxX; i++) {
            locs.add(new Location(Bukkit.getWorld(worldName), i, minY, minZ));
            locs.add(new Location(Bukkit.getWorld(worldName), i, minY, maxZ));
            locs.add(new Location(Bukkit.getWorld(worldName), i, maxY, minZ));
            locs.add(new Location(Bukkit.getWorld(worldName), i, maxY, maxZ));
        }
        for (int i = minZ; i < maxZ; i++) {
            locs.add(new Location(Bukkit.getWorld(worldName), minX, minY, i));
            locs.add(new Location(Bukkit.getWorld(worldName), maxX, minY, i));
            locs.add(new Location(Bukkit.getWorld(worldName), minX, maxY, i));
            locs.add(new Location(Bukkit.getWorld(worldName), maxX, maxY, i));
        }
        for (int i = minY; i < maxY; i++) {
            locs.add(new Location(Bukkit.getWorld(worldName), minX, i, minZ));
            locs.add(new Location(Bukkit.getWorld(worldName), maxX, i, maxZ));
            locs.add(new Location(Bukkit.getWorld(worldName), minX, i, maxZ));
            locs.add(new Location(Bukkit.getWorld(worldName), maxX, i, minZ));
        }
        return locs;
    }

    public List<Location> getAll() {
        List<Location> locs = new ArrayList<>();
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        for (int i = minX; i < maxX; i++) {
            for (int j = minZ; j < maxZ; j++) {
                for (int k = minY; k < maxY; k++) {
                    locs.add(new Location(Bukkit.getWorld(worldName), i, k, j));
                }
            }
        }
        return locs;
    }

    public Location getRandomLocation() {
        int n;
        Random random;
        int n2;
        int n3;
        Location location;
        World world = Bukkit.getWorld(worldName);
        Location location2 = new Location(world, (x1 + (n3 = (random = new Random()).nextInt(x2 - x1 + 1))), (y1 + (n = random.nextInt(y2 - y1 + 1))), (z1 + (n2 = random.nextInt(z2 - z1 + 1))));
        if (location2.getBlock().getType().equals(Material.AIR)) {
            location = location2;
            return location;
        }
        location = world.getHighestBlockAt(location2).getLocation();
        return location;
    }


    public String toString() {
        return String.valueOf(worldName) + ", " + x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2;
    }

    public static Cuboid fromString(String s) {
        String[] args = s.split(", ");
        World w = Bukkit.getWorld(args[0]);
        Location l1 = new Location(w, Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
        Location l2 = new Location(w, Double.valueOf(args[4]), Double.valueOf(args[5]), Double.valueOf(args[6]));
        return new Cuboid(l1, l2);
    }


}
