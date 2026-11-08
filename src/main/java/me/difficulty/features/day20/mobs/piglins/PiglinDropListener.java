
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalGold;

import org.bukkit.Material;
import org.bukkit.entity.PiglinAbstract;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.ThreadLocalRandom;

public final class PiglinDropListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final ElementalGold elementalGold;

    public PiglinDropListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            ElementalGold elementalGold
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.elementalGold = elementalGold;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Funciona con Piglin y PiglinBrute.
         */
        if (!(event.getEntity()
                instanceof PiglinAbstract piglin)) {
            return;
        }

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        /*
         * ========================================
         * REY PIGLIN
         * ========================================
         */
        if (registry.is(
                piglin,
                MobIds.KING_PIGLIN
        )) {

            event.getDrops().clear();

            /*
             * 1 a 3 Oros Elementales.
             */
            int amount = random.nextInt(1, 4);

            ItemStack gold = elementalGold.createItem();
            gold.setAmount(amount);

            event.getDrops().add(gold);
            return;
        }

        /*
         * ========================================
         * PIGLIN CORRUPTO
         * ========================================
         */
        if (registry.is(
                piglin,
                MobIds.CORRUPTED_PIGLIN
        )) {

            event.getDrops().clear();

            event.getDrops().add(
                    new ItemStack(
                            Material.GOLD_BLOCK,
                            1
                    )
            );

            return;
        }

        /*
         * ========================================
         * PIGLIN EXPLOSIVO
         * ========================================
         */
        if (!registry.is(
                piglin,
                MobIds.EXPLOSIVE_PIGLIN
        )) {
            return;
        }

        event.getDrops().clear();

        /*
         * 1 a 3 cohetes de vuelo 3.
         */
        int amount = random.nextInt(1, 4);

        ItemStack rockets = new ItemStack(
                Material.FIREWORK_ROCKET,
                amount
        );

        if (rockets.getItemMeta()
                instanceof FireworkMeta meta) {

            meta.setPower(3);
            rockets.setItemMeta(meta);
        }

        event.getDrops().add(rockets);
    }
}