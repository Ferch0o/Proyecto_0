package me.difficulty.mobs.day10;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AbyssSpider extends CustomMob {

    public AbyssSpider(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.ABYSS_SPIDER
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
                        "Araña del Abismo",
                        NamedTextColor.DARK_PURPLE
                )
        );

        spider.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * VIDA
         * ========================================
         *
         * 30 puntos de vida
         * = 15 corazones.
         */

        AttributeInstance maxHealth =
                spider.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (maxHealth != null) {

            maxHealth.setBaseValue(
                    60.0
            );

            spider.setHealth(
                    60.0
            );
        }


        /*
         * ========================================
         * FUERZA III
         * ========================================
         *
         * 0 = I
         * 1 = II
         * 2 = III
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.STRENGTH,
                        PotionEffect.INFINITE_DURATION,
                        3,
                        false,
                        false
                )
        );


        /*
         * ========================================
         * LENTITUD I
         * ========================================
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );

        /*
         * ========================================
         * RESISTENCIA V
         * ========================================
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        PotionEffect.INFINITE_DURATION,
                        2,
                        false,
                        false
                )
        );
    }
}