
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalTear;

import org.bukkit.entity.Ghast;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class GhastDropListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final ElementalTear elementalTear;

    public GhastDropListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            ElementalTear elementalTear
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.elementalTear = elementalTear;
    }

    /*
     * ========================================
     * DROPS DE GHASTS - DÍA 20
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity() instanceof Ghast ghast)) {
            return;
        }

        /*
         * ========================================
         * GHAST CORRUPTO
         * DROPS TRIPLES
         * ========================================
         */
        if (registry.is(
                ghast,
                MobIds.CORRUPTED_GHAST
        )) {

            List<ItemStack> tripled = new ArrayList<>();

            for (ItemStack original : event.getDrops()) {

                int total = original.getAmount() * 3;

                int maxStack = Math.max(
                        1,
                        original.getMaxStackSize()
                );

                while (total > 0) {

                    ItemStack part = original.clone();

                    int amount = Math.min(
                            total,
                            maxStack
                    );

                    part.setAmount(amount);
                    tripled.add(part);

                    total -= amount;
                }
            }

            event.getDrops().clear();
            event.getDrops().addAll(tripled);

            return;
        }

        if (registry.is(
                ghast,
                MobIds.DEMONIC_GHAST
        )) {

            /*
             * Eliminar todos los drops:
             * - Lágrimas vanilla.
             * - Pólvora.
             * - Otros objetos.
             */
            event.getDrops().clear();

            /*
             * Entre 3 y 6 Lágrimas Elementales.
             */
            int amount = ThreadLocalRandom
                    .current()
                    .nextInt(3, 7);

            ItemStack tears = elementalTear.createItem();
            tears.setAmount(amount);

            event.getDrops().add(tears);

            return;
        }
    }
}
