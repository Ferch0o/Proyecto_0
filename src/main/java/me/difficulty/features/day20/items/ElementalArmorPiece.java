
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class ElementalArmorPiece extends CustomItem {

    /*
     * ========================================
     * LAS CUATRO PIEZAS
     * ========================================
     */
    public enum Kind {

        HELMET(
                ItemIds.ELEMENTAL_HELMET,
                ItemIds.ELEMENTAL_HELMET_MODEL,
                Material.NETHERITE_HELMET,
                "Casco Elemental",
                EquipmentSlotGroup.HEAD,
                3.0
        ),

        CHESTPLATE(
                ItemIds.ELEMENTAL_CHESTPLATE,
                ItemIds.ELEMENTAL_CHESTPLATE_MODEL,
                Material.NETHERITE_CHESTPLATE,
                "Pechera Elemental",
                EquipmentSlotGroup.CHEST,
                8.0
        ),

        LEGGINGS(
                ItemIds.ELEMENTAL_LEGGINGS,
                ItemIds.ELEMENTAL_LEGGINGS_MODEL,
                Material.NETHERITE_LEGGINGS,
                "Pantalón Elemental",
                EquipmentSlotGroup.LEGS,
                6.0
        ),

        BOOTS(
                ItemIds.ELEMENTAL_BOOTS,
                ItemIds.ELEMENTAL_BOOTS_MODEL,
                Material.NETHERITE_BOOTS,
                "Botas Elementales",
                EquipmentSlotGroup.FEET,
                3.0
        );

        final String id;
        final int model;
        final Material material;
        final String displayName;
        final EquipmentSlotGroup slot;
        final double armor;

        Kind(
                String id,
                int model,
                Material material,
                String displayName,
                EquipmentSlotGroup slot,
                double armor
        ) {
            this.id = id;
            this.model = model;
            this.material = material;
            this.displayName = displayName;
            this.slot = slot;
            this.armor = armor;
        }
    }

    private final JavaPlugin plugin;
    private final Kind kind;

    public ElementalArmorPiece(
            JavaPlugin plugin,
            Kind kind
    ) {

        super(
                plugin,
                kind.id,
                kind.material,
                kind.model
        );

        this.plugin = plugin;
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    /*
     * ========================================
     * CREAR PIEZA
     * ========================================
     */
    @Override
    public ItemStack createItem() {

        ItemStack item = super.createItem();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        /*
         * Irrompible en todos los días.
         */
        meta.setUnbreakable(true);

        /*
         * Al definir atributos propios,
         * incluimos todos los atributos
         * defensivos de netherite.
         *
         * Protección: igual que netherite.
         * Dureza: 4 por pieza.
         * Resistencia al empuje: 0.1.
         */
        addModifier(
                meta,
                Attribute.ARMOR,
                kind.id + "_armor",
                kind.armor
        );

        addModifier(
                meta,
                Attribute.ARMOR_TOUGHNESS,
                kind.id + "_toughness",
                4.0
        );

        addModifier(
                meta,
                Attribute.KNOCKBACK_RESISTANCE,
                kind.id + "_knockback",
                0.1
        );

        item.setItemMeta(meta);
        return item;
    }

    private void addModifier(
            ItemMeta meta,
            Attribute attribute,
            String name,
            double amount
    ) {

        meta.addAttributeModifier(
                attribute,
                new AttributeModifier(
                        new NamespacedKey(plugin, name),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        kind.slot
                )
        );
    }

    /*
     * ========================================
     * MEJORAR PIEZA DE NETHERITE
     * ========================================
     *
     * Conservamos encantamientos y
     * adornos de la pieza original.
     *
     * No copiamos CustomModelData ni
     * el ID del objeto antiguo.
     */
    public ItemStack createFromBase(
            ItemStack base
    ) {

        ItemStack result = createItem();

        if (base == null
                || base.getType() != kind.material) {
            return result;
        }

        ItemMeta baseMeta = base.getItemMeta();
        ItemMeta resultMeta = result.getItemMeta();

        if (baseMeta == null || resultMeta == null) {
            return result;
        }

        /*
         * Conservar encantamientos.
         */
        for (Map.Entry<Enchantment, Integer> entry
                : baseMeta.getEnchants().entrySet()) {

            resultMeta.addEnchant(
                    entry.getKey(),
                    entry.getValue(),
                    true
            );
        }

        /*
         * Conservar armor trims.
         */
        if (baseMeta instanceof ArmorMeta oldArmor
                && resultMeta instanceof ArmorMeta newArmor
                && oldArmor.hasTrim()) {

            newArmor.setTrim(
                    oldArmor.getTrim()
            );
        }

        result.setItemMeta(resultMeta);
        return result;
    }

    /*
     * ========================================
     * NOMBRE
     * ========================================
     */
    @Override
    protected Component getDisplayName() {

        return Component.text(
                kind.displayName,
                NamedTextColor.LIGHT_PURPLE
        ).decoration(
                TextDecoration.ITALIC,
                false
        );
    }

    /*
     * ========================================
     * DESCRIPCIÓN
     * ========================================
     */
    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                        "Protección de netherite mejorada.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Dureza de armadura: 4.",
                        NamedTextColor.AQUA
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Irrompible.",
                        NamedTextColor.GOLD
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Conjunto completo desde el Día 20:",
                        NamedTextColor.YELLOW
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "+6 puntos de vida máxima.",
                        NamedTextColor.GREEN
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Overworld: Resistencia II.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Nether: Fuerza II.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "End: Velocidad II.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}