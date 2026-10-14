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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Gladiator extends CustomMob {

    public Gladiator(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.GLADIATOR
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
                        "Gladiador",
                        NamedTextColor.DARK_RED
                )
        );

        skeleton.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * RESISTENCIA III
         * ========================================
         */

        skeleton.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        PotionEffect.INFINITE_DURATION,
                        2,
                        false,
                        false
                )
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
         * ARMA INICIAL
         * ========================================
         *
         * El Gladiador comienza con arco.
         *
         * La mano secundaria permanece vacía.
         */

        equipment.setItemInMainHand(
                createBow()
        );

        equipment.setItemInOffHand(
                new ItemStack(
                        Material.AIR
                )
        );

        equipment.setItemInMainHandDropChance(
                0.0f
        );

        equipment.setItemInOffHandDropChance(
                0.0f
        );


        /*
         * ========================================
         * ARMADURA COMPLETA DE DIAMANTE
         * ========================================
         */

        equipment.setHelmet(
                new ItemStack(
                        Material.DIAMOND_HELMET
                )
        );

        equipment.setChestplate(
                new ItemStack(
                        Material.DIAMOND_CHESTPLATE
                )
        );

        equipment.setLeggings(
                new ItemStack(
                        Material.DIAMOND_LEGGINGS
                )
        );

        equipment.setBoots(
                new ItemStack(
                        Material.DIAMOND_BOOTS
                )
        );


        /*
         * No dropear la armadura.
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
     * CREAR ARCO DEL GLADIADOR
     * ========================================
     */

    public ItemStack createBow() {

        ItemStack bow =
                new ItemStack(
                        Material.BOW
                );

        ItemMeta meta =
                bow.getItemMeta();

        meta.addEnchant(
                Enchantment.POWER,
                5,
                true
        );

        meta.addEnchant(
                Enchantment.PUNCH,
                2,
                true
        );

        bow.setItemMeta(
                meta
        );

        return bow;
    }


    /*
     * ========================================
     * CREAR ESPADA DEL GLADIADOR
     * ========================================
     */

    public ItemStack createSword() {

        ItemStack sword =
                new ItemStack(
                        Material.NETHERITE_SWORD
                );

        ItemMeta meta =
                sword.getItemMeta();

        meta.addEnchant(
                Enchantment.SHARPNESS,
                5,
                true
        );

        meta.addEnchant(
                Enchantment.KNOCKBACK,
                5,
                true
        );

        sword.setItemMeta(
                meta
        );

        return sword;
    }
}