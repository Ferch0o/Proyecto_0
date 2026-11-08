
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day20.items.ElementalHead;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Wolf;

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

    /*
     * ========================================
     * PROBABILIDAD VANILLA
     * ========================================
     *
     * 2,5% base.
     * +1% por nivel de Saqueo.
     *
     * Wither Corrupto: probabilidad x3.
     */
    private static final double BASE_SKULL_CHANCE = 0.025;

    private static final double LOOTING_SKULL_BONUS = 0.01;

    private static final double SKULL_CHANCE_MULTIPLIER = 3.0;

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
     * DROPS DE WITHER SKELETONS
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
         * WITHER CORRUPTO
         * ========================================
         */
        if (registry.is(
                skeleton,
                MobIds.CORRUPTED_WITHER_SKELETON
        )) {

            handleCorruptedDrops(event, skeleton);
            return;
        }

        /*
         * ========================================
         * GUARDIÁN DE LA FORTALEZA
         * ========================================
         */
        if (registry.is(
                skeleton,
                MobIds.FORTRESS_GUARDIAN
        )) {

            /*
             * Sin drops vanilla.
             */
            event.getDrops().clear();

            /*
             * 50% de obtener exactamente
             * una Cabeza Elemental.
             */
            if (ThreadLocalRandom.current()
                    .nextInt(100) < 50) {

                event.getDrops().add(
                        elementalHead.createItem()
                );
            }
        }
    }

    /*
     * ========================================
     * DROPS DEL WITHER CORRUPTO
     * ========================================
     */
    private void handleCorruptedDrops(
            EntityDeathEvent event,
            WitherSkeleton skeleton
    ) {

        List<ItemStack> amplified = new ArrayList<>();

        /*
         * Guardar la cabeza vanilla,
         * si Minecraft la generó.
         */
        ItemStack vanillaSkull = null;

        for (ItemStack original : event.getDrops()) {

            if (original.getType()
                    == Material.WITHER_SKELETON_SKULL) {

                /*
                 * Una sola cabeza como máximo.
                 */
                if (vanillaSkull == null) {

                    vanillaSkull = original.clone();
                    vanillaSkull.setAmount(1);
                }

                continue;
            }

            /*
             * ========================================
             * RESTO DE DROPS: x3
             * ========================================
             */
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

        /*
         * ========================================
         * CABEZA DE WITHER SKELETON
         * ========================================
         *
         * Si vanilla ya entregó una cabeza,
         * conservar exactamente una.
         *
         * Esto también respeta el drop
         * garantizado por creeper cargado.
         */
        if (vanillaSkull != null) {

            amplified.add(vanillaSkull);

        } else if (shouldAddExtraSkull(
                event,
                skeleton
        )) {

            amplified.add(
                    new ItemStack(
                            Material.WITHER_SKELETON_SKULL,
                            1
                    )
            );
        }

        /*
         * Reemplazar drops originales.
         */
        event.getDrops().clear();
        event.getDrops().addAll(amplified);
    }

    /*
     * ========================================
     * AUMENTAR PROBABILIDAD DE CABEZA
     * ========================================
     *
     * Vanilla ya realizó un sorteo.
     *
     * Si ese sorteo falló, calculamos
     * una probabilidad complementaria
     * para que la probabilidad total
     * sea exactamente el triple.
     */
    private boolean shouldAddExtraSkull(
            EntityDeathEvent event,
            WitherSkeleton skeleton
    ) {

        Player killer = skeleton.getKiller();

        int lootingLevel = 0;

        /*
         * Cabeza aleatoria vanilla:
         * solo corresponde a muertes
         * atribuidas a jugador o lobo
         * domesticado.
         */
        if (killer != null) {

            ItemStack weapon = killer
                    .getInventory()
                    .getItemInMainHand();

            lootingLevel = weapon.getEnchantmentLevel(
                    Enchantment.LOOTING
            );

        } else {

            Entity causing = event
                    .getDamageSource()
                    .getCausingEntity();

            if (!(causing instanceof Wolf wolf)
                    || !wolf.isTamed()) {
                return false;
            }
        }

        /*
         * Probabilidad base de Minecraft.
         */
        double vanillaChance =
                BASE_SKULL_CHANCE
                        + lootingLevel
                        * LOOTING_SKULL_BONUS;

        vanillaChance = Math.min(
                1.0,
                vanillaChance
        );

        /*
         * Objetivo: triple probabilidad,
         * sin superar el 100%.
         */
        double targetChance = Math.min(
                1.0,
                vanillaChance * SKULL_CHANCE_MULTIPLIER
        );

        /*
         * Evitar división por cero.
         */
        if (vanillaChance >= 1.0) {
            return false;
        }

        /*
         * Si el primer sorteo vanilla
         * falló, el segundo debe compensar
         * exactamente la diferencia.
         */
        double extraChance =
                (targetChance - vanillaChance)
                        / (1.0 - vanillaChance);

        return ThreadLocalRandom.current()
                .nextDouble() < extraChance;
    }
}