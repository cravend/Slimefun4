package io.github.thebusybiscuit.slimefun4.core.services.plugins;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;

import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.MaterialParseEvent;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;

class ChestShopHook implements Listener {
    ChestShopHook (SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Slimefun.getLogger().log(Level.INFO, "CUSTOM FILE REGISTERED");
    }


    @EventHandler
    public void onParseItem(ItemParseEvent e) {
      Slimefun.getLogger().log(Level.INFO, "ITEM PARSE 2");
      e.setItem(new ItemStack(Material.ACACIA_BOAT));
      Slimefun.getLogger().log(Level.INFO, e.getItemString());
    }

    @EventHandler
    public void onParseMaterial(MaterialParseEvent e) {
      Slimefun.getLogger().log(Level.INFO, "MATERIAL PARSE 2");
      e.setMaterial(Material.ACACIA_BOAT);
      Slimefun.getLogger().log(Level.INFO, e.getMaterialString());
    }
}
