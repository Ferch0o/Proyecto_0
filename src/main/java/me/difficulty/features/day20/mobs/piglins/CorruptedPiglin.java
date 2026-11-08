
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CorruptedPiglin extends CustomMob {

    public CorruptedPiglin(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_PIGLIN);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Piglin piglin)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        piglin.customName(
                Component.text(
                        "Piglin Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        piglin.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA
         * ========================================
         *
         * Conservar la vida vanilla.
         *
         * Evitar la zombificación para
         * conservar la identidad del mob
         * si llega al Overworld.
         */
        piglin.setImmuneToZombification(true);

        /*
         * ========================================
         * FUERZA II PERMANENTE
         * ========================================
         */
        piglin.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.STRENGTH,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false,
                        true
                )
        );

        /*
         * ========================================
         * LENTITUD I PERMANENTE
         * ========================================
         */
        piglin.addPotionEffect(
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
         * ========================================
         * HACHA DE ORO - FILO V
         * ========================================
         */
        EntityEquipment equipment =
                piglin.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack axe = new ItemStack(
                Material.GOLDEN_AXE
        );

        axe.addUnsafeEnchantment(
                Enchantment.SHARPNESS,
                5
        );

        equipment.setItemInMainHand(axe);

        /*
         * La herramienta no debe caer.
         * El listener también elimina
         * los drops vanilla.
         */
        equipment.setItemInMainHandDropChance(0.0f);
    }
}