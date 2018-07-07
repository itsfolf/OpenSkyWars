package me.checkium.openskywars;

import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.arena.SignUpdater;
import me.checkium.openskywars.commands.MainCommand;
import me.checkium.openskywars.config.ChestsConfig;
import me.checkium.openskywars.config.MainConfig;
import me.checkium.openskywars.config.TeamsConfig;
import me.checkium.openskywars.game.GameManager;
import me.checkium.openskywars.lobby.SignListener;
import me.checkium.openskywars.lobby.Lobby;
import org.bukkit.plugin.java.JavaPlugin;

public class OpenSkyWars extends JavaPlugin {

    public static OpenSkyWars getInstance() {
        return getPlugin(OpenSkyWars.class);
    }

    MainConfig mainConfig;
    Lobby lobby;

    @Override
    public void onEnable() {
        mainConfig = new MainConfig();
        mainConfig.load();
        lobby = new Lobby();
        ArenaManager.get().loadArenas();
        GameManager.get().init();
        new TeamsConfig();
        new ChestsConfig();
        new SignUpdater();

        getCommand("osw").setExecutor(new MainCommand());
        getCommand("osw").setTabCompleter(new MainCommand());

        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }

    @Override
    public void onDisable() {
        GameManager.get().getGames().forEach(game -> {
            if (!game.players.isEmpty()) {
                game.resetGame(false);
            }
        });
        ArenaManager.get().saveArenas();
        lobby.close();
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public Lobby getLobby() {
        return lobby;
    }
}
