
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class CorruptedPigman extends CustomMob {

    private static final double MAX_HEALTH = 100.0;
    private static final int SHARPNESS_CHANCE = 15;

    public CorruptedPigman(JavaPlugin plugin) {
        super(plugin, MobIds.CORRUPTED_PIGMAN);
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
                        "Pigman Corrupto",
                        NamedTextColor.DARK_RED
                )
        );

        pigman.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 100 HP = 50 CORAZONES
         * ========================================
         */
        AttributeInstance health = pigman.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            pigman.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * ESPADA DE NETHERITA
         * ========================================
         */
        EntityEquipment equipment = pigman.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack sword = new ItemStack(
                Material.NETHERITE_SWORD
        );

        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {

            // Aspecto Ígneo X: siempre.
            meta.addEnchant(
                    Enchantment.FIRE_ASPECT,
                    10,
                    true
            );

            /*
             * Filo XX: solo en el 15%
             * de los Pigman Corruptos.
             */
            if (ThreadLocalRandom.current()
                    .nextInt(100) < SHARPNESS_CHANCE) {

                meta.addEnchant(
                        Enchantment.SHARPNESS,
                        20,
                        true
                );
            }

            sword.setItemMeta(meta);
        }

        equipment.setItemInMainHand(sword);

        /*
         * No soltar el arma al morir.
         * El listener también elimina
         * los drops vanilla.
         */
        equipment.setItemInMainHandDropChance(0.0f);
    }
}
