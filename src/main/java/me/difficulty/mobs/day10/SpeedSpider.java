package me.difficulty.mobs.day10;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedSpider extends CustomMob {

    public SpeedSpider(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.SPEED_SPIDER
        );
    }

    @Override
    protected void configure(
            LivingEntity entity
    ) {

        if (!(entity instanceof Spider spider)) {
            return;
        }


        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        spider.customName(
                Component.text(
                        "Araña Velocista",
                        NamedTextColor.YELLOW
                )
        );

        spider.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * VELOCIDAD III
         * ========================================
         *
         * amplifier:
         *
         * 0 = Velocidad I
         * 1 = Velocidad II
         * 2 = Velocidad III
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        2,
                        false,
                        false
                )
        );

        /*
         * ========================================
         * INVISIBILIDAD PERMANENTE
         * ========================================
         *
         * Todas las Arañas Velocistas
         * serán invisibles.
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );
    }
}