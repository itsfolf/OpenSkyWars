package me.checkium.openskywars.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<JsonElement> toArray(JsonArray array) {
        ArrayList<JsonElement> r = new ArrayList<>();
        array.forEach(a -> r.add(a));
        return r;
    }

    public static Location fromString(String s) {
        String[] args = s.split(":");
        return new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
    }

    public static String locationToString(Location l) {
        return l.getWorld().getName() + ":" + l.getX() + ":" + l.getY() + ":" + l.getZ();
    }
}
