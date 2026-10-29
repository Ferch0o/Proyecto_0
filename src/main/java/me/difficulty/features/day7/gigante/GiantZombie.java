
package me.difficulty.features.day7.giant;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class GiantZombie extends CustomMob {

    public GiantZombie(JavaPlugin plugin) {
        super(
                plugin,
                MobIds.GIANT_ZOMBIE
        );
    }

    @Override
    protected void configure(LivingEntity entity) {

        // Comprobar que realmente es un Gigante
        if (!(entity instanceof Giant giant)) {
            return;
        }

        // Nombre personalizado
        giant.customName(
                Component.text(
                        "Zombie Gigante",
                        NamedTextColor.DARK_GREEN
                )
        );

        giant.setCustomNameVisible(true);

        // Lentitud II permanente
        giant.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false
                )
        );
    }
}