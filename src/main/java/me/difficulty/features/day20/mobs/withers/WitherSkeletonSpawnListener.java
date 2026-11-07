
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day20.mobs.FortressLocationUtil;

import org.bukkit.block.Biome;
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
     * GENERACIÓN - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(
            CreatureSpawnEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (event.getEntityType()
                != EntityType.WITHER_SKELETON) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        WitherSkeleton skeleton =
                (WitherSkeleton) event.getEntity();

        if (registry.getMobId(skeleton).isPresent()) {
            return;
        }

        /*
         * ========================================
         * VALLE DE ARENA DE ALMAS
         * 100% WITHER CORRUPTO
         * ========================================
         */
        if (skeleton.getLocation()
                .getBlock()
                .getBiome() == Biome.SOUL_SAND_VALLEY) {

            corrupted.apply(skeleton);
            return;
        }

        /*
         * ========================================
         * OTROS LUGARES
         * ========================================
         */
        if (!FortressLocationUtil.isInFortress(
                skeleton.getLocation()
        )) {
            return;
        }

        /*
         * ========================================
         * FORTALEZAS DEL NETHER
         *
         * 70% Wither Corrupto
         * 30% Guardián de la Fortaleza
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
