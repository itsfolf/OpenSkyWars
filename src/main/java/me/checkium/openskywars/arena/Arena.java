package me.checkium.openskywars.arena;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.checkium.openskywars.utils.ChainedTextComponent;
import me.checkium.openskywars.utils.Cuboid;
import me.checkium.openskywars.utils.Utils;
import me.checkium.openskywars.utils.WorldUtils;
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
    public String prettyName;
    public boolean enabled;
    public int teamSize, minTeams, maxTeams;
    public int lobbyCountdown, gameLength;
    public List<Integer> refillTimes = new ArrayList<>();
    public HashMap<String, Location> teams = new HashMap<>();
    public HashMap<Location, String> chests = new HashMap<>();
    public Cuboid cuboid = new Cuboid(new Location(Bukkit.getWorlds().get(0), 0, 0, 0), new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
    public List<Location> signs = new ArrayList<>();
    public Location lobby;

    public Arena(String name) {
        this.name = name;
        this.prettyName = name;
    }

    Arena(JsonObject data) {
        this.name = data.get("name").getAsString();
        this.prettyName = data.get("prettyName").getAsString();
        this.enabled = data.get("enabled").getAsBoolean();
        this.teamSize = data.get("teamSize").getAsInt();
        this.minTeams = data.get("minTeams").getAsInt();
        this.maxTeams = data.get("maxTeams").getAsInt();
        this.lobbyCountdown = data.get("lobbyCountdown").getAsInt();
        this.gameLength = data.get("gameLength").getAsInt();
        this.refillTimes = toArray(data.get("refillTimes").getAsJsonArray()).stream().map(JsonElement::getAsInt).collect(Collectors.toList());
        this.cuboid = Cuboid.fromString(data.get("cuboid").getAsString());
        WorldUtils.getWorld(cuboid.worldName);
        toArray(data.get("teams").getAsJsonArray()).stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()).forEach(jsonObject -> teams.put(jsonObject.get("name").getAsString(), fromString(jsonObject.get("spawnpoint").getAsString())));
        toArray(data.get("chests").getAsJsonArray()).stream().map(JsonElement::getAsJsonObject).collect(Collectors.toList()).forEach(jsonObject -> chests.put(fromString(jsonObject.get("location").getAsString()), jsonObject.get("type").getAsString()));
        this.signs = toArray(data.get("signs").getAsJsonArray()).stream().map(jsonElement -> Utils.fromString(jsonElement.getAsString())).collect(Collectors.toList());
        this.lobby = Utils.fromString(data.get("lobby").getAsString());
    }

    JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("prettyName", prettyName);
        object.addProperty("enabled", enabled);
        object.addProperty("teamSize", teamSize);
        object.addProperty("minTeams", minTeams);
        object.addProperty("maxTeams", maxTeams);
        object.addProperty("lobbyCountdown", lobbyCountdown);
        object.addProperty("gameLength", gameLength);
        object.add("refillTimes", new GsonBuilder().create().toJsonTree(refillTimes));
        JsonArray teamsArray = new JsonArray();
        teams.forEach((s, location) -> {
            WorldUtils.getWorld(cuboid.worldName);
            JsonObject teamsObject = new JsonObject();
            teamsObject.addProperty("name", s);
            teamsObject.addProperty("spawnpoint", locationToString(location));
            teamsArray.add(teamsObject);
        });
        object.add("teams", teamsArray);
        JsonArray chestsArray = new JsonArray();
        chests.forEach((location, s) -> {
            JsonObject chestsObject = new JsonObject();
            chestsObject.addProperty("location", locationToString(location));
            chestsObject.addProperty("type", s);
            chestsArray.add(chestsObject);
        });
        object.add("chests", chestsArray);
        object.addProperty("cuboid", cuboid.toString());
        object.add("signs", new GsonBuilder().create().toJsonTree(signs.stream().map(location -> Utils.locationToString(location)).collect(Collectors.toList())));
        object.addProperty("lobby", Utils.locationToString(lobby));
        return object;
    }

    public TextComponent getComponent() {
        return new ChainedTextComponent("==========Arena ").color(ChatColor.GREEN).add(new ChainedTextComponent(name).color(ChatColor.BLUE)).add(new ChainedTextComponent("==========").color(ChatColor.GREEN))
                .add(new ChainedTextComponent("\nPretty name - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(prettyName).suggestOnClick(command() + " name <name>"))
                .add(new ChainedTextComponent("\nEnabled - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(enabled)).suggestOnClick(command() + " enabled <true/false>"))
                .add(new ChainedTextComponent("\nTeam size - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(teamSize)).suggestOnClick(command() + " teamsize <number>"))
                .add(new ChainedTextComponent("\nMin teams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(minTeams)).suggestOnClick(command() + " minteams <number>"))
                .add(new ChainedTextComponent("\nMax teams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(maxTeams)).suggestOnClick(command() + " maxteams <number>"))
                .add(new ChainedTextComponent("\nLobby countdown - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(lobbyCountdown)).suggestOnClick(command() + " lobbycountdown <number>"))
                .add(new ChainedTextComponent("\nGame length - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(gameLength)).suggestOnClick(command() + " gameLength <number>"))
                .add(new ChainedTextComponent("\nRefill times - ").color(ChatColor.GREEN).add(new ChainedTextComponent(getElement(refillTimes)).suggestOnClick(command() + " refilltimes <add/remove> <number>")))
                .add(new ChainedTextComponent("\nSize - ").color(ChatColor.GREEN)).add(new ChainedTextComponent("" + cuboid.getSize()).color(cuboid.getSize() > 1 ? ChatColor.BLUE : ChatColor.RED).suggestOnClick(command() + " bordersetup"))
                .add(new ChainedTextComponent("\nTeams - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(teams)).suggestOnClick(command() + " spawnsetup"))
                .add(new ChainedTextComponent("\nLobby - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(lobby)).suggestOnClick(command() + " lobby"))
                .add(new ChainedTextComponent("\nChests - ").color(ChatColor.GREEN)).add(new ChainedTextComponent(getElement(chests)).suggestOnClick(command() + " chestsetup"))
                .add(new ChainedTextComponent("\n============").color(ChatColor.GREEN).add(new ChainedTextComponent("Save").suggestOnClick(command() + " save").color(ChatColor.GREEN).bold().add(new ChainedTextComponent("=============").color(ChatColor.GREEN)))).get();
    }

    private String getElement(Object element) {
        if (element != null) {
            if (element instanceof List) {
                String r = ChatColor.BLUE + "" + ((List) element).stream().map(Object::toString).collect(Collectors.joining(", "));
                return r.length() < 3 ? ChatColor.RED + "None." : r;
            } else if (element instanceof HashMap) {
                if (((HashMap) element).keySet().stream().anyMatch(o -> o instanceof String)) {
                    return ChatColor.BLUE + "" + ((HashMap) element).keySet().stream().collect(Collectors.joining(ChatColor.BLUE + ", "));
                } else {
                    return (((HashMap) element).size() > 0 ? ChatColor.BLUE : ChatColor.RED) + "" + ((HashMap) element).size() + " set.";
                }
            } else if (element instanceof Integer) {
                return (Integer) element > 0 ? ChatColor.BLUE + "" + element : ChatColor.RED + "" + element;
            } else if (element instanceof Boolean) {
                return (Boolean) element ? ChatColor.BLUE + "" + element : ChatColor.RED + "" + element;
            } else if (element instanceof Location) {
                return Utils.locationToString((Location) element);
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
            response = ChatColor.GREEN + "The arena " + ChatColor.BLUE + name + ChatColor.GREEN + " is now enabled" + ChatColor.GREEN + "!";
        }
        return response;
    }

    private String command() {
        return "/osw arena " + name;
    }
}
