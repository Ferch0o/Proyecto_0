package me.difficulty.features.day17.curse;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CursePlayerListener implements Listener {
    private final JavaPlugin plugin;
    private final CurseManager manager;
    private final CurseInventoryService inventory;

    public CursePlayerListener(JavaPlugin plugin, CurseManager manager,
                               CurseInventoryService inventory) {
        this.plugin = plugin;
        this.manager = manager;
        this.inventory = inventory;
    }

    private void syncSoon(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            manager.refresh(player);
            if (!manager.isActive(player)) manager.clearCurseEffects(player);
            inventory.synchronize(player);
        }, 2L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        syncSoon(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        syncSoon(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Los Structure Void no son un botín; la maldición se reconstruye al revivir.
        event.getDrops().removeIf(inventory::isMarker);
    }
}
