package me.checkium.openskywars.config;

import me.checkium.openskywars.OpenSkyWars;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TeamsConfig {

    private static FileConfiguration config;
    private static HashMap<String, String> teams = new HashMap<>();

    public static FileConfiguration get() {
        return config;
    }

    public static HashMap<String, String> getTeams() {
        return teams;
    }

    public TeamsConfig() {
        File configFile = new File(OpenSkyWars.getInstance().getDataFolder(), "teams.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            OpenSkyWars.getInstance().saveResource("teams.yml", false);
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        config.getKeys(false).forEach(s -> teams.put(s.toUpperCase(), ChatColor.translateAlternateColorCodes('&', config.getString(s))));
    }
}
