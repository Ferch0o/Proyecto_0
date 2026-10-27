
package me.difficulty.features.day10.mobs.spiders;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class SpeedSpider extends CustomMob {

    public SpeedSpider(JavaPlugin plugin) {
        super(plugin, MobIds.SPEED_SPIDER);
    }

    @Override
    protected void configure(LivingEntity entity) {

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

        spider.setCustomNameVisible(true);

        /*
         * ========================================
         * VELOCIDAD IV
         * ========================================
         *
         * Amplificador 3 = nivel IV.
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        3,
                        false,
                        false
                )
        );

        /*
         * ========================================
         * INVISIBILIDAD PERMANENTE
         * ========================================
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

        /*
         * ========================================
         * 50% DE PROBABILIDAD DE GLOWING
         * ========================================
         *
         * Se mantiene invisible, pero
         * se podrá ver su contorno.
         */

        if (ThreadLocalRandom.current()
                .nextInt(100) < 50) {

            spider.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.GLOWING,
                            PotionEffect.INFINITE_DURATION,
                            0,
                            false,
                            false
                    )
            );
        }
    }
}