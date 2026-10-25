package me.difficulty.mobs.day10;

import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
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

    public CorruptedZombie(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.CORRUPTED_ZOMBIE
        );
    }

    @Override
    protected void configure(
            LivingEntity entity
    ) {

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
         * VIDA
         * ========================================
         *
         * 40 puntos de vida = 20 corazones.
         */

        AttributeInstance maxHealth =
                zombie.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (maxHealth != null) {

            maxHealth.setBaseValue(
                    40.0
            );

            zombie.setHealth(
                    40.0
            );
        }


        /*
         * ========================================
         * LENTITUD I
         * ========================================
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
         * RESISTENCIA III
         * ========================================
         */

        zombie.addPotionEffect(
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
         * RESISTENCIA AL EMPUJE
         * ========================================
         *
         * 1.0 = 100 %.
         *
         * El TXT solo especifica que es resistente
         * al empuje, no un porcentaje concreto.
         */

        AttributeInstance knockbackResistance =
                zombie.getAttribute(
                        Attribute.KNOCKBACK_RESISTANCE
                );

        if (knockbackResistance != null) {

            knockbackResistance.setBaseValue(
                    1.0
            );
        }


        /*
         * ========================================
         * ARMADURA DE CUERO COMPLETA
         * ========================================
         */

        EntityEquipment equipment =
                zombie.getEquipment();

        if (equipment != null) {

            equipment.setHelmet(
                    new ItemStack(
                            Material.LEATHER_HELMET
                    )
            );

            equipment.setChestplate(
                    new ItemStack(
                            Material.LEATHER_CHESTPLATE
                    )
            );

            equipment.setLeggings(
                    new ItemStack(
                            Material.LEATHER_LEGGINGS
                    )
            );

            equipment.setBoots(
                    new ItemStack(
                            Material.LEATHER_BOOTS
                    )
            );


            /*
             * Evitamos que la armadura
             * se dropee normalmente.
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

            /*
             * ========================================
             * ESPADA DE HIERRO CON FILO III
             * ========================================
             */

            ItemStack sword =
                    new ItemStack(
                            Material.NETHERITE_SWORD
                    );

            ItemMeta swordMeta = sword.getItemMeta();

            swordMeta.addEnchant(
                    Enchantment.SHARPNESS,
                    2,
                    true
            );

            sword.setItemMeta(swordMeta);

            equipment.setItemInMainHand(sword);

            equipment.setItemInMainHandDropChance(0.0f);
        }
    }
}