
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class PigmanSpawnListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedPigman corrupted;
    private final NinjaPigman ninja;

    public PigmanSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedPigman corrupted,
            NinjaPigman ninja
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corrupted = corrupted;
        this.ninja = ninja;
    }

    /*
     * ========================================
     * APARICIÓN - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(CreatureSpawnEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo Piglins zombificados.
         */
        if (event.getEntityType()
                != EntityType.ZOMBIFIED_PIGLIN) {
            return;
        }

        /*
         * Mantener las mismas reglas
         * de aparición que los otros
         * mobs personalizados del día 20.
         */
        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        PigZombie pigman =
                (PigZombie) event.getEntity();

        /*
         * Evitar doble asignación.
         */
        if (registry.getMobId(pigman).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DISTRIBUCIÓN
         *
         * 0 - 49: Pigman Corrupto (50%)
         * 50 - 99: Ninja          (50%)
         * ========================================
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < 50) {
            corrupted.apply(pigman);
            return;
        }

        ninja.apply(pigman);
    }
}
