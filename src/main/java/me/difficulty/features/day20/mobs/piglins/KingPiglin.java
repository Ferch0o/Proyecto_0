
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class KingPiglin extends CustomMob {

    private static final double MAX_HEALTH = 150.0;
    private static final int SHARPNESS_LEVEL = 30;

    public KingPiglin(JavaPlugin plugin) {
        super(plugin, MobIds.KING_PIGLIN);
    }

    @Override
    protected void configure(LivingEntity entity) {

        /*
         * Todos los Reyes Piglin utilizan
         * la entidad base PiglinBrute.
         */
        if (!(entity instanceof PiglinBrute brute)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        brute.customName(
                Component.text(
                        "Rey Piglin",
                        NamedTextColor.GOLD
                )
        );

        brute.setCustomNameVisible(true);

        /*
         * ========================================
         * CONFIGURACIÓN GENERAL
         * ========================================
         *
         * El Piglin Bruto ignora la
         * armadura de oro y no realiza
         * trueques vanilla.
         */
        brute.setAdult();

        brute.setImmuneToZombification(true);

        brute.setCanPickupItems(false);

        /*
         * ========================================
         * VIDA: 150 HP
         * ========================================
         */
        AttributeInstance health = brute.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {

            health.setBaseValue(MAX_HEALTH);
            brute.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * HACHA DE NETHERITA - FILO XXX
         * ========================================
         */
        EntityEquipment equipment =
                brute.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack axe = new ItemStack(
                Material.NETHERITE_AXE
        );

        axe.addUnsafeEnchantment(
                Enchantment.SHARPNESS,
                SHARPNESS_LEVEL
        );

        equipment.setItemInMainHand(axe);
        equipment.setItemInMainHandDropChance(0.0f);

        equipment.setItemInOffHand(null);
        equipment.setItemInOffHandDropChance(0.0f);
    }
}