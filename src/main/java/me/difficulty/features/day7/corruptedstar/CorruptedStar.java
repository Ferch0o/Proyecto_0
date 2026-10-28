package me.difficulty.features.day7.corruptedstar;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CorruptedStar extends CustomItem {

    private final JavaPlugin plugin;

    public CorruptedStar(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.CORRUPTED_STAR,
                Material.NETHER_STAR,
                ItemIds.CORRUPTED_STAR_MODEL
        );

        this.plugin = plugin;
    }

    @Override
    public ItemStack createItem() {

        // Primero creamos el CustomItem normal
        ItemStack item =
                super.createItem();

        ItemMeta meta =
                item.getItemMeta();

        /*
         * Creamos un grupo de cooldown exclusivo
         * para la Estrella Corrupta.
         */
        UseCooldownComponent cooldown =
                meta.getUseCooldown();

        cooldown.setCooldownSeconds(60.0f);

        cooldown.setCooldownGroup(
                new NamespacedKey(
                        plugin,
                        "corrupted_star"
                )
        );

        meta.setUseCooldown(cooldown);

        item.setItemMeta(meta);

        return item;
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                        "Estrella Corrupta",
                        NamedTextColor.DARK_PURPLE
                )
                .decoration(
                        TextDecoration.ITALIC,
                        false
                );
    }

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                                "Un objeto contaminado por una energía desconocida.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Disponible desde el Día 7.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }

    public boolean isCorruptedStar(
            ItemStack item
    ) {

        return isItem(item);
    }
}