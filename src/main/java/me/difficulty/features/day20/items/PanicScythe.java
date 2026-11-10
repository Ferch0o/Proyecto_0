
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
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class PanicScythe extends CustomItem {

    public static final int COOLDOWN_TICKS = 20;

    private final NamespacedKey cooldownGroup;

    public PanicScythe(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.PANIC_SCYTHE,
                Material.NETHERITE_HOE,
                ItemIds.PANIC_SCYTHE_MODEL
        );

        cooldownGroup = new NamespacedKey(
                plugin,
                "panic_scythe"
        );
    }

    @Override
    public ItemStack createItem() {

        ItemStack item = super.createItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.setUnbreakable(true);

            UseCooldownComponent cooldown =
                    meta.getUseCooldown();

            cooldown.setCooldownSeconds(1.0f);
            cooldown.setCooldownGroup(cooldownGroup);

            meta.setUseCooldown(cooldown);
            item.setItemMeta(meta);
        }

        return item;
    }

    public NamespacedKey getCooldownGroup() {
        return cooldownGroup;
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Guadaña del Pánico",
                NamedTextColor.DARK_RED
        ).decoration(
                TextDecoration.ITALIC,
                false
        );
    }

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                        "Clic derecho: teletransporte de 5 bloques.",
                        NamedTextColor.LIGHT_PURPLE
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Costo: 2 bloques de oro por uso.",
                        NamedTextColor.GOLD
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Enfriamiento: 1 segundo.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Objeto especial del Día 20.",
                        NamedTextColor.DARK_GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}