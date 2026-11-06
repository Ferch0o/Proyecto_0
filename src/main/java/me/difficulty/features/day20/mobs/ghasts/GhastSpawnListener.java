
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class GhastSpawnListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedGhast corrupted;
    private final DemonicGhast demonic;

    public GhastSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedGhast corrupted,
            DemonicGhast demonic
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corrupted = corrupted;
        this.demonic = demonic;
    }

    /*
     * ========================================
     * APARICIÓN DE GHASTS - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onGhastSpawn(
            CreatureSpawnEvent event
    ) {

        /*
         * SOLO DESDE EL DÍA 20
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * SOLO GHASTS
         */
        if (event.getEntityType() != EntityType.GHAST) {
            return;
        }

        /*
         * MOTIVOS DE APARICIÓN PERMITIDOS
         */
        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Ghast ghast = (Ghast) event.getEntity();

        /*
         * EVITAR ASIGNAR DOS VARIANTES
         */
        if (registry.getMobId(ghast).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DISTRIBUCIÓN
         *
         * 60% GHAST CORRUPTO
         * 40% GHAST DEMONÍACO
         * ========================================
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < 60) {
            corrupted.apply(ghast);
            return;
        }

        demonic.apply(ghast);
    }
}
