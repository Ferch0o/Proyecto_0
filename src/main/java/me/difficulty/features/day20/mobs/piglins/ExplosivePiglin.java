
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ExplosivePiglin extends CustomMob {

    private static final double MAX_HEALTH = 10.0;

    private final ExplosiveCrossbow explosiveCrossbow;

    public ExplosivePiglin(
            JavaPlugin plugin,
            ExplosiveCrossbow explosiveCrossbow
    ) {
        super(plugin, MobIds.EXPLOSIVE_PIGLIN);
        this.explosiveCrossbow = explosiveCrossbow;
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof Piglin piglin)) {
            return;
        }

        piglin.customName(
                Component.text(
                        "Piglin Explosivo",
                        NamedTextColor.RED
                )
        );

        piglin.setCustomNameVisible(true);

        // Debe ser adulto para utilizar la ballesta.
        piglin.setAdult();

        piglin.setImmuneToZombification(true);

        // No recoger oro ni sustituir su ballesta.
        piglin.setCanPickupItems(false);

        // Evitar que el oro lo distraiga.
        piglin.setMemory(
                MemoryKey.ADMIRING_DISABLED,
                Boolean.TRUE
        );

        piglin.setMemory(
                MemoryKey.DISABLE_WALK_TO_ADMIRE_ITEM,
                Boolean.TRUE
        );

        /*
         * VIDA: 10 HP
         */
        AttributeInstance health = piglin.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            piglin.setHealth(MAX_HEALTH);
        }

        /*
         * VELOCIDAD II
         */
        piglin.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        1,
                        false,
                        false,
                        true
                )
        );

        /*
         * BALLESTA EXPLOSIVA
         * CARGA RÁPIDA III
         */
        EntityEquipment equipment = piglin.getEquipment();

        if (equipment == null) {
            return;
        }

        ItemStack crossbow = explosiveCrossbow.createItem();

        crossbow.addUnsafeEnchantment(
                Enchantment.QUICK_CHARGE,
                3
        );

        equipment.setItemInMainHand(crossbow);
        equipment.setItemInMainHandDropChance(0.0f);

        // Sin equipo secundario.
        equipment.setItemInOffHand(null);
        equipment.setItemInOffHandDropChance(0.0f);
    }
}