
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Gladiator extends CustomMob {

    public Gladiator(JavaPlugin plugin) {
        super(plugin, MobIds.GLADIATOR);
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
                        "Gladiador",
                        NamedTextColor.DARK_RED
                )
        );

        skeleton.setCustomNameVisible(true);

        /*
         * ========================================
         * 40 CORAZONES
         * ========================================
         */

        AttributeInstance maxHealth =
                skeleton.getAttribute(Attribute.MAX_HEALTH);

        if (maxHealth != null) {
            maxHealth.setBaseValue(80.0);
            skeleton.setHealth(80.0);
        }

        /*
         * ========================================
         * LENTITUD I
         * ========================================
         */

        skeleton.addPotionEffect(
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
         * ARMA INICIAL: ARCO
         * ========================================
         *
         * GladiatorWeaponTask cambiará
         * entre arco y espada.
         */

        equipment.setItemInMainHand(createBow());
        equipment.setItemInOffHand(
                new ItemStack(Material.AIR)
        );

        equipment.setItemInMainHandDropChance(0.0f);
        equipment.setItemInOffHandDropChance(0.0f);

        /*
         * ========================================
         * ARMADURA DE DIAMANTE
         * PROTECCIÓN II
         * ========================================
         */

        equipment.setHelmet(
                createArmor(Material.DIAMOND_HELMET)
        );

        equipment.setChestplate(
                createArmor(Material.DIAMOND_CHESTPLATE)
        );

        equipment.setLeggings(
                createArmor(Material.DIAMOND_LEGGINGS)
        );

        equipment.setBoots(
                createArmor(Material.DIAMOND_BOOTS)
        );

        /*
         * NO SOLTAR ARMADURA
         */

        equipment.setHelmetDropChance(0.0f);
        equipment.setChestplateDropChance(0.0f);
        equipment.setLeggingsDropChance(0.0f);
        equipment.setBootsDropChance(0.0f);
    }

    /*
     * ========================================
     * ARMADURA DE PROTECCIÓN II
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

    /*
     * ========================================
     * ARCO:
     * PODER V + RETROCESO II
     * ========================================
     */

    public ItemStack createBow() {

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();

        if (meta != null) {

            meta.addEnchant(
                    Enchantment.POWER,
                    20,
                    true
            );

            meta.addEnchant(
                    Enchantment.PUNCH,
                    2,
                    true
            );

            bow.setItemMeta(meta);
        }

        return bow;
    }

    /*
     * ========================================
     * ESPADA:
     * NETHERITE + FILO V + EMPUJE V
     * ========================================
     */

    public ItemStack createSword() {

        ItemStack sword =
                new ItemStack(Material.NETHERITE_SWORD);

        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {

            meta.addEnchant(
                    Enchantment.SHARPNESS,
                    20,
                    true
            );

            meta.addEnchant(
                    Enchantment.KNOCKBACK,
                    5,
                    true
            );

            sword.setItemMeta(meta);
        }

        return sword;
    }
}
