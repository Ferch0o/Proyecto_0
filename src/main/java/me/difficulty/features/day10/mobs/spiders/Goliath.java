
package me.difficulty.features.day10.mobs.spiders;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Goliath extends CustomMob {

    public Goliath(JavaPlugin plugin) {
        super(plugin, MobIds.GOLIATH);
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
                        "Goliat",
                        NamedTextColor.DARK_GREEN
                )
        );

        spider.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 160 PUNTOS
         * 80 CORAZONES
         * ========================================
         */

        AttributeInstance maxHealth =
                spider.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {

            maxHealth.setBaseValue(160.0);
            spider.setHealth(160.0);
        }

        /*
         * ========================================
         * FUERZA III
         * ========================================
         *
         * Amplificador 2.
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.STRENGTH,
                        PotionEffect.INFINITE_DURATION,
                        2,
                        false,
                        false
                )
        );

        /*
         * ========================================
         * RESISTENCIA I
         * ========================================
         *
         * Amplificador 0.
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );

        /*
         * ========================================
         * VELOCIDAD I
         * ========================================
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );
    }
}