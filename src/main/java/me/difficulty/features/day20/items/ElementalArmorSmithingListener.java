
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.SmithItemEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingTransformRecipe;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ElementalArmorSmithingListener
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final ElementalTemplate template;
    private final ElementalIngot ingot;

    private final NamespacedKey customIdKey;

    private final Map<NamespacedKey, ElementalArmorPiece>
            recipes = new LinkedHashMap<>();

    public ElementalArmorSmithingListener(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalTemplate template,
            ElementalIngot ingot,
            Collection<ElementalArmorPiece> pieces
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.template = template;
        this.ingot = ingot;

        customIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );

        for (ElementalArmorPiece piece : pieces) {

            NamespacedKey key = new NamespacedKey(
                    plugin,
                    piece.getKind().id + "_smithing"
            );

            recipes.put(key, piece);
        }
    }

    /*
     * ========================================
     * REGISTRAR RECETAS
     * ========================================
     */
    public void register() {

        for (Map.Entry<NamespacedKey, ElementalArmorPiece>
                entry : recipes.entrySet()) {

            ElementalArmorPiece piece = entry.getValue();

            SmithingTransformRecipe recipe =
                    new SmithingTransformRecipe(
                            entry.getKey(),
                            piece.createItem(),

                            new RecipeChoice.ExactChoice(
                                    template.createItem()
                            ),

                            new RecipeChoice.MaterialChoice(
                                    piece.getMaterial()
                            ),

                            new RecipeChoice.ExactChoice(
                                    ingot.createItem()
                            ),

                            /*
                             * El resultado tiene su
                             * propio ID y atributos.
                             *
                             * Los encantamientos y trims
                             * se copiarán por separado.
                             */
                            false
                    );

            plugin.getServer().addRecipe(recipe);
        }
    }

    public NamespacedKey[] getRecipeKeys() {

        return recipes.keySet().toArray(
                new NamespacedKey[0]
        );
    }

    private ElementalArmorPiece findPiece(
            Recipe recipe
    ) {

        if (!(recipe instanceof Keyed keyed)) {
            return null;
        }

        return recipes.get(keyed.getKey());
    }

    /*
     * ========================================
     * RECONOCER NETHERITE VANILLA
     * ========================================
     */
    private boolean isVanillaArmor(
            ItemStack item,
            ElementalArmorPiece piece
    ) {

        if (item == null
                || item.getType() != piece.getMaterial()) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return true;
        }

        return !item.getItemMeta()
                .getPersistentDataContainer()
                .has(
                        customIdKey,
                        PersistentDataType.STRING
                );
    }

    private boolean validInputs(
            SmithingInventory inventory,
            ElementalArmorPiece piece
    ) {

        ItemStack inputTemplate =
                inventory.getInputTemplate();

        ItemStack inputArmor =
                inventory.getInputEquipment();

        ItemStack inputIngot =
                inventory.getInputMineral();

        return template.isItem(inputTemplate)
                && inputTemplate.isSimilar(
                template.createItem()
        )

                && isVanillaArmor(
                inputArmor,
                piece
        )

                && ingot.isItem(inputIngot)
                && inputIngot.isSimilar(
                ingot.createItem()
        );
    }

    /*
     * ========================================
     * PREPARAR PIEZA ELEMENTAL
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(
            PrepareSmithingEvent event
    ) {

        SmithingInventory inventory =
                event.getInventory();

        ElementalArmorPiece piece =
                findPiece(inventory.getRecipe());

        if (piece == null) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || !validInputs(inventory, piece)) {

            event.setResult(null);
            return;
        }

        /*
         * Preparar un resultado con
         * encantamientos y trims originales.
         */
        event.setResult(
                piece.createFromBase(
                        inventory.getInputEquipment()
                )
        );
    }

    /*
     * ========================================
     * VALIDAR RETIRADA DEL RESULTADO
     * ========================================
     *
     * No modificamos manualmente los
     * ingredientes ni el cursor.
     *
     * Minecraft consume los tres
     * ingredientes normalmente.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSmith(
            SmithItemEvent event
    ) {

        SmithingInventory inventory =
                event.getInventory();

        ElementalArmorPiece piece =
                findPiece(inventory.getRecipe());

        if (piece == null) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || !validInputs(inventory, piece)) {

            event.setCancelled(true);
            return;
        }

        ItemStack expected =
                piece.createFromBase(
                        inventory.getInputEquipment()
                );

        ItemStack result =
                event.getCurrentItem();

        /*
         * Comprobar que el resultado
         * corresponda a la pieza auténtica.
         */
        if (result == null
                || !result.isSimilar(expected)
                || result.getAmount() != 1) {

            event.setCancelled(true);
        }
    }
}