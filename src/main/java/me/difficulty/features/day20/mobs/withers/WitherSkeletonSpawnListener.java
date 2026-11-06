
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkeleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class WitherSkeletonSpawnListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedWitherSkeleton corrupted;
    private final FortressGuardian guardian;

    public WitherSkeletonSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedWitherSkeleton corrupted,
            FortressGuardian guardian
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corrupted = corrupted;
        this.guardian = guardian;
    }

    /*
     * ========================================
     * GENERACIÓN DE WITHER SKELETONS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(CreatureSpawnEvent event) {

        /*
         * ACTIVACIÓN DESDE EL DÍA 20
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * SOLO WITHER SKELETON
         */
        if (event.getEntityType()
                != EntityType.WITHER_SKELETON) {
            return;
        }

        /*
         * SPAWNS PERMITIDOS
         */
        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        WitherSkeleton skeleton =
                (WitherSkeleton) event.getEntity();

        /*
         * EVITAR DOBLE ASIGNACIÓN
         */
        if (registry.getMobId(skeleton).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DISTRIBUCIÓN
         *
         * 0 - 69  = Wither Corrupto (70%)
         * 70 - 99 = Guardián (30%)
         * ========================================
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < 70) {
            corrupted.apply(skeleton);
            return;
        }

        guardian.apply(skeleton);
    }
}
