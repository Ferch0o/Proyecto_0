
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CorruptedWitherSkeleton extends CustomMob {

    private static final double MAX_HEALTH = 150.0;

    public CorruptedWitherSkeleton(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_WITHER_SKELETON);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof WitherSkeleton skeleton)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        skeleton.customName(
                Component.text(
                        "Wither Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        skeleton.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 150 PUNTOS = 75 CORAZONES
         * ========================================
         */
        AttributeInstance health = skeleton.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            skeleton.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * RESISTENCIA I PERMANENTE
         * ========================================
         */
        skeleton.addPotionEffect(
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
         * EQUIPAMIENTO
         * ========================================
         */
        EntityEquipment equipment = skeleton.getEquipment();

        if (equipment == null) {
            return;
        }

        /*
         * ESPADA NETHERITA - FILO L
         */
        ItemStack sword = new ItemStack(
                Material.NETHERITE_SWORD
        );

        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.addEnchant(
                    Enchantment.SHARPNESS,
                    50,
                    true
            );

            sword.setItemMeta(meta);
        }

        equipment.setItemInMainHand(sword);

        // No soltar la espada personalizada.
        equipment.setItemInMainHandDropChance(0.0f);
    }
}