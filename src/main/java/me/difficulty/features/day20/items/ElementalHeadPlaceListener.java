
package me.difficulty.features.day20.items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class ElementalHeadPlaceListener
        implements Listener {

    private final ElementalHead elementalHead;

    public ElementalHeadPlaceListener(
            ElementalHead elementalHead
    ) {
        this.elementalHead = elementalHead;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlace(BlockPlaceEvent event) {

        /*
         * SOLO CABEZA ELEMENTAL AUTÉNTICA
         */
        if (elementalHead.isItem(
                event.getItemInHand()
        )) {
            event.setCancelled(true);
        }
    }
}
