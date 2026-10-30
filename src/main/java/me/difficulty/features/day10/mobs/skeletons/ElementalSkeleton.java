
package me.difficulty.features.day10.mobs.skeletons;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ElementalSkeleton extends CustomMob {

    public ElementalSkeleton(JavaPlugin plugin) {
        super(plugin, MobIds.ELEMENTAL_SKELETON);
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Skeleton skeleton)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        skeleton.customName(
                Component.text(
                        "Esqueleto Elemental",
                        NamedTextColor.AQUA
                )
        );

        skeleton.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 40 PUNTOS
         * 20 CORAZONES
         * ========================================
         */

        AttributeInstance maxHealth =
                skeleton.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(40.0);
            skeleton.setHealth(40.0);
        }

        /*
         * ========================================
         * EQUIPAMIENTO
         * ========================================
         */

        EntityEquipment equipment =
                skeleton.getEquipment();

        if (equipment == null) {
            return;
        }

        /*
         * ========================================
         * ARCO CON FUEGO X
         * SIN RETROCESO
         * ========================================
         */

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();

        if (bowMeta != null) {

            bowMeta.addEnchant(
                    Enchantment.FLAME,
                    30,
                    true
            );

            bow.setItemMeta(bowMeta);
        }

        equipment.setItemInMainHand(bow);
        equipment.setItemInMainHandDropChance(0.0f);

        /*
         * ========================================
         * ARMADURA DE HIERRO
         * PROTECCIÓN II
         * ========================================
         */

        equipment.setHelmet(
                createArmor(Material.IRON_HELMET)
        );

        equipment.setChestplate(
                createArmor(Material.IRON_CHESTPLATE)
        );

        equipment.setLeggings(
                createArmor(Material.IRON_LEGGINGS)
        );

        equipment.setBoots(
                createArmor(Material.IRON_BOOTS)
        );

        /*
         * EVITAR DROPS DE EQUIPAMIENTO
         */

        equipment.setHelmetDropChance(0.0f);
        equipment.setChestplateDropChance(0.0f);
        equipment.setLeggingsDropChance(0.0f);
        equipment.setBootsDropChance(0.0f);
    }

    /*
     * ========================================
     * CREAR ARMADURA
     * PROTECCIÓN II
     * ========================================
     */

    private ItemStack createArmor(Material material) {

        ItemStack armor = new ItemStack(material);
        ItemMeta meta = armor.getItemMeta();

        if (meta != null) {

            meta.addEnchant(
                    Enchantment.PROTECTION,
                    2,
                    true
            );

            armor.setItemMeta(meta);
        }

        return armor;
    }
}
