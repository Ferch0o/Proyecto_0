package me.difficulty.features.day10.mobs.golems;

import me.difficulty.core.day.DayManager;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class IronGolemListener implements Listener {

    private final DayManager dayManager;


    public IronGolemListener(
            DayManager dayManager
    ) {

        this.dayManager = dayManager;
    }


    /*
     * ========================================
     * VELOCIDAD II
     * ========================================
     *
     * Todo Golem de Hierro que aparezca
     * desde el Día 10 tendrá Velocidad II.
     */

    @EventHandler
    public void onIronGolemSpawn(
            CreatureSpawnEvent event
    ) {

        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Solo Golems de Hierro.
         */
        if (!(event.getEntity()
                instanceof IronGolem golem)) {

            return;
        }


        /*
         * ========================================
         * VELOCIDAD II
         * ========================================
         *
         * amplifier:
         *
         * 0 = Velocidad I
         * 1 = Velocidad II
         */

        golem.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false
                )
        );
    }


    /*
     * ========================================
     * LENTITUD I AL GOLPEAR
     * ========================================
     *
     * El Golem aplica Lentitud I durante
     * 30 segundos a cualquier LivingEntity
     * que consiga golpear.
     *
     * No solamente jugadores.
     */

    @EventHandler(ignoreCancelled = true)
    public void onIronGolemHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * El atacante debe ser
         * un Golem de Hierro.
         */
        if (!(event.getDamager()
                instanceof IronGolem)) {

            return;
        }


        /*
         * La víctima debe ser
         * una entidad viva.
         *
         * Ejemplos:
         *
         * - Player
         * - Zombie
         * - Skeleton
         * - Spider
         * - Ravager
         * - etc.
         */
        if (!(event.getEntity()
                instanceof LivingEntity target)) {

            return;
        }


        /*
         * ========================================
         * LENTITUD I
         *
         * 30 segundos
         * 100% de probabilidad
         * ========================================
         *
         * Minecraft utiliza ticks:
         *
         * 20 ticks = 1 segundo
         *
         * 30 * 20 = 600 ticks
         */

        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        30 * 20,
                        0,
                        false,
                        true
                )
        );
    }


    /*
     * ========================================
     * DROP ESPECIAL
     * ========================================
     *
     * 40% de probabilidad de añadir
     * 1 Bloque de Hierro.
     *
     * Los drops vanilla NO se eliminan.
     */

    @EventHandler
    public void onIronGolemDeath(
            EntityDeathEvent event
    ) {

        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Solo Golems de Hierro.
         */
        if (!(event.getEntity()
                instanceof IronGolem)) {

            return;
        }


        /*
         * ========================================
         * 40% DE PROBABILIDAD
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll < 40) {

            event.getDrops().add(
                    new ItemStack(
                            Material.IRON_BLOCK,
                            1
                    )
            );
        }
    }
}