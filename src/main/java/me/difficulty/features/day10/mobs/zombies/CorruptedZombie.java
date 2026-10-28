
package me.difficulty.features.day10.mobs.zombies;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CorruptedZombie extends CustomMob {

    public CorruptedZombie(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_ZOMBIE);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Zombie zombie)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        zombie.customName(
                Component.text(
                        "Zombie Corrupto",
                        NamedTextColor.DARK_PURPLE
                )
        );

        zombie.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 50 PUNTOS = 25 CORAZONES
         * ========================================
         */

        AttributeInstance maxHealth =
                zombie.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(50.0);
            zombie.setHealth(50.0);
        }

        /*
         * ========================================
         * LENTITUD I
         * ========================================
         *
         * Ya no tiene Resistencia permanente.
         */

        zombie.addPotionEffect(
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
         * RESISTENCIA AL EMPUJE: 100%
         * ========================================
         */

        AttributeInstance knockbackResistance =
                zombie.getAttribute(
                        Attribute.KNOCKBACK_RESISTANCE
                );

        if (knockbackResistance != null) {
            knockbackResistance.setBaseValue(1.0);
        }

        /*
         * ========================================
         * EQUIPAMIENTO
         * ========================================
         */

        EntityEquipment equipment = zombie.getEquipment();

        if (equipment == null) {
            return;
        }

        /*
         * ========================================
         * NETHERITE COMPLETA
         * ========================================
         */

        equipment.setHelmet(
                createProtectionArmor(
                        Material.NETHERITE_HELMET
                )
        );

        equipment.setChestplate(
                createProtectionArmor(
                        Material.NETHERITE_CHESTPLATE
                )
        );

        equipment.setLeggings(
                createProtectionArmor(
                        Material.NETHERITE_LEGGINGS
                )
        );

        equipment.setBoots(
                createProtectionArmor(
                        Material.NETHERITE_BOOTS
                )
        );

        /*
         * No soltar la armadura.
         */

        equipment.setHelmetDropChance(0.0f);
        equipment.setChestplateDropChance(0.0f);
        equipment.setLeggingsDropChance(0.0f);
        equipment.setBootsDropChance(0.0f);

        /*
         * ========================================
         * ESPADA DE NETHERITE
         * FILO II
         * ========================================
         */

        ItemStack sword =
                new ItemStack(Material.NETHERITE_SWORD);

        ItemMeta swordMeta = sword.getItemMeta();

        if (swordMeta != null) {

            swordMeta.addEnchant(
                    Enchantment.SHARPNESS,
                    2,
                    true
            );

            sword.setItemMeta(swordMeta);
        }

        equipment.setItemInMainHand(sword);
        equipment.setItemInMainHandDropChance(0.0f);
    }

    private ItemStack createProtectionArmor(Material material) {
        return new ItemStack(material);
    }
}
