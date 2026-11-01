
package me.difficulty.features.day17.items.elementalpotato;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.day17.items.Day17Material;
import me.difficulty.features.day17.items.Day17Materials;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public final class ElementalPotatoRecipeBookListener
        implements Listener {

    private final DayManager dayManager;
    private final Day17Materials materials;
    private final ElementalPotato elementalPotato;
    private final NamespacedKey recipeKey;
    private final NamespacedKey customItemIdKey;

    public ElementalPotatoRecipeBookListener(
            JavaPlugin plugin,
            DayManager dayManager,
            Day17Materials materials,
            ElementalPotato elementalPotato,
            NamespacedKey recipeKey
    ) {
        this.dayManager = dayManager;
        this.materials = materials;
        this.elementalPotato = elementalPotato;
        this.recipeKey = recipeKey;

        this.customItemIdKey =
                new NamespacedKey(plugin, "custom_item_id");
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        // Solo nuestra receta personalizada.
        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

        // Impedir que Minecraft coloque
        // solamente una unidad por casilla.
        event.setCancelled(true);

        Player player = event.getPlayer();

        if (!dayManager.isAtLeast(17)) {
            return;
        }

        // Debe utilizarse una mesa 3x3.
        if (!(player.getOpenInventory()
                .getTopInventory()
                instanceof CraftingInventory crafting)) {
            return;
        }

        ItemStack[] currentMatrix =
                crafting.getMatrix();

        if (currentMatrix.length != 9) {
            player.sendMessage(
                    "§cNecesitas una mesa de crafteo 3x3."
            );
            return;
        }

        // Por seguridad, exigimos mesa vacía.
        for (ItemStack item : currentMatrix) {
            if (item != null && !item.getType().isAir()) {
                player.sendMessage(
                        "§cVacía primero la mesa de crafteo."
                );
                return;
            }
        }

        ItemStack[] storage =
                player.getInventory().getStorageContents();

        // Verificar cantidades TOTALES.
        if (count(storage, materials.driedMeat()::matches) < 64
                || count(storage, materials.guardianBone()::matches) < 64
                || count(storage, materials.magicGunpowder()::matches) < 64
                || count(storage, materials.wardenHeart()::matches) < 2
                || count(storage, materials.titaniumBlock()::matches) < 32
                || count(storage, materials.goliathEye()::matches) < 64
                || count(storage, this::isVanillaPoisonousPotato) < 16) {

            player.sendMessage(
                    "§cNo tienes todos los materiales necesarios."
            );
            return;
        }

        // Copiar almacenamiento antes de retirar.
        ItemStack[] copy = storage.clone();

        for (int i = 0; i < copy.length; i++) {
            if (copy[i] != null) {
                copy[i] = copy[i].clone();
            }
        }

        // Descontar los ingredientes que
        // trasladaremos a la mesa.
        take(copy, materials.driedMeat()::matches, 64);
        take(copy, materials.guardianBone()::matches, 64);
        take(copy, materials.magicGunpowder()::matches, 64);
        take(copy, materials.wardenHeart()::matches, 2);
        take(copy, materials.titaniumBlock()::matches, 32);
        take(copy, materials.goliathEye()::matches, 64);
        take(copy, this::isVanillaPoisonousPotato, 16);

        // Construir exactamente las nueve casillas.
        ItemStack[] matrix = {

                stack(materials.driedMeat(), 64),
                stack(materials.guardianBone(), 64),
                stack(materials.magicGunpowder(), 64),

                stack(materials.wardenHeart(), 1),
                new ItemStack(Material.POISONOUS_POTATO, 16),
                stack(materials.wardenHeart(), 1),

                stack(materials.titaniumBlock(), 16),
                stack(materials.goliathEye(), 64),
                stack(materials.titaniumBlock(), 16)
        };

        // Actualizar inventario y mesa.
        player.getInventory().setStorageContents(copy);

        crafting.setMatrix(matrix);

        crafting.setResult(
                elementalPotato.createItem()
        );

        player.updateInventory();
    }

    private static ItemStack stack(
            Day17Material material,
            int amount
    ) {
        ItemStack item = material.createItem();
        item.setAmount(amount);
        return item;
    }

    private boolean isVanillaPoisonousPotato(
            ItemStack item
    ) {
        if (item == null
                || item.getType() != Material.POISONOUS_POTATO) {
            return false;
        }

        return !item.hasItemMeta()
                || !item.getItemMeta()
                .getPersistentDataContainer()
                .has(
                        customItemIdKey,
                        PersistentDataType.STRING
                );
    }

    private static int count(
            ItemStack[] contents,
            Predicate<ItemStack> condition
    ) {
        int total = 0;

        for (ItemStack item : contents) {
            if (item != null && condition.test(item)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private static void take(
            ItemStack[] contents,
            Predicate<ItemStack> condition,
            int amount
    ) {
        int remaining = amount;

        for (int i = 0;
             i < contents.length && remaining > 0;
             i++) {

            ItemStack item = contents[i];

            if (item == null || !condition.test(item)) {
                continue;
            }

            int removed = Math.min(
                    remaining,
                    item.getAmount()
            );

            int left = item.getAmount() - removed;

            if (left <= 0) {
                contents[i] = null;
            } else {
                item.setAmount(left);
            }

            remaining -= removed;
        }
    }
}
