
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public final class CorruptedEnderman extends CustomMob {

    private static final double MAX_HEALTH = 60.0;

    public CorruptedEnderman(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_ENDERMAN);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Enderman enderman)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        enderman.customName(
                Component.text(
                        "Enderman Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        enderman.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 60 HP = 30 CORAZONES
         * ========================================
         */
        AttributeInstance health = enderman.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            enderman.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * COMPORTAMIENTO
         * ========================================
         *
         * Conservamos:
         *
         * - Ataque cuerpo a cuerpo vanilla.
         * - Teletransportación.
         * - Agresividad al mirarlo.
         * - Agresividad al atacarlo.
         * - Capacidad de transportar bloques.
         *
         * La Velocidad I se gestionará
         * mediante una tarea independiente
         * cuando tenga un objetivo.
         */
    }
}
