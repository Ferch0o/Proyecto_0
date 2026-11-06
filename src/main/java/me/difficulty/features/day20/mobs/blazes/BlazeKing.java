
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

public final class BlazeKing extends CustomMob {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    private static final double MAX_HEALTH = 50.0;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */

    public BlazeKing(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.BLAZE_KING
        );
    }

    /*
     * ========================================
     * CONFIGURAR REY BLAZE
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
                        "Rey Blaze",
                        NamedTextColor.GOLD
                )
        );

        blaze.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 40 PUNTOS
         * ========================================
         *
         * 40 puntos = 20 corazones.
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
         * INMUNIDAD A FLECHAS
         * ========================================
         *
         * Las flechas no podrán dañarlo.
         *
         * Se implementará mediante
         * un listener de daño.
         */

        /*
         * ========================================
         * ATAQUE ESPECIAL
         * ========================================
         *
         * Cada tercer ataque:
         *
         * Dispara bolas de fuego
         * formando un arco de 90 grados.
         *
         * El contador de ataques
         * será individual para cada Rey Blaze.
         */

        /*
         * ========================================
         * DROP ESPECIAL
         * ========================================
         *
         * Varas Elementales:
         * entre 3 y 5 unidades.
         *
         * El drop se implementará
         * mediante BlazeDropListener.
         */
    }
}
