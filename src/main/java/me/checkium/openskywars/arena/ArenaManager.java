package me.checkium.openskywars.arena;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.utils.Logger;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaManager {

    private static ArenaManager instance;
    public List<Arena> loadedArenas = new ArrayList<>();
    Logger logger = Logger.getLogger("ArenaManager");

    public static ArenaManager get() {
        if (instance == null) {
            instance = new ArenaManager();
        }
        return instance;
    }

    public void loadArenas() {
        logger.info(ChatColor.GREEN + "Loading arenas...");
        long start = System.currentTimeMillis();
        File arenaFolder = new File(OpenSkyWars.getInstance().getDataFolder() + "/arenas");
        if (!arenaFolder.exists()) arenaFolder.mkdirs();
        File[] files = arenaFolder.listFiles();
        int num = 0, invalid = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                try {
                    String content = Files.readAllLines(Paths.get(file.getPath())).stream().collect(Collectors.joining());
                    try {
                        JsonObject object = new JsonParser().parse(content).getAsJsonObject();
                        Arena a = new Arena(object);
                        loadedArenas.add(a);
                        num++;
                    } catch (JsonParseException e) {
                        logger.error(ChatColor.RED + "Arena file " + file.getName() + " is invalid.");
                        invalid++;
                    }
                } catch (IOException e) {
                    logger.error(ChatColor.RED + "There was an error loading " + file.getName() + ":\n" + e.getMessage());
                }
            } else {
                invalid++;
            }
        }
        logger.info(ChatColor.GREEN + "Loaded " + num + " arenas in " + (System.currentTimeMillis() - start) + "ms (" + invalid + " invalid).");
    }

    public Arena forName(String name) {
        return loadedArenas.stream().filter(arena -> arena.name.equals(name)).findFirst().orElse(null);
    }

}
