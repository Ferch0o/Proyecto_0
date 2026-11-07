
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day20.mobs.FortressLocationUtil;

import org.bukkit.block.Biome;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class BlazeSpawnListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedBlaze corruptedBlaze;
    private final PhantomBlaze phantomBlaze;
    private final BlazeKing blazeKing;

    public BlazeSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedBlaze corruptedBlaze,
            PhantomBlaze phantomBlaze,
            BlazeKing blazeKing
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corruptedBlaze = corruptedBlaze;
        this.phantomBlaze = phantomBlaze;
        this.blazeKing = blazeKing;
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
    public void onBlazeSpawn(
            CreatureSpawnEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (event.getEntityType() != EntityType.BLAZE) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Blaze blaze = (Blaze) event.getEntity();

        if (registry.getMobId(blaze).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DELTAS DE BASALTO - 100% CORRUPTO
         * ========================================
         *
         * El bioma tiene prioridad sobre
         * la distribución de las fortalezas.
         */
        if (blaze.getLocation()
                .getBlock()
                .getBiome() == Biome.BASALT_DELTAS) {

            corruptedBlaze.apply(blaze);
            return;
        }

        /*
         * ========================================
         * FUERA DE FORTALEZAS
         * ========================================
         *
         * No transformar otros Blazes
         * aparecidos fuera de los lugares
         * contemplados en el documento.
         */
        if (!FortressLocationUtil.isInFortress(
                blaze.getLocation()
        )) {
            return;
        }

        /*
         * ========================================
         * FORTALEZAS DEL NETHER
         *
         * 55% Blaze Corrupto
         * 10% Blaze Fantasmal
         * 35% Rey Blaze
         * ========================================
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < 55) {
            corruptedBlaze.apply(blaze);
            return;
        }

        if (roll < 65) {
            phantomBlaze.apply(blaze);
            return;
        }

        blazeKing.apply(blaze);
    }
}
