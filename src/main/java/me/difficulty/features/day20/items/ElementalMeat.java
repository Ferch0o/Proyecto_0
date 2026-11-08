
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalMeat extends CustomItem {

    /*
     * COOLDOWN: 15 SEGUNDOS
     */
    public static final int COOLDOWN_SECONDS = 15;

    private final NamespacedKey cooldownGroup;

    public ElementalMeat(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_MEAT,
                Material.COOKED_BEEF,
                ItemIds.ELEMENTAL_MEAT_MODEL
        );

        /*
         * Grupo exclusivo de cooldown.
         * No afecta a la carne vanilla.
         */
        cooldownGroup = new NamespacedKey(
                plugin,
                "elemental_meat"
        );
    }

    /*
     * ========================================
     * CREAR CARNE ELEMENTAL
     * ========================================
     */
    @Override
    public ItemStack createItem() {

        ItemStack item = super.createItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            /*
             * Permitir consumir incluso
             * con la barra de hambre llena.
             */
            FoodComponent food = meta.getFood();

            food.setCanAlwaysEat(true);
            meta.setFood(food);

            /*
             * ========================================
             * COOLDOWN VISUAL
             * ========================================
             *
             * Mismo sistema que utiliza
             * la Estrella Corrupta.
             */
            UseCooldownComponent cooldown =
                    meta.getUseCooldown();

            cooldown.setCooldownSeconds(
                    COOLDOWN_SECONDS
            );

            cooldown.setCooldownGroup(
                    cooldownGroup
            );

            meta.setUseCooldown(cooldown);

            item.setItemMeta(meta);
        }

        return item;
    }

    /*
     * ========================================
     * GRUPO DE COOLDOWN
     * ========================================
     */
    public NamespacedKey getCooldownGroup() {
        return cooldownGroup;
    }

    /*
     * ========================================
     * NOMBRE
     * ========================================
     */
    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Carne Elemental",
                NamedTextColor.GOLD
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
                        "Regeneración V durante 2 segundos.",
                        NamedTextColor.GREEN
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Hambre X durante 4 segundos.",
                        NamedTextColor.RED
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Reutilización: 15 segundos.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Obtenida de los Ninjas.",
                        NamedTextColor.DARK_GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}
