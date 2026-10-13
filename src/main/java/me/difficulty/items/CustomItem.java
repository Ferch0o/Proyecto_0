package me.difficulty.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class CustomItem {

    private final String id;
    private final Material material;
    private final int modelId;

    private final NamespacedKey itemIdKey;

    protected CustomItem(
            JavaPlugin plugin,
            String id,
            Material material,
            int modelId
    ) {

        this.id = id;
        this.material = material;
        this.modelId = modelId;

        this.itemIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_item_id"
                );
    }

    public ItemStack createItem() {

        ItemStack item =
                new ItemStack(material);

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                getDisplayName()
        );

        meta.lore(
                getLore()
        );

        /*
         * ID lógico del objeto.
         *
         * Ejemplo:
         * difficultyplugin:custom_item_id
         * =
         * corrupted_star
         */
        meta.getPersistentDataContainer().set(
                itemIdKey,
                PersistentDataType.STRING,
                id
        );

        /*
         * ID reservado para el futuro Resource Pack.
         *
         * Aunque ahora no tengamos textura,
         * podemos guardar ya el número.
         */
        meta.setCustomModelData(
                modelId
        );

        item.setItemMeta(meta);

        return item;
    }

    public boolean isItem(ItemStack item) {

        if (item == null
                || item.getType() != material) {

            return false;
        }

        ItemMeta meta =
                item.getItemMeta();

        if (meta == null) {
            return false;
        }

        String storedId =
                meta.getPersistentDataContainer().get(
                        itemIdKey,
                        PersistentDataType.STRING
                );

        return id.equals(storedId);
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getModelId() {
        return modelId;
    }

    protected abstract Component getDisplayName();

    protected abstract List<Component> getLore();
}