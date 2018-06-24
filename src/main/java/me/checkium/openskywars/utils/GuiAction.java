package me.checkium.openskywars.utils;

import me.checkium.openskywars.OpenSkyWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Easily create menu GUIs with up to 5 options
 * with runnables.
 *
 * @author Checkium
 */
public class GuiAction implements Listener {

    String title;
    ItemStack main;
    List<ItemStack> options = new ArrayList<>();
    List<Runnable> runnables = new ArrayList<>();
    Inventory inv;
    Player p;

    public GuiAction(String title, ItemStack main, List<ItemStack> options, List<Runnable> runnables, Player p) {
        this.title = title;
        this.main = main;
        this.options = options;
        this.runnables = runnables;
        build(p);
    }

    public GuiAction() {
    }

    public GuiAction add(ItemStack option, Runnable runnable) {
        options.add(option);
        runnables.add(runnable);
        return this;
    }

    public GuiAction main(ItemStack main) {
        this.main = main;
        return this;
    }

    public GuiAction title(String title) {
        this.title = title;
        return this;
    }

    public void build(Player p) {
        inv = Bukkit.createInventory(null, 9 * 4, title);
        inv.setItem(4, main);
        int n = 0;
        for (int i : getPlaces()) {
            inv.setItem(17 + i, options.get(n));
            n++;
        }
        register();
        p.openInventory(inv);
    }

    private int[] getPlaces() {
        int[] places;
        switch (options.size()) {
            case 1:
                places = new int[]{5};
                break;
            case 2:
                places = new int[]{3, 7};
                break;
            case 3:
                places = new int[]{3, 5, 7};
                break;
            case 4:
                places = new int[]{2, 4, 6, 8};
                break;
            case 5:
                places = new int[]{1, 3, 5, 7, 9};
                break;
            default:
                places = new int[]{0};
        }
        return places;
    }


    private void register() {
        Bukkit.getServer().getPluginManager().registerEvents(this, OpenSkyWars.getInstance());
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getUniqueId().equals(p.getUniqueId())) {
            if (e.getInventory().getName().equals(inv.getName())) {
                if (e.getCurrentItem() != null) {
                    if (options.contains(e.getCurrentItem())) {
                        runnables.get(options.indexOf(e.getCurrentItem())).run();
                    }
                }
                e.setCancelled(true);
            }
        }
    }


}
