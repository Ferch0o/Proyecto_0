
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;

import org.bukkit.plugin.java.JavaPlugin;

public final class CorruptedBlaze extends CustomMob {

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public CorruptedBlaze(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.CORRUPTED_BLAZE
        );
    }

    /*
     * ========================================
     * CONFIGURACIÓN DEL BLAZE CORRUPTO
     * ========================================
     */
    @Override
    protected void configure(
            LivingEntity entity
    ) {

        /*
         * Comprobar que la entidad
         * realmente sea un Blaze.
         */
        if (!(entity instanceof Blaze blaze)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE PERSONALIZADO
         * ========================================
         */
        blaze.customName(
                Component.text(
                        "Blaze Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        blaze.setCustomNameVisible(true);

        /*
         * ========================================
         * HABILIDADES PENDIENTES
         * ========================================
         *
         * - Eliminar Resistencia al Fuego
         *   cuando ataque al jugador.
         *
         * - Infligir el doble de daño.
         *
         * - Duplicar los drops.
         *
         * Estas habilidades serán
         * implementadas con listeners
         * para controlar tanto ataques
         * directos como proyectiles.
         */
    }
}