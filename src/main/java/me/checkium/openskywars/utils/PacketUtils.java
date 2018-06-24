package me.checkium.openskywars.utils;


import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PacketUtils {


    public static void sendPacket(Player p, Object packet) {
        try {
            Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }
}
