package me.checkium.openskywars.game;

import me.checkium.openskywars.OpenSkyWars;
import me.checkium.openskywars.arena.Arena;
import me.checkium.openskywars.arena.ArenaManager;
import me.checkium.openskywars.game.regen.GameReset;
import me.checkium.openskywars.utils.Logger;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private List<Game> games = new ArrayList<>();
    private Logger logger = Logger.getLogger("GameManager");

    public static GameManager get() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    private void createGame(Arena a) {
        if (!a.enabled) return;
        logger.debug("Creating game for arena " + a.name);
        Game game = new Game(a);

        File file = new File(OpenSkyWars.getInstance().getDataFolder() + "/cache");
        if (!file.exists()) file.mkdirs();
        File cacheFile = new File(file, a.name + "_cache");
        if (cacheFile.exists()) {
            GameReset temp = new GameReset(game);
            temp.loadBlocksFromFile(cacheFile, true);
        }

        games.add(game);
    }

    public List<Game> getGames() {
        return games;
    }

    public Game getGame(Player p) {
        return games.stream().filter(game -> game.players.containsKey(p)).findFirst().orElse(null);
    }

    public void init() {
        ArenaManager.get().loadedArenas.forEach(this::createGame);
    }
}
