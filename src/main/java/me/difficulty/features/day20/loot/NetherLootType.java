
package me.difficulty.features.day20.loot;

import org.bukkit.NamespacedKey;

public enum NetherLootType {

    FORTRESS("nether_bridge", 2),

    BASTION_OTHER("bastion_other", 3),

    BASTION_BRIDGE("bastion_bridge", 3),

    BASTION_STABLE("bastion_hoglin_stable", 4),

    BASTION_TREASURE("bastion_treasure", 5);

    private final String tableName;
    private final int minimumRewards;

    NetherLootType(String tableName, int minimumRewards) {
        this.tableName = tableName;
        this.minimumRewards = minimumRewards;
    }

    public int getMinimumRewards() {
        return minimumRewards;
    }

    /*
     * Identificar la variante a partir de
     * su LootTable original de Minecraft.
     */
    public static NetherLootType fromLootTable(
            NamespacedKey key
    ) {

        if (key == null ||
                !key.getNamespace().equals("minecraft")) {
            return null;
        }

        for (NetherLootType type : values()) {

            if (key.getKey().equals(
                    "chests/" + type.tableName
            )) {
                return type;
            }
        }

        return null;
    }

    /*
     * Recuperar un tipo guardado anteriormente
     * en el marcador persistente del cofre.
     */
    public static NetherLootType fromStoredId(
            String id
    ) {

        if (id == null) {
            return null;
        }

        try {
            return NetherLootType.valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}