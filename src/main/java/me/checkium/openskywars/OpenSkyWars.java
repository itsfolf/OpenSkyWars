package me.checkium.openskywars;

import me.checkium.openskywars.arena.ArenaManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenSkyWars extends JavaPlugin {

    private static OpenSkyWars instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {

    }

    public static OpenSkyWars getInstance() {
        return instance;
    }
}
