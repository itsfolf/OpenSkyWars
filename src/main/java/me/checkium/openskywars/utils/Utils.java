package me.checkium.openskywars.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static ArrayList<JsonElement> toArray(JsonArray array) {
        ArrayList<JsonElement> r = new ArrayList<>();
        array.forEach(r::add);
        return r;
    }

    public static Location fromString(String s) {
        String[] args = s.split(", ");
        return new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
    }

    public static String locationToString(Location l) {
        return l.getWorld().getName() + ", " + l.getX() + ", " + l.getY() + ", " + l.getZ();
    }

    public static void spawnParticle(Player p, Location l, int r, int g, int b) {
        p.spigot().playEffect(l, Effect.COLOURED_DUST, 0, 1, (float) r / 255, (float) g / 255, (float) b / 255, 1, 0, 64);
    }

    public static Color translateChatColorToColor(ChatColor chatColor) {
        switch (chatColor) {
            case AQUA:
                return Color.fromRGB(85, 255, 255);
            case BLACK:
                return Color.fromRGB(0, 0, 0);
            case BLUE:
                return Color.fromRGB(85, 85, 255);
            case DARK_AQUA:
                return Color.fromRGB(0, 170, 160);
            case DARK_BLUE:
                return Color.fromRGB(0, 0, 170);
            case DARK_GRAY:
                return Color.fromRGB(85, 85, 85);
            case DARK_GREEN:
                return Color.fromRGB(0, 170, 0);
            case DARK_PURPLE:
                return Color.fromRGB(170, 0, 170);
            case DARK_RED:
                return Color.fromRGB(170, 0, 0);
            case GOLD:
                return Color.fromRGB(255, 170, 0);
            case GRAY:
                return Color.fromRGB(170, 170, 170);
            case GREEN:
                return Color.fromRGB(85, 255, 85);
            case LIGHT_PURPLE:
                return Color.fromRGB(255, 85, 255);
            case RED:
                return Color.fromRGB(255, 85, 85);
            case WHITE:
                return Color.fromRGB(255, 255, 255);
            case YELLOW:
                return Color.fromRGB(255, 255, 85);
            default:
                break;
        }
        return null;
    }

    public static Arena getSignOwner(Location l) {
        for (Arena loadedArena : ArenaManager.get().loadedArenas) {
            for (Location sign : loadedArena.signs) {
                if (l.equals(sign)) return loadedArena;
            }
        }
        return null;
    }

    public static List<String> match(String match, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(match)) {
                matches.add(option);
            }
        }
        return matches;
    }


}
