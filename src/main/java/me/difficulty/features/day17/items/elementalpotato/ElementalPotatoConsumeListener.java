package me.difficulty.features.day17.items.elementalpotato;

import me.difficulty.features.day17.curse.CurseManager;
import me.difficulty.features.day17.curse.CurseInventoryService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalPotatoConsumeListener implements Listener {
    private final JavaPlugin plugin;
    private final CurseManager manager;
    private final CurseInventoryService inventory;
    private final ElementalPotato potato;

    public ElementalPotatoConsumeListener(JavaPlugin plugin, CurseManager manager,
                                         CurseInventoryService inventory, ElementalPotato potato) {
        this.plugin = plugin;
        this.manager = manager;
        this.inventory = inventory;
        this.potato = potato;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!potato.isElementalPotato(event.getItem())) return;
        Player player = event.getPlayer();
        if (!manager.isActive(player) || !manager.getState(player).canBeCured()) {
            event.setCancelled(true);
            player.sendMessage("§cNo puedes consumir la Patata Elemental: no tienes una maldición curable.");
            return;
        }
        if (!manager.cure(player)) {
            event.setCancelled(true);
            return;
        }
        manager.clearCurseEffects(player);
        // El evento consume la patata de forma vanilla. No descontarla manualmente.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) inventory.synchronize(player);
        });
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        player.sendMessage("§a¡Maldición eliminada! Tu vida máxima ahora es de 12 corazones.");
    }
}
