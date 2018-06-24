package me.checkium.openskywars;

import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.commands.MainCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class OpenSkyWars extends JavaPlugin {

    @Override
    public void onEnable() {
        ArenaManager.get().loadArenas();

        getCommand("osw").setExecutor(new MainCommand());
        getCommand("osw").setTabCompleter(new MainCommand());
    }

    @Override
    public void onDisable() {

    }

    public static OpenSkyWars getInstance() {
        return getPlugin(OpenSkyWars.class);
    }
}
