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

public class Goliath extends CustomMob {

    public Goliath(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.GOLIATH
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
                        "Goliat",
                        NamedTextColor.DARK_GREEN
                )
        );

        spider.setCustomNameVisible(
                true
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
                        4,
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