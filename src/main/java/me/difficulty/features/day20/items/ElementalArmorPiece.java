
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
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
     * MEJORAR NETHERITE A ARMADURA ELEMENTAL
     * ========================================
     *
     * Conservamos los datos personalizados,
     * excepto:
     *
     * - Reparación.
     * - Irrompibilidad.
     * - Daño acumulado.
     * - Atributos defensivos anteriores.
     * - Modelo e identidad anteriores.
     */
    public ItemStack createFromBase(
            ItemStack base
    ) {

        ItemStack elemental = createItem();

        if (base == null
                || base.getType() != kind.material) {
            return elemental;
        }

        ItemMeta originalMeta = base.getItemMeta();
        ItemMeta elementalMeta = elemental.getItemMeta();

        if (originalMeta == null
                || elementalMeta == null) {
            return elemental;
        }

        /*
         * ========================================
         * CONSERVAR METADATOS DEL JUGADOR
         * ========================================
         *
         * Incluye:
         * - Encantamientos.
         * - Armor trims.
         * - Nombres personalizados.
         * - Repair cost.
         * - Datos de otros plugins.
         * - Item flags.
         */
        ItemMeta newMeta = originalMeta.clone();

        /*
         * ========================================
         * NOMBRE
         * ========================================
         *
         * Respetar el nombre dado mediante
         * un yunque.
         *
         * Si no existe, utilizar el nombre
         * correspondiente a la pieza elemental.
         */
        if (!originalMeta.hasCustomName()
                && !originalMeta.hasItemName()) {

            newMeta.customName(
                    elementalMeta.customName()
            );
        }

        /*
         * ========================================
         * LORE PERSONALIZADO
         * ========================================
         */
        List<Component> lore = new ArrayList<>();

        List<Component> oldLore = originalMeta.lore();

        if (oldLore != null && !oldLore.isEmpty()) {

            lore.addAll(oldLore);
            lore.add(Component.empty());
        }

        List<Component> elementalLore =
                elementalMeta.lore();

        if (elementalLore != null) {
            lore.addAll(elementalLore);
        }

        newMeta.lore(lore);

        /*
         * ========================================
         * ELIMINAR ENCANTAMIENTOS INNECESARIOS
         * ========================================
         *
         * El resto de encantamientos permanece.
         */
        newMeta.removeEnchant(
                Enchantment.MENDING
        );

        newMeta.removeEnchant(
                Enchantment.UNBREAKING
        );

        /*
         * ========================================
         * REPARAR COMPLETAMENTE
         * ========================================
         *
         * Incluso si la armadura original
         * solamente tenía 1 punto restante.
         */
        if (newMeta instanceof Damageable damageable) {

            damageable.resetDamage();
        }

        /*
         * ========================================
         * HACER IRROMPIBLE LA PIEZA
         * ========================================
         *
         * Esta propiedad no es el
         * encantamiento Irrompibilidad.
         */
        newMeta.setUnbreakable(true);

        /*
         * ========================================
         * ASIGNAR IDENTIDAD ELEMENTAL
         * ========================================
         *
         * Conservar PDC anteriores, pero
         * sobrescribir nuestra identificación
         * con el ID de Armadura Elemental.
         */
        elementalMeta.getPersistentDataContainer()
                .copyTo(
                        newMeta.getPersistentDataContainer(),
                        true
                );

        /*
         * Utilizar el modelo elemental.
         * No conservar el modelo original.
         */
        newMeta.setCustomModelDataComponent(
                elementalMeta.getCustomModelDataComponent()
        );

        newMeta.setItemModel(
                elementalMeta.getItemModel()
        );

        /*
         * ========================================
         * ATRIBUTOS DEFENSIVOS ELEMENTALES
         * ========================================
         *
         * Quitar estadísticas anteriores para
         * evitar que se acumulen.
         */
        newMeta.removeAttributeModifier(
                Attribute.ARMOR
        );

        newMeta.removeAttributeModifier(
                Attribute.ARMOR_TOUGHNESS
        );

        newMeta.removeAttributeModifier(
                Attribute.KNOCKBACK_RESISTANCE
        );

        /*
         * Restaurar la protección de netherite.
         */
        addModifier(
                newMeta,
                Attribute.ARMOR,
                kind.id + "_armor",
                kind.armor
        );

        /*
         * Dureza elemental: 4 por pieza.
         */
        addModifier(
                newMeta,
                Attribute.ARMOR_TOUGHNESS,
                kind.id + "_toughness",
                4.0
        );

        /*
         * Resistencia al empuje de netherite.
         */
        addModifier(
                newMeta,
                Attribute.KNOCKBACK_RESISTANCE,
                kind.id + "_knockback",
                0.1
        );

        /*
         * ========================================
         * ENTREGAR PIEZA ELEMENTAL
         * ========================================
         */
        elemental.setItemMeta(newMeta);

        return elemental;
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