
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalMeat;

import org.bukkit.Material;
import org.bukkit.entity.PigZombie;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class PigmanDropListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final ElementalMeat elementalMeat;

    public PigmanDropListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            ElementalMeat elementalMeat
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.elementalMeat = elementalMeat;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof PigZombie pigman)) {
            return;
        }

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        /*
         * ========================================
         * PIGMAN CORRUPTO
         * ========================================
         */
        if (registry.is(
                pigman,
                MobIds.CORRUPTED_PIGMAN
        )) {

            // Eliminar todos los drops vanilla.
            event.getDrops().clear();

            /*
             * 80% de realizar el sorteo.
             *
             * Cantidad posible: 0 a 4.
             *
             * Conservamos provisionalmente
             * BLOQUES DE ORO del TXT anterior.
             */
            if (random.nextInt(100) < 80) {

                int amount = random.nextInt(0, 5);

                if (amount > 0) {
                    event.getDrops().add(
                            new ItemStack(
                                    Material.GOLD_BLOCK,
                                    amount
                            )
                    );
                }
            }

            return;
        }

        /*
         * ========================================
         * NINJA
         * ========================================
         */
        if (!registry.is(
                pigman,
                MobIds.NINJA_PIGMAN
        )) {
            return;
        }

        // Sin drops vanilla.
        event.getDrops().clear();

        /*
         * ========================================
         * CARNE ELEMENTAL: 0 A 2
         * ========================================
         */
        int meatAmount = random.nextInt(0, 3);

        if (meatAmount > 0) {

            ItemStack meat = elementalMeat.createItem();
            meat.setAmount(meatAmount);

            event.getDrops().add(meat);
        }

        /*
         * ========================================
         * NETHERITE SCRAP - 5%
         * ========================================
         */
        if (random.nextInt(10_000) < 500) {

            event.getDrops().add(
                    new ItemStack(
                            Material.NETHERITE_SCRAP
                    )
            );
        }

        /*
         * ========================================
         * LINGOTE DE NETHERITA - 0,5%
         * ========================================
         */
        if (random.nextInt(10_000) < 50) {

            event.getDrops().add(
                    new ItemStack(
                            Material.NETHERITE_INGOT
                    )
            );
        }

        /*
         * ========================================
         * BLOQUE DE NETHERITA - 0,01%
         * ========================================
         */
        if (random.nextInt(10_000) < 1) {

            event.getDrops().add(
                    new ItemStack(
                            Material.NETHERITE_BLOCK
                    )
            );
        }

        /*
         * Las tres probabilidades de
         * netherita son independientes.
         *
         * No modificamos la experiencia.
         */
    }
}
