
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

public class AbyssSpider extends CustomMob {

    public AbyssSpider(JavaPlugin plugin) {
        super(plugin, MobIds.ABYSS_SPIDER);
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
                        "Araña del Abismo",
                        NamedTextColor.DARK_PURPLE
                )
        );

        spider.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 70 PUNTOS
         * 35 CORAZONES
         * ========================================
         */

        AttributeInstance maxHealth =
                spider.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {

            maxHealth.setBaseValue(70.0);
            spider.setHealth(70.0);
        }

        /*
         * ========================================
         * FUERZA VI
         * ========================================
         */

        spider.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.STRENGTH,
                        PotionEffect.INFINITE_DURATION,
                        7,
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
         * Sin Resistencia permanente.
         *
         * Durante Tormenta del Día 10
         * recibirá Resistencia I desde
         * el sistema global.
         */
    }
}
