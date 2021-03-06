package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.materials.MaterialCollections;
import io.github.thebusybiscuit.slimefun4.implementation.items.magical.talismans.MagicianTalisman;
import io.github.thebusybiscuit.slimefun4.implementation.items.magical.talismans.MagicianTalisman.TalismanEnchantment;
import io.github.thebusybiscuit.slimefun4.implementation.items.magical.talismans.Talisman;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.api.Slimefun;

public class TalismanListener implements Listener {

    private final int[] armorSlots = { 39, 38, 37, 36 };

    public TalismanListener(SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageGet(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getCause() == DamageCause.LAVA) {
                Talisman.checkFor(e, SlimefunItems.TALISMAN_LAVA);
            }

            if (e.getCause() == DamageCause.DROWNING) {
                Talisman.checkFor(e, SlimefunItems.TALISMAN_WATER);
            }

            if (e.getCause() == DamageCause.FALL) {
                Talisman.checkFor(e, SlimefunItems.TALISMAN_ANGEL);
            }

            if (e.getCause() == DamageCause.FIRE) {
                Talisman.checkFor(e, SlimefunItems.TALISMAN_FIRE);
            }

            if (e.getCause() == DamageCause.ENTITY_ATTACK) {
                Talisman.checkFor(e, SlimefunItems.TALISMAN_KNIGHT);
                Talisman.checkFor(e, SlimefunItems.TALISMAN_WARRIOR);
            }

            if (e.getCause() == DamageCause.PROJECTILE && ((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) ((EntityDamageByEntityEvent) e).getDamager();

                if (Talisman.checkFor(e, SlimefunItems.TALISMAN_WHIRLWIND)) {
                    Player p = (Player) e.getEntity();
                    Vector direction = p.getEyeLocation().getDirection().multiply(2.0);
                    Location loc = p.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ());
                    Projectile clone = (Projectile) e.getEntity().getWorld().spawnEntity(loc, projectile.getType());
                    clone.setShooter(projectile.getShooter());
                    clone.setVelocity(direction);
                    projectile.remove();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent e) {
        if (e.getDrops().isEmpty() || e.getEntity().getKiller() == null) {
            return;
        }

        LivingEntity entity = e.getEntity();

        if (!(entity instanceof Player) && !(entity instanceof ArmorStand) && Talisman.checkFor(e, SlimefunItems.TALISMAN_HUNTER)) {
            Collection<ItemStack> extraDrops = getExtraDrops(e.getEntity(), e.getDrops());

            for (ItemStack drop : extraDrops) {
                if (drop != null) {
                    e.getDrops().add(drop.clone());
                }
            }
        }
    }

    private Collection<ItemStack> getExtraDrops(LivingEntity entity, Collection<ItemStack> drops) {
        List<ItemStack> items = new ArrayList<>(drops);

        // Prevent duplication of items stored inside a Horse's chest
        if (entity instanceof ChestedHorse) {
            ChestedHorse horse = (ChestedHorse) entity;

            if (horse.isCarryingChest()) {
                // The chest is not included in getStorageContents()
                items.remove(new ItemStack(Material.CHEST));

                for (ItemStack item : horse.getInventory().getStorageContents()) {
                    items.remove(item);
                }
            }
        }

        // Prevent duplication of handheld items or armor
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            for (ItemStack item : equipment.getArmorContents()) {
                items.remove(item);
            }

            items.remove(equipment.getItemInMainHand());
            items.remove(equipment.getItemInOffHand());
        }

        return items;
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent e) {
        if (Talisman.checkFor(e, SlimefunItems.TALISMAN_ANVIL)) {
            PlayerInventory inv = e.getPlayer().getInventory();
            int slot = inv.getHeldItemSlot();

            // Did the tool in our hand broke or was it an Armorpiece?
            if (!inv.getItem(inv.getHeldItemSlot()).equals(e.getBrokenItem())) {
                for (int s : armorSlots) {
                    if (inv.getItem(s).equals(e.getBrokenItem())) {
                        slot = s;
                        break;
                    }
                }
            }

            ItemStack item = e.getBrokenItem().clone();
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(0);
            }

            item.setItemMeta(meta);

            int itemSlot = slot;

            // Update the item forcefully
            Slimefun.runSync(() -> inv.setItem(itemSlot, item), 1L);
        }
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent e) {
        if (e.isSprinting()) {
            Talisman.checkFor(e, SlimefunItems.TALISMAN_TRAVELLER);
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        Random random = ThreadLocalRandom.current();

        if (Talisman.checkFor(e, SlimefunItems.TALISMAN_MAGICIAN)) {
            MagicianTalisman talisman = (MagicianTalisman) SlimefunItems.TALISMAN_MAGICIAN.getItem();
            TalismanEnchantment enchantment = talisman.getRandomEnchantment();
            e.getEnchantsToAdd().put(enchantment.getEnchantment(), enchantment.getLevel());
        }

        if (!e.getEnchantsToAdd().containsKey(Enchantment.SILK_TOUCH) && Enchantment.LOOT_BONUS_BLOCKS.canEnchantItem(e.getItem()) && Talisman.checkFor(e, SlimefunItems.TALISMAN_WIZARD)) {
            Set<Enchantment> enchantments = e.getEnchantsToAdd().keySet();

            for (Enchantment en : enchantments) {
                if (random.nextInt(100) < 40) {
                    e.getEnchantsToAdd().put(en, random.nextInt(3) + 1);
                }
            }

            e.getEnchantsToAdd().put(Enchantment.LOOT_BONUS_BLOCKS, random.nextInt(3) + 3);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

        if (item.getType() != Material.AIR && item.getAmount() > 0) {
            List<ItemStack> drops = new ArrayList<>(e.getBlock().getDrops(item));
            int dropAmount = 1;

            if (item.getEnchantments().containsKey(Enchantment.LOOT_BONUS_BLOCKS) && !item.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                Random random = ThreadLocalRandom.current();
                dropAmount = random.nextInt(item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 2) - 1;
                dropAmount = Math.max(dropAmount, 1);
                dropAmount = (e.getBlock().getType() == Material.LAPIS_ORE ? 4 + random.nextInt(5) : 1) * (dropAmount + 1);
            }

            if (!item.getEnchantments().containsKey(Enchantment.SILK_TOUCH) && MaterialCollections.getAllOres().contains(e.getBlock().getType()) && Talisman.checkFor(e, SlimefunItems.TALISMAN_MINER)) {
                for (ItemStack drop : drops) {
                    if (!drop.getType().isBlock()) {
                        int amount = Math.max(1, (dropAmount * 2) - drop.getAmount());
                        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new CustomItem(drop, amount));
                    }
                }
            }
        }
    }
}
