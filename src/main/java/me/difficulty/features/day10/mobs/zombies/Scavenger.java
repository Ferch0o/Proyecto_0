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

import java.util.concurrent.ThreadLocalRandom;

public class Scavenger extends CustomMob {

    public Scavenger(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.SCAVENGER
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
                        "Carroñero",
                        NamedTextColor.GOLD
                )
        );

        zombie.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * VIDA
         * ========================================
         *
         * 10 puntos = 5 corazones.
         */

        AttributeInstance maxHealth =
                zombie.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (maxHealth != null) {

            maxHealth.setBaseValue(
                    10.0
            );

            zombie.setHealth(
                    10.0
            );
        }


        /*
         * ========================================
         * VELOCIDAD II
         * ========================================
         */

        zombie.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false
                )
        );


        /*
         * ========================================
         * 40 % DE PROBABILIDAD DE SER BEBÉ
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 40) {

            zombie.setBaby(
                    true
            );
        } else {

            zombie.setBaby(
                    false
            );
        }

        /*
         * ========================================
         * EQUIPAMIENTO
         * ========================================
         */

        EntityEquipment equipment =
                zombie.getEquipment();

        if (equipment == null) {
            return;
        }

        /*
         * ========================================
         * HACHA DE DIAMANTE + FILO XV
         * ========================================
         */

        ItemStack axe =
                new ItemStack(
                        Material.DIAMOND_AXE
                );

        ItemMeta axeMeta =
                axe.getItemMeta();

        axeMeta.addEnchant(
                Enchantment.SHARPNESS,
                20,
                true
        );

        axe.setItemMeta(
                axeMeta
        );

        equipment.setItemInMainHand(
                axe
        );

        equipment.setItemInMainHandDropChance(
                0.0f
        );


        /*
         * ========================================
         * CASCO DE HIERRO + PROTECCIÓN II
         * ========================================
         */

        ItemStack helmet =
                new ItemStack(
                        Material.IRON_HELMET
                );

        ItemMeta helmetMeta =
                helmet.getItemMeta();

        helmetMeta.addEnchant(
                Enchantment.PROTECTION,
                2,
                true
        );

        helmet.setItemMeta(
                helmetMeta
        );

        equipment.setHelmet(
                helmet
        );

        equipment.setHelmetDropChance(
                0.0f
        );
    }
}