
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.ItemIds;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.memory.MemoryKey;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class PiglinGoldListener implements Listener {

    private static final long TRADE_PAUSE_MS = 8_000L;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final NamespacedKey tradePauseKey;
    private final NamespacedKey customItemIdKey;

    public PiglinGoldListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;

        tradePauseKey = new NamespacedKey(
                plugin,
                "corrupted_piglin_trade_pause_until"
        );

        customItemIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );
    }

    /*
     * ========================================
     * ENTREGAR ORO DIRECTAMENTE
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInteract(
            PlayerInteractEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getRightClicked()
                instanceof Piglin piglin)) {
            return;
        }

        ItemStack held = event.getPlayer()
                .getInventory()
                .getItem(event.getHand());

        if (held == null || !isGold(held.getType())) {
            return;
        }

        /*
         * El Oro Elemental y otros ítems
         * custom no son monedas de trueque.
         */
        if (isCustomGold(held)) {
            event.setCancelled(true);
            return;
        }

        /*
         * Solo el Piglin Corrupto
         * puede recibir oro.
         */
        if (registry.is(
                piglin,
                MobIds.CORRUPTED_PIGLIN
        )) {

            if (held.getType() == Material.GOLD_INGOT) {
                pauseAggression(piglin);
            }

            return;
        }

        /*
         * Piglin Explosivo y Rey:
         * rechazar cualquier oro.
         */
        event.setCancelled(true);
    }

    /*
     * ========================================
     * ORO EN EL SUELO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPickup(
            EntityPickupItemEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof Piglin piglin)) {
            return;
        }

        ItemStack item = event.getItem()
                .getItemStack();

        if (!isGold(item.getType())) {
            return;
        }

        /*
         * No recoger materiales personalizados
         * como Oro Elemental.
         */
        if (isCustomGold(item)) {
            event.setCancelled(true);
            return;
        }

        if (registry.is(
                piglin,
                MobIds.CORRUPTED_PIGLIN
        )) {

            if (item.getType() == Material.GOLD_INGOT) {
                pauseAggression(piglin);
            }

            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * FINALIZACIÓN DE TRUEQUES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBarter(PiglinBarterEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Únicamente el Corrupto y solo
         * con lingotes de oro normales.
         */
        if (!registry.is(
                event.getEntity(),
                MobIds.CORRUPTED_PIGLIN
        )
                || event.getInput().getType()
                != Material.GOLD_INGOT
                || isCustomGold(event.getInput())) {

            event.setCancelled(true);
        }
    }

    private boolean isGold(Material material) {

        String name = material.name();

        return name.contains("GOLD")
                || name.startsWith("GILDED_");
    }

    /*
     * Reconocer cualquier ítem personalizado
     * mediante su identificador del plugin.
     */
    private boolean isCustomGold(ItemStack item) {

        ItemMeta meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(
                customItemIdKey,
                PersistentDataType.STRING
        );
    }

    /*
     * Pausa breve para completar
     * el trueque del Piglin Corrupto.
     */
    private void pauseAggression(Piglin piglin) {

        long until =
                System.currentTimeMillis() + TRADE_PAUSE_MS;

        piglin.getPersistentDataContainer().set(
                tradePauseKey,
                PersistentDataType.LONG,
                until
        );

        piglin.setMemory(
                MemoryKey.ANGRY_AT,
                null
        );

        piglin.setTarget(null);
    }

    /*
     * ========================================
     * ORO ELEMENTAL TIRADO AL SUELO
     * ========================================
     *
     * Los mobs no podrán recogerlo.
     * Los jugadores sí podrán recogerlo.
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onItemSpawn(ItemSpawnEvent event) {

        protectElementalGold(event.getEntity());
    }

    /*
     * ========================================
     * ORO ELEMENTAL EN CHUNKS CARGADOS
     * ========================================
     *
     * También protegemos los ítems que ya
     * estaban guardados en el mundo.
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {

        for (Entity entity : event.getEntities()) {

            if (entity instanceof Item item) {
                protectElementalGold(item);
            }
        }
    }

    /*
     * ========================================
     * PROTEGER ORO ELEMENTAL
     * ========================================
     */
    private void protectElementalGold(Item item) {

        ItemStack stack = item.getItemStack();

        ItemMeta meta = stack.getItemMeta();

        if (meta == null) {
            return;
        }

        String itemId = meta.getPersistentDataContainer()
                .get(
                        customItemIdKey,
                        PersistentDataType.STRING
                );

        if (!ItemIds.ELEMENTAL_GOLD.equals(itemId)) {
            return;
        }

        /*
         * Ningún mob puede recogerlo.
         * Los jugadores conservan
         * la posibilidad de recogerlo.
         */
        item.setCanMobPickup(false);
    }
}