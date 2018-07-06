package me.checkium.openskywars.config;

import me.checkium.openskywars.OpenSkyWars;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChestsConfig {

    private static HashMap<String, HashMap<ItemStack, Integer>> items = new HashMap<>();
    public static HashMap<String, HashMap<ItemStack, Integer>> getItems() {
        return items;
    }

    public ChestsConfig() {
        loadType("basic");
        loadType("op");
    }
    public void loadType(String type) {
        File configFile = new File(OpenSkyWars.getInstance().getDataFolder() + "/chests/", type + ".yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (OpenSkyWars.getInstance().getResource("chests/" + type + ".yml") != null) {
                OpenSkyWars.getInstance().saveResource("chests/" + type + ".yml", false);
            } else {
                return;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (config.getConfigurationSection("items") != null) {
            HashMap<ItemStack, Integer> map = new HashMap<>();
            for (String key: config.getConfigurationSection("items").getKeys(false)) {
                if (isInteger(key)) {
                    int percent = Integer.valueOf(key);
                    List<ItemStack> itemsl = (List<ItemStack>) config.getList("items." + key);
                    for (ItemStack iStack: itemsl) {
                        map.put(iStack, percent);
                    }
                }
            }
            items.put(type, map);
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
