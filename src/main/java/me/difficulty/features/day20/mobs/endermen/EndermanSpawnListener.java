
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class EndermanSpawnListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;
    private final CorruptedEnderman corrupted;
    private final AncestralEnderman ancestral;

    public EndermanSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedEnderman corrupted,
            AncestralEnderman ancestral
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corrupted = corrupted;
        this.ancestral = ancestral;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(CreatureSpawnEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (event.getEntityType() != EntityType.ENDERMAN) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Enderman enderman = (Enderman) event.getEntity();

        if (registry.getMobId(enderman).isPresent()) {
            return;
        }

        /*
         * 0–69:  Enderman Corrupto  (70%)
         * 70–99: Enderman Ancestral (30%)
         */
        int roll = ThreadLocalRandom.current()
                .nextInt(100);

        if (roll < 70) {
            corrupted.apply(enderman);
        } else {
            ancestral.apply(enderman);
        }
    }
}