
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;

import org.bukkit.plugin.java.JavaPlugin;

public final class PhantomBlaze extends CustomMob {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    private static final double MAX_HEALTH = 80.0;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */

    public PhantomBlaze(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.PHANTOM_BLAZE
        );
    }

    /*
     * ========================================
     * CONFIGURAR BLAZE FANTASMAL
     * ========================================
     */

    @Override
    protected void configure(
            LivingEntity entity
    ) {

        if (!(entity instanceof Blaze blaze)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        blaze.customName(
                Component.text(
                        "Blaze Fantasmal",
                        NamedTextColor.DARK_PURPLE
                )
        );

        blaze.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 80 PUNTOS
         * ========================================
         *
         * 80 puntos = 40 corazones.
         */

        AttributeInstance health =
                blaze.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (health != null) {

            health.setBaseValue(MAX_HEALTH);

            blaze.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * HABILIDAD ESPECIAL
         * ========================================
         *
         * Probabilidad: 15%
         *
         * Lanza una bola de fuego similar
         * a la del Ghast.
         *
         * Efectos independientes:
         *
         * Debilidad III:    40%
         * Ceguera I:        35%
         * Lentitud III:     55%
         * Fatiga Minera II: 20%
         * Hambre III:       60%
         * Veneno V:         30%
         * Oscuridad I:      15%
         *
         * Los efectos podrán combinarse.
         *
         * La habilidad se implementará
         * en el listener de combate.
         */
    }
}
