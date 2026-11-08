
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.memory.MemoryKey;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class PiglinAggressionTask
        extends BukkitRunnable {

    private static final double RANGE = 24.0;
    private static final double VERTICAL_RANGE = 16.0;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final NamespacedKey tradePauseKey;

    public PiglinAggressionTask(
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
    }

    @Override
    public void run() {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Encontrar el jugador más cercano
         * para cada Piglin.
         */
        Map<Piglin, Player> targets = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!isValidPlayer(player)) {
                continue;
            }

            for (Entity entity : player.getNearbyEntities(
                    RANGE,
                    VERTICAL_RANGE,
                    RANGE
            )) {

                if (!(entity instanceof Piglin piglin)) {
                    continue;
                }

                if (!piglin.isValid()
                        || piglin.isDead()
                        || !piglin.hasAI()) {
                    continue;
                }

                double distance = piglin
                        .getLocation()
                        .distanceSquared(
                                player.getLocation()
                        );

                if (distance > RANGE * RANGE) {
                    continue;
                }

                Player previous = targets.get(piglin);

                if (previous == null
                        || distance < piglin.getLocation()
                        .distanceSquared(
                                previous.getLocation()
                        )) {

                    targets.put(piglin, player);
                }
            }
        }

        /*
         * Aplicar agresividad.
         */
        for (Map.Entry<Piglin, Player> entry
                : targets.entrySet()) {

            Piglin piglin = entry.getKey();
            Player player = entry.getValue();

            boolean corrupted = registry.is(
                    piglin,
                    MobIds.CORRUPTED_PIGLIN
            );

            /*
             * El Corrupto puede detenerse
             * brevemente para tradear.
             */
            if (corrupted && isTrading(piglin)) {
                continue;
            }

            if (!corrupted) {

                /*
                 * El resto ignora el oro.
                 */
                piglin.setMemory(
                        MemoryKey.ADMIRING_DISABLED,
                        Boolean.TRUE
                );

                piglin.setMemory(
                        MemoryKey.DISABLE_WALK_TO_ADMIRE_ITEM,
                        Boolean.TRUE
                );
            }

            /*
             * Memoria de enfado:
             * permite seguir siendo hostil
             * ante jugadores con armadura
             * de oro.
             */
            piglin.setMemory(
                    MemoryKey.ANGRY_AT,
                    player.getUniqueId()
            );

            if (piglin.getTarget() != player) {
                piglin.setTarget(player);
            }
        }
    }

    private boolean isTrading(Piglin piglin) {

        if (Boolean.TRUE.equals(
                piglin.getMemory(
                        MemoryKey.ADMIRING_ITEM
                )
        )) {
            return true;
        }

        long until = piglin.getPersistentDataContainer()
                .getOrDefault(
                        tradePauseKey,
                        PersistentDataType.LONG,
                        0L
                );

        return System.currentTimeMillis() < until;
    }

    private boolean isValidPlayer(Player player) {

        return player.isOnline()
                && !player.isDead()
                && player.getGameMode()
                != GameMode.CREATIVE
                && player.getGameMode()
                != GameMode.SPECTATOR;
    }
}