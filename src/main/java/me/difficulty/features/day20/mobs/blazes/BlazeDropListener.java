
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import me.difficulty.features.day20.items.ElementalRod;

import org.bukkit.entity.Blaze;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class BlazeDropListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;
    private final ElementalRod elementalRod;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public BlazeDropListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            ElementalRod elementalRod
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
        this.elementalRod = elementalRod;
    }

    /*
     * ========================================
     * DROPS DE LOS BLAZES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onBlazeDeath(
            EntityDeathEvent event
    ) {

        /*
         * Solo desde el Día 20.
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo entidades Blaze.
         */
        if (!(event.getEntity()
                instanceof Blaze blaze)) {
            return;
        }

        /*
         * ========================================
         * BLAZE CORRUPTO
         * DROPS DOBLES
         * ========================================
         */
        if (mobRegistry.is(
                blaze,
                MobIds.CORRUPTED_BLAZE
        )) {

            /*
             * Duplicamos las cantidades
             * que Minecraft ya determinó.
             *
             * Esto respeta:
             * - La probabilidad vanilla.
             * - El encantamiento Saqueo.
             * - La cantidad original.
             */
            for (ItemStack drop : event.getDrops()) {

                int originalAmount =
                        drop.getAmount();

                drop.setAmount(
                        originalAmount * 2
                );
            }

            return;
        }

        /*
         * ========================================
         * REY BLAZE
         * VARAS ELEMENTALES
         * ========================================
         */
        if (mobRegistry.is(
                blaze,
                MobIds.BLAZE_KING
        )) {


            /*
             * ========================================
             * REY BLAZE - SOLO VARAS ELEMENTALES
             * ========================================
             *
             * Eliminar absolutamente todos
             * los drops vanilla.
             */
            event.getDrops().clear();

            /*
             * Cantidad aleatoria: 1 a 3.
             */
            int amount = ThreadLocalRandom
                    .current()
                    .nextInt(1, 4);

            ItemStack rods = elementalRod.createItem();
            rods.setAmount(amount);

            event.getDrops().add(rods);
        }
    }
}
