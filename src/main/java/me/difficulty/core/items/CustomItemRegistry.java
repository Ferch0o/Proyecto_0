package me.difficulty.core.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomItemRegistry {

    private final Map<String, CustomItem> items =
            new HashMap<>();

    private final NamespacedKey itemIdKey;

    public CustomItemRegistry(JavaPlugin plugin) {

        this.itemIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_item_id"
                );
    }

    public void register(CustomItem item) {

        items.put(
                item.getId(),
                item
        );
    }

    public Optional<CustomItem> get(
            String id
    ) {

        return Optional.ofNullable(
                items.get(id)
        );
    }

    public Optional<String> getItemId(
            ItemStack item
    ) {

        if (item == null
                || item.getType().isAir()) {

            return Optional.empty();
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return Optional.empty();
        }

        String id =
                meta.getPersistentDataContainer().get(
                        itemIdKey,
                        PersistentDataType.STRING
                );

        return Optional.ofNullable(id);
    }

    public boolean is(
            ItemStack item,
            String id
    ) {

        return getItemId(item)
                .map(id::equals)
                .orElse(false);
    }
}