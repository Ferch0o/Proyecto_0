
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public final class CorruptedGhast extends CustomMob {

    public CorruptedGhast(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_GHAST);
    }

    /*
     * ========================================
     * CONFIGURAR GHAST CORRUPTO
     * ========================================
     */
    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Ghast ghast)) {
            return;
        }

        /*
         * NOMBRE
         */
        ghast.customName(
                Component.text(
                        "Ghast Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        ghast.setCustomNameVisible(true);

        /*
         * Conservamos la vida vanilla.
         *
         * Las habilidades de explosión
         * y eliminación de Fuerza
         * se implementan por separado.
         */
    }
}