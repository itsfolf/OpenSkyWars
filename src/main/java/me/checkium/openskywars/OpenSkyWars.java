package me.checkium.openskywars;

import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.commands.MainCommand;
import me.checkium.openskywars.config.TeamsConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class OpenSkyWars extends JavaPlugin {

    public static OpenSkyWars getInstance() {
        return getPlugin(OpenSkyWars.class);
    }

    @Override
    public void onEnable() {
        ArenaManager.get().loadArenas();
        new TeamsConfig().load();

        getCommand("osw").setExecutor(new MainCommand());
        getCommand("osw").setTabCompleter(new MainCommand());
    }

    @Override
    public void onDisable() {
        ArenaManager.get().saveArenas();
    }
}
