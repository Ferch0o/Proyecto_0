
package me.difficulty.features.day7.corruptedstar;

import io.papermc.paper.event.player.PlayerItemGroupCooldownEvent;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CorruptedStarCooldownPersistenceListener
        implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey cooldownGroup;
    private final NamespacedKey cooldownEndKey;

    private final Set<UUID> restoring = new HashSet<>();

    public CorruptedStarCooldownPersistenceListener(
            JavaPlugin plugin
    ) {
        this.plugin = plugin;

        // Debe coincidir con el grupo del item.
        this.cooldownGroup = new NamespacedKey(
                "difficultyplugin",
                "corrupted_star"
        );

        this.cooldownEndKey = new NamespacedKey(
                plugin,
                "corrupted_star_cooldown_end_ms"
        );
    }

    // Guardar cuándo termina el cooldown.
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onCooldown(PlayerItemGroupCooldownEvent event) {

        if (!cooldownGroup.equals(event.getCooldownGroup())) {
            return;
        }

        Player player = event.getPlayer();

        if (restoring.contains(player.getUniqueId())) {
            return;
        }

        PersistentDataContainer data =
                player.getPersistentDataContainer();

        int ticks = event.getCooldown();

        if (ticks <= 0) {
            data.remove(cooldownEndKey);
            return;
        }

        long endTime =
                System.currentTimeMillis() + ticks * 50L;

        data.set(
                cooldownEndKey,
                PersistentDataType.LONG,
                endTime
        );
    }

    // Restaurar el tiempo restante al reconectarse.
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!player.isOnline()) {
                return;
            }

            PersistentDataContainer data =
                    player.getPersistentDataContainer();

            Long endTime = data.get(
                    cooldownEndKey,
                    PersistentDataType.LONG
            );

            if (endTime == null) {
                return;
            }

            long remaining =
                    endTime - System.currentTimeMillis();

            if (remaining <= 0) {
                data.remove(cooldownEndKey);
                return;
            }

            int ticks = (int) Math.min(
                    Integer.MAX_VALUE,
                    (remaining + 49L) / 50L
            );

            UUID uuid = player.getUniqueId();

            restoring.add(uuid);

            try {
                player.setCooldown(cooldownGroup, ticks);
            } finally {
                restoring.remove(uuid);
            }
        });
    }
}
