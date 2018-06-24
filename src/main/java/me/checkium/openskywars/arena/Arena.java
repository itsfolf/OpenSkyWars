package me.checkium.openskywars.arena;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.checkium.openskywars.utils.ChainedTextComponent;
import me.checkium.openskywars.utils.Cuboid;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static me.checkium.openskywars.utils.Utils.*;

public class Arena {
    public String name;
    public  boolean enabled;
    public  int teamSize, minTeams, maxTeams;
    public int lobbyCountdown, gameLength;
    public  List<Integer> refillTimes = new ArrayList<>();
    public HashMap<String, Location> teams = new HashMap<>();
    public  HashMap<Location, String> chests = new HashMap<>();
    public Cuboid cuboid = new Cuboid(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), new Location(Bukkit.getWorlds().get(0), 0, 0, 0));

    public Arena(String name) {
        this.name = name;
    }

    public Arena(JsonObject data) {
        this.name = data.get("name").getAsString();
        this.enabled = data.get("enabled").getAsBoolean();
        this.teamSize = data.get("teamSize").getAsInt();
        this.minTeams = data.get("minTeams").getAsInt();
        this.maxTeams = data.get("maxTeams").getAsInt();
        this.lobbyCountdown = data.get("lobbyCountdown").getAsInt();
        this.gameLength = data.get("gameLength").getAsInt();
        this.refillTimes = toArray(data.get("refillTimes").getAsJsonArray()).stream().map(JsonElement::getAsInt).collect(Collectors.toList());
        toArray(data.get("teams").getAsJsonArray()).stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()).forEach(jsonObject -> {
            teams.put(jsonObject.get("name").getAsString(), fromString(jsonObject.get("spawnpoint").getAsString()));
        });
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

    public TextComponent getComponent() {
        return new ChainedTextComponent("==========Arena ").color(ChatColor.GREEN).add(new ChainedTextComponent(name).color(ChatColor.BLUE)).add(new ChainedTextComponent("==========").color(ChatColor.GREEN))
                .add(new ChainedTextComponent("\nEnabled - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(enabled)).suggestOnClick(command() + " enabled <true/false>"))
                .add(new ChainedTextComponent("\nTeam size - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(teamSize)).suggestOnClick(command() + " teamsize <number>"))
                .add(new ChainedTextComponent("\nMin teams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(minTeams)).suggestOnClick(command() + " minteams <number>"))
                .add(new ChainedTextComponent("\nMax teams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(maxTeams)).suggestOnClick(command() + " maxteams <number>"))
                .add(new ChainedTextComponent("\nLobby countdown - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(lobbyCountdown)).suggestOnClick(command() + " lobbycountdown <number>"))
                .add(new ChainedTextComponent("\nGame length - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(gameLength)).suggestOnClick(command() + " gameLength <number>"))
                .add(new ChainedTextComponent("\nRefill times - ").color(ChatColor.GREEN).add(new ChainedTextComponent(getElement(refillTimes)).suggestOnClick(command() + " refilltiems <add/remove> <number>")))
                .add(new ChainedTextComponent("\nTeams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(teams)).suggestOnClick(command() + " spawnsetup"))
                .add(new ChainedTextComponent("\nChests - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(chests)).suggestOnClick(command() + " chestsetup"))
                .add(new ChainedTextComponent("\nSize - ").color(ChatColor.GREEN)).add(new ChainedTextComponent("" + cuboid.getSize()).color(ChatColor.BLUE).suggestOnClick(command() + " bordersetuo"))
                .add(new ChainedTextComponent("\n=============================").color(ChatColor.GREEN)).get();
    }

    public String getElement(Object element) {
        if (element != null) {
            if (element instanceof List) {
                return ChatColor.BLUE + "" + ((List) element).stream().map(o -> o.toString()).collect(Collectors.joining(", "));
            } else if (element instanceof HashMap) {
                if (((HashMap) element).keySet().stream().anyMatch(o -> o instanceof String)) {
                    return ChatColor.BLUE + "" + ((HashMap) element).keySet().stream().collect(Collectors.joining(ChatColor.BLUE + ", "));
                } else {
                    return ChatColor.BLUE + "" + ((HashMap) element).size() + " set.";
                }
            } else {
                return ChatColor.BLUE + element.toString();
            }
        } else {
            return ChatColor.RED + "Unset";
        }
    }

    public String enable() {
        String response = ChatColor.RED + "" + ChatColor.BOLD + "Error: " + ChatColor.RESET + "" + ChatColor.RED;
        if (teamSize <= 0) {
            response += "Team size needs to be greater than zero.";
        } else if (minTeams <= 0) {
            response += "Min teams needs to be greater than zero.";
        } else if (maxTeams <= 0) {
            response += "Max teams needs to be greater than zero.";
        } else if (minTeams > maxTeams) {
            response += "Min teams needs to be less than max teams.";
        } else if (lobbyCountdown <= 0) {
            response += "Lobby countdown needs to be greater than zero.";
        } else if (gameLength <= 0) {
            response += "Game length needs to be greater than zero.";
        } else if (teams.size() <= 0) {
            response += "The amount of teams needs to be greater than zero.";
        } else if (teams.size() != maxTeams) {
            response += "The amount of teams needs to be the same as the max teams";
        } else {
            response = ChatColor.GREEN + "Successfully created arena " + ChatColor.BLUE + name;
        }
        return response;
    }

    public String command() {
        return "/osw arena " + name;
    }
}
