package me.checkium.openskywars.config;

import me.checkium.openskywars.OpenSkyWars;

import java.io.File;
import java.util.Arrays;

public class MainConfig extends fr.skyost.utils.Skyoconfig {

    @ConfigOptions(name = "prefix")
    public String prefix = "&7[&2OpenSkyWars&7]";

    @ConfigOptions(name = "lobby.enabled")
    public boolean lobbyEnabled = true;

    @ConfigOptions(name = "lobby.bungeecord")
    public boolean autoLobby = false;

    @ConfigOptions(name = "lobby.location")
    public String lobbyLocation = null;

    public MainConfig() {
        super(new File(OpenSkyWars.getInstance().getDataFolder(), "/config.yml"), Arrays.asList("header"));
    }

}
