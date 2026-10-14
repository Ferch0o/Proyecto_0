package me.difficulty.mobs.day10;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ElementalSkeleton extends CustomMob {

    public ElementalSkeleton(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.ELEMENTAL_SKELETON
        );
    }

    @Override
    protected void configure(
            LivingEntity entity
    ) {

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

        skeleton.setCustomNameVisible(
                true
        );


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
         * ARCO
         *
         * Fuego X
         * Retroceso II
         * ========================================
         */

        ItemStack bow =
                new ItemStack(
                        Material.BOW
                );

        ItemMeta bowMeta =
                bow.getItemMeta();

        bowMeta.addEnchant(
                Enchantment.FLAME,
                10,
                true
        );

        bowMeta.addEnchant(
                Enchantment.PUNCH,
                2,
                true
        );

        bow.setItemMeta(
                bowMeta
        );

        equipment.setItemInMainHand(
                bow
        );

        equipment.setItemInMainHandDropChance(
                0.0f
        );


        /*
         * ========================================
         * ARMADURA DE HIERRO
         *
         * Protección V en todas las piezas.
         * ========================================
         */

        equipment.setHelmet(
                createArmor(
                        Material.IRON_HELMET
                )
        );

        equipment.setChestplate(
                createArmor(
                        Material.IRON_CHESTPLATE
                )
        );

        equipment.setLeggings(
                createArmor(
                        Material.IRON_LEGGINGS
                )
        );

        equipment.setBoots(
                createArmor(
                        Material.IRON_BOOTS
                )
        );


        /*
         * No dropear automáticamente
         * la armadura.
         */

        equipment.setHelmetDropChance(
                0.0f
        );

        equipment.setChestplateDropChance(
                0.0f
        );

        equipment.setLeggingsDropChance(
                0.0f
        );

        equipment.setBootsDropChance(
                0.0f
        );
    }


    /*
     * ========================================
     * CREAR PIEZA DE ARMADURA
     * ========================================
     */

    private ItemStack createArmor(
            Material material
    ) {

        ItemStack armor =
                new ItemStack(
                        material
                );

        ItemMeta meta =
                armor.getItemMeta();

        meta.addEnchant(
                Enchantment.PROTECTION,
                4,
                true
        );

        armor.setItemMeta(
                meta
        );

        return armor;
    }
}