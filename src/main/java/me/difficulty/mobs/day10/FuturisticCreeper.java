package me.difficulty.mobs.day10;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class FuturisticCreeper extends CustomMob {

    public FuturisticCreeper(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.FUTURISTIC_CREEPER
        );
    }

    @Override
    protected void configure(
            LivingEntity entity
    ) {

        if (!(entity instanceof Creeper creeper)) {
            return;
        }


        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        creeper.customName(
                Component.text(
                        "Creeper Futurista",
                        NamedTextColor.AQUA
                )
        );

        creeper.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * EXPLOSIÓN MÁS RÁPIDA
         * ========================================
         *
         * 20 ticks = 1 segundo.
         *
         * 15 ticks = 0.75 segundos.
         */

        creeper.setMaxFuseTicks(
                15
        );

        creeper.setExplosionRadius(
                5
        );
    }
}