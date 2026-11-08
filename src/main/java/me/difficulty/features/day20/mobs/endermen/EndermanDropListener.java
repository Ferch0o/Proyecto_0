
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalPearl;

import org.bukkit.entity.Enderman;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EndermanDropListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final ElementalPearl elementalPearl;

    public EndermanDropListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            ElementalPearl elementalPearl
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.elementalPearl = elementalPearl;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof Enderman enderman)) {
            return;
        }

        /*
         * ========================================
         * ENDERMAN ANCESTRAL
         * ========================================
         *
         * Sin drops vanilla.
         * 0 a 2 Perlas Elementales.
         */
        if (registry.is(
                enderman,
                MobIds.ANCESTRAL_ENDERMAN
        )) {

            event.getDrops().clear();

            int amount = ThreadLocalRandom.current()
                    .nextInt(0, 3);

            if (amount > 0) {

                ItemStack pearls =
                        elementalPearl.createItem();

                pearls.setAmount(amount);
                event.getDrops().add(pearls);
            }

            return;
        }

        /*
         * ========================================
         * ENDERMAN CORRUPTO
         * ========================================
         *
         * Mantener sus drops dobles.
         */
        if (!registry.is(
                enderman,
                MobIds.CORRUPTED_ENDERMAN
        )) {
            return;
        }

        List<ItemStack> doubled = new ArrayList<>();

        for (ItemStack original : event.getDrops()) {

            int total = original.getAmount() * 2;

            int maxStack = Math.max(
                    1,
                    original.getMaxStackSize()
            );

            while (total > 0) {

                ItemStack part = original.clone();

                int amount = Math.min(total, maxStack);

                part.setAmount(amount);
                doubled.add(part);

                total -= amount;
            }
        }

        event.getDrops().clear();
        event.getDrops().addAll(doubled);
    }
}