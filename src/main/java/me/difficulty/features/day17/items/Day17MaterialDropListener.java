package me.difficulty.features.day17.items;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** El reemplazo de drops se ejecuta después de los listeners anteriores (NORMAL). */
public final class Day17MaterialDropListener implements Listener {
    private final DayManager dayManager;
    private final CustomMobRegistry mobs;
    private final Day17Materials items;

    public Day17MaterialDropListener(DayManager dayManager, CustomMobRegistry mobs, Day17Materials items) {
        this.dayManager = dayManager;
        this.mobs = mobs;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        if (!dayManager.isAtLeast(17)) return;
        LivingEntity dead = event.getEntity();

        if (dead instanceof Zombie && mobs.is(dead, MobIds.SCAVENGER)) {
            replace(event, Material.ROTTEN_FLESH, items.driedMeat(), 2, 4);
        } else if (dead instanceof Skeleton && mobs.is(dead, MobIds.GLADIATOR)) {
            replace(event, Material.BONE, items.guardianBone(), 4, 8);
        } else if (dead instanceof Creeper && mobs.is(dead, MobIds.FUTURISTIC_CREEPER)) {
            replace(event, Material.GUNPOWDER, items.magicGunpowder(), 2, 4);
        } else if (dead instanceof Spider && mobs.is(dead, MobIds.GOLIATH)) {
            replace(event, Material.FERMENTED_SPIDER_EYE, items.goliathEye(), 6, 9);
        } else if (dead instanceof IronGolem) {
            // Mantener probabilidad preexistente del día 10 (70%):
            // reemplazar SOLO los bloques de hierro realmente generados.
            int blockAmount = event.getDrops().stream()
                    .filter(s -> s.getType() == Material.IRON_BLOCK)
                    .mapToInt(ItemStack::getAmount).sum();
            if (blockAmount > 0) {
                event.getDrops().removeIf(s -> s.getType() == Material.IRON_BLOCK);
                ItemStack titanium = items.titaniumBlock().createItem();
                titanium.setAmount(blockAmount);
                event.getDrops().add(titanium);
            }
        } else if (dead instanceof Warden) {
            event.getDrops().add(items.wardenHeart().createItem());
        }
    }

    private void replace(EntityDeathEvent event, Material vanilla,
                         Day17Material custom, int min, int max) {
        event.getDrops().removeIf(s -> s.getType() == vanilla);
        ItemStack stack = custom.createItem();
        stack.setAmount(ThreadLocalRandom.current().nextInt(min, max + 1));
        event.getDrops().add(stack);
    }
}
