package me.checkium.openskywars.arena;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.checkium.openskywars.utils.Cuboid;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static me.checkium.openskywars.utils.Utils.*;

public class Arena {
    String name;
    boolean enabled;
    int teamSize, minTeams, maxTeams;
    int lobbyCountdown, gameLength;
    List<Integer> refillTimes;
    HashMap<String, Location> teams;
    HashMap<Location, String> chests;
    Cuboid cuboid;

    public Arena(JsonObject data) {
        this.name = data.get("name").getAsString();
        this.enabled = data.get("enabled").getAsBoolean();
        this.teamSize = data.get("teamSize").getAsInt();
        this.minTeams = data.get("minTeams").getAsInt();
        this.maxTeams = data.get("maxTeams").getAsInt();
        this.lobbyCountdown = data.get("lobbyCountdown").getAsInt();
        this.gameLength = data.get("gameLength").getAsInt();
        this.refillTimes = toArray(data.get("refillTimes").getAsJsonArray()).stream().map(JsonElement::getAsInt).collect(Collectors.toList());
        this.teams = new HashMap<>();
        toArray(data.get("teams").getAsJsonArray()).stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()).forEach(jsonObject -> {
            teams.put(jsonObject.get("name").getAsString(), fromString(jsonObject.get("spawnpoint").getAsString()));
        });
        this.chests = new HashMap<>();
        toArray(data.get("chests").getAsJsonArray()).stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()).forEach(jsonObject -> {
            chests.put(fromString(jsonObject.get("location").getAsString()), jsonObject.get("type").getAsString());
        });
        this.cuboid = Cuboid.fromString(data.get("cuboid").getAsString());
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("enabled", enabled);
        object.addProperty("teamSize", teamSize);
        object.addProperty("minTeams", minTeams);
        object.addProperty("maxTeams", maxTeams);
        object.addProperty("lobbyCountdown", lobbyCountdown);
        object.addProperty("gameLength", gameLength);
        object.addProperty("refillTimes", new Gson().toJson(refillTimes));
        JsonObject teamsObject = new JsonObject();
        teams.forEach((s, location) -> {
            teamsObject.addProperty("name", s);
            teamsObject.addProperty("spawnpoint", locationToString(location));
        });
        object.add("teams", teamsObject);
        JsonObject chestsObject = new JsonObject();
        chests.forEach((location, s) -> {
            chestsObject.addProperty("location", locationToString(location));
            chestsObject.addProperty("type", s);
        });
        object.add("chests", chestsObject);
        object.addProperty("cuboid", cuboid.toString());
        return object;
    }
}
