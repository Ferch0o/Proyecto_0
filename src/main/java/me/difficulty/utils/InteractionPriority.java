package me.difficulty.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.inventory.InventoryHolder;

import java.util.EnumSet;
import java.util.Set;

public final class InteractionPriority {

    private InteractionPriority() {
    }

    /*
     * Bloques especiales que tienen una interacción
     * importante pero no siempre pueden detectarse
     * mediante InventoryHolder, Openable o Switch.
     */
    private static final Set<Material> PRIORITY_BLOCKS =
            EnumSet.of(

                    // Mesas / estaciones
                    Material.CRAFTING_TABLE,
                    Material.ENCHANTING_TABLE,
                    Material.ANVIL,
                    Material.CHIPPED_ANVIL,
                    Material.DAMAGED_ANVIL,
                    Material.GRINDSTONE,
                    Material.SMITHING_TABLE,
                    Material.STONECUTTER,
                    Material.LOOM,
                    Material.CARTOGRAPHY_TABLE,

                    // Otros bloques importantes
                    Material.ENDER_CHEST,
                    Material.BEACON,
                    Material.LECTERN,
                    Material.BELL,
                    Material.JUKEBOX,
                    Material.NOTE_BLOCK,
                    Material.RESPAWN_ANCHOR
            );

    public static boolean shouldBlockHavePriority(
            Block block
    ) {

        if (block == null) {
            return false;
        }

        Material material =
                block.getType();

        /*
         * Cofres, barriles, hornos, hoppers,
         * dispensadores, shulker boxes, etc.
         */
        if (block.getState() instanceof InventoryHolder) {
            return true;
        }

        BlockData blockData =
                block.getBlockData();

        /*
         * Puertas, trampillas y cercas con puerta.
         */
        if (blockData instanceof Openable) {
            return true;
        }

        /*
         * Botones y palancas.
         */
        if (blockData instanceof Switch) {
            return true;
        }

        /*
         * Camas de cualquier color.
         */
        if (Tag.BEDS.isTagged(material)) {
            return true;
        }

        /*
         * Bloques especiales definidos arriba.
         */
        return PRIORITY_BLOCKS.contains(material);
    }
}