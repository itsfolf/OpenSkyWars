package me.checkium.openskywars.config;

import me.checkium.openskywars.OpenSkyWars;

import java.io.File;
import java.util.Arrays;

class MainConfig extends fr.skyost.utils.Skyoconfig {

    @ConfigOptions(name = "prefix")
    public String prefix = "&7[&2OpenSkyWars&7]";

    public MainConfig() {
        super(new File(OpenSkyWars.getInstance().getDataFolder(), "/config.yml"), Arrays.asList("header"));
    }

}
