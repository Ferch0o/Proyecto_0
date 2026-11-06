
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FortressGuardian extends CustomMob {

    private static final double MAX_HEALTH = 40.0;

    private final JavaPlugin plugin;
    private boolean bowGoalWarningLogged = false;

    public FortressGuardian(JavaPlugin plugin) {
        super(plugin, MobIds.FORTRESS_GUARDIAN);
        this.plugin = plugin;
    }

    @Override
    protected void configure(LivingEntity entity) {

        if (!(entity instanceof WitherSkeleton skeleton)) {
            return;
        }

        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        skeleton.customName(
                Component.text(
                        "Guardián de la Fortaleza",
                        NamedTextColor.GOLD
                )
        );

        skeleton.setCustomNameVisible(true);

        /*
         * ========================================
         * VIDA: 40 PUNTOS = 20 CORAZONES
         * ========================================
         */
        AttributeInstance health = skeleton.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            skeleton.setHealth(MAX_HEALTH);
        }

        /*
         * ========================================
         * VELOCIDAD I
         * ========================================
         */
        skeleton.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );

        EntityEquipment equipment = skeleton.getEquipment();

        if (equipment == null) {
            return;
        }

        /*
         * ========================================
         * ARCO: PODER LX Y FUEGO I
         * ========================================
         */
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();

        if (bowMeta != null) {

            bowMeta.addEnchant(
                    Enchantment.POWER,
                    60,
                    true
            );

            bowMeta.addEnchant(
                    Enchantment.FLAME,
                    1,
                    true
            );

            bow.setItemMeta(bowMeta);
        }

        equipment.setItemInMainHand(bow);
        equipment.setItemInOffHand(
                new ItemStack(Material.AIR)
        );

        equipment.setItemInMainHandDropChance(0.0f);
        equipment.setItemInOffHandDropChance(0.0f);

        /*
         * ========================================
         * DOS PIEZAS ALEATORIAS DE NETHERITA
         * ========================================
         */
        List<Material> pieces = new ArrayList<>(
                List.of(
                        Material.NETHERITE_HELMET,
                        Material.NETHERITE_CHESTPLATE,
                        Material.NETHERITE_LEGGINGS,
                        Material.NETHERITE_BOOTS
                )
        );

        Collections.shuffle(pieces);

        for (int i = 0; i < 2; i++) {

            Material type = pieces.get(i);
            ItemStack armor = new ItemStack(type);

            ItemMeta armorMeta = armor.getItemMeta();

            if (armorMeta != null) {
                armorMeta.addEnchant(
                        Enchantment.PROTECTION,
                        7,
                        true
                );

                armor.setItemMeta(armorMeta);
            }

            switch (type) {

                case NETHERITE_HELMET ->
                        equipment.setHelmet(armor);

                case NETHERITE_CHESTPLATE ->
                        equipment.setChestplate(armor);

                case NETHERITE_LEGGINGS ->
                        equipment.setLeggings(armor);

                case NETHERITE_BOOTS ->
                        equipment.setBoots(armor);

                default -> throw new IllegalStateException(
                        "Pieza de armadura inesperada"
                );
            }
        }

        /*
         * NO SOLTAR LAS ARMADURAS
         */
        equipment.setHelmetDropChance(0.0f);
        equipment.setChestplateDropChance(0.0f);
        equipment.setLeggingsDropChance(0.0f);
        equipment.setBootsDropChance(0.0f);

        /*
         * ========================================
         * ACTUALIZAR IA PARA USAR ARCO
         * ========================================
         */
        plugin.getServer()
                .getScheduler()
                .runTask(
                        plugin,
                        () -> refreshBowCombat(skeleton)
                );
    }

    /*
     * ========================================
     * REEVALUAR LA IA DEL WITHER SKELETON
     * ========================================
     */
    private void refreshBowCombat(
            WitherSkeleton skeleton
    ) {

        if (!skeleton.isValid() || skeleton.isDead()) {
            return;
        }

        try {

            Object handle = skeleton.getClass()
                    .getMethod("getHandle")
                    .invoke(skeleton);

            Class<?> clazz = handle.getClass();
            Method target = null;

            while (clazz != null && target == null) {

                try {
                    target = clazz.getDeclaredMethod(
                            "reassessWeaponGoal"
                    );

                } catch (NoSuchMethodException ex) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (target == null) {
                throw new NoSuchMethodException(
                        "reassessWeaponGoal"
                );
            }

            target.setAccessible(true);
            target.invoke(handle);

        } catch (ReflectiveOperationException
                 | RuntimeException ex) {

            if (!bowGoalWarningLogged) {

                bowGoalWarningLogged = true;

                plugin.getLogger().warning(
                        "No se pudo actualizar la IA "
                                + "del Guardián: "
                                + ex.getMessage()
                );
            }
        }
    }
}
