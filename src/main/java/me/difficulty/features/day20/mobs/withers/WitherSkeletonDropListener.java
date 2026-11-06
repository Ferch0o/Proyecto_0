
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalHead;

import org.bukkit.entity.WitherSkeleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class WitherSkeletonDropListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final ElementalHead elementalHead;

    public WitherSkeletonDropListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            ElementalHead elementalHead
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.elementalHead = elementalHead;
    }

    /*
     * ========================================
     * DROPS DE WITHER SKELETON
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof WitherSkeleton skeleton)) {
            return;
        }

        /*
         * ========================================
         * WITHER CORRUPTO - DROPS TRIPLES
         * ========================================
         */
        if (registry.is(
                skeleton,
                MobIds.CORRUPTED_WITHER_SKELETON
        )) {

            List<ItemStack> amplified = new ArrayList<>();

            for (ItemStack original : event.getDrops()) {

                int total = original.getAmount() * 3;

                int limit = Math.max(
                        1,
                        original.getMaxStackSize()
                );

                while (total > 0) {

                    ItemStack part = original.clone();

                    int count = Math.min(
                            limit,
                            total
                    );

                    part.setAmount(count);
                    amplified.add(part);

                    total -= count;
                }
            }

            event.getDrops().clear();
            event.getDrops().addAll(amplified);

            return;
        }

        /*
         * ========================================
         * GUARDIÁN DE LA FORTALEZA
         * CABEZA ELEMENTAL - 50%
         * ========================================
         */
        if (registry.is(
                skeleton,
                MobIds.FORTRESS_GUARDIAN
        )) {

            int roll = ThreadLocalRandom
                    .current()
                    .nextInt(100);

            if (roll < 50) {

                event.getDrops().add(
                        elementalHead.createItem()
                );
            }
        }
    }
}
