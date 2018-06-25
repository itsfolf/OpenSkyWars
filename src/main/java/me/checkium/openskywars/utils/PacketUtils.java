package me.checkium.openskywars.utils;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

class PacketUtils {


    public static void sendPacket(Player p, Object packet) {
        try {
            Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

    public static double getVersion() {
        String[] ver = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1).split("_");
        return Double.valueOf(ver[0] + "." + ver[1]);
    }
}
