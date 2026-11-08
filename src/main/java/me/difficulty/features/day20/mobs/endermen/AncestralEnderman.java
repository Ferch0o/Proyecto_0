
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class AncestralEnderman extends CustomMob {

    private static final double MAX_HEALTH = 100.0;

    public AncestralEnderman(JavaPlugin plugin) {
        super(plugin, MobIds.ANCESTRAL_ENDERMAN);
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
                        "Enderman Ancestral",
                        NamedTextColor.DARK_PURPLE
                )
        );

        enderman.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 100 HP
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
         * LENTITUD I PERMANENTE
         * ========================================
         */
        enderman.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false,
                        true
                )
        );

        /*
         * Conservamos toda la IA vanilla:
         * enfado, ataques y teletransporte.
         *
         * La Levitación III se aplicará
         * desde EndermanCombatListener.
         */
    }
}