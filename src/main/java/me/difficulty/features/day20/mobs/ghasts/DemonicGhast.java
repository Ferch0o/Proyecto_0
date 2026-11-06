
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public final class DemonicGhast extends CustomMob {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */
    private static final double MAX_HEALTH = 80.0;

    public DemonicGhast(JavaPlugin plugin) {
        super(plugin, MobIds.DEMONIC_GHAST);
    }

    /*
     * ========================================
     * CONFIGURAR GHAST DEMONÍACO
     * ========================================
     */
    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Ghast ghast)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        ghast.customName(
                Component.text(
                        "Ghast Demoníaco",
                        NamedTextColor.DARK_PURPLE
                )
        );

        ghast.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 80 HP = 40 CORAZONES
         * ========================================
         */
        AttributeInstance health = ghast.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            ghast.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * RESISTENCIA AL EMPUJE
         * ========================================
         *
         * Valor 1.0 = resistencia máxima
         * mediante este atributo.
         */
        AttributeInstance knockback = ghast.getAttribute(
                Attribute.KNOCKBACK_RESISTANCE
        );

        if (knockback != null) {
            knockback.setBaseValue(1.0);
        }

        /*
         * ========================================
         * EXPLOSIÓN DE EFECTOS
         * ========================================
         *
         * Se implementará mediante
         * DemonicGhastAttackListener.
         */
    }
}