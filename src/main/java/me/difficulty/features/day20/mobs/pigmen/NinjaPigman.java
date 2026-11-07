
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NinjaPigman extends CustomMob {

    /*
     * Invisibilidad: 2 minutos.
     * 120 segundos x 20 ticks.
     */
    private static final int INVISIBILITY_DURATION = 2400;

    public NinjaPigman(JavaPlugin plugin) {
        super(plugin, MobIds.NINJA_PIGMAN);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof PigZombie pigman)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        pigman.customName(
                Component.text(
                        "Ninja",
                        NamedTextColor.DARK_PURPLE
                )
        );

        /*
         * Ocultar el nombre flotante
         * para no revelar su posición
         * durante la invisibilidad.
         */
        pigman.setCustomNameVisible(false);

        /*
         * ========================================
         * INVISIBILIDAD - 2 MINUTOS
         * ========================================
         */
        pigman.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        INVISIBILITY_DURATION,
                        0,
                        false,
                        false,
                        true
                )
        );

        /*
         * ========================================
         * VELOCIDAD II PERMANENTE
         * ========================================
         */
        pigman.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false,
                        true
                )
        );

        /*
         * ========================================
         * HACHA DE NETHERITA
         * ========================================
         */
        EntityEquipment equipment = pigman.getEquipment();

        if (equipment == null) {
            return;
        }

        equipment.setItemInMainHand(
                new ItemStack(Material.NETHERITE_AXE)
        );

        equipment.setItemInMainHandDropChance(0.0f);

        /*
         * La inmunidad al daño por caída
         * se implementa en el listener
         * de combate.
         */
    }
}
