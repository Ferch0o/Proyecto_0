
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day20.mobs.FortressLocationUtil;

import org.bukkit.Location;
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

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
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
     * GENERACIÓN DE BLAZES - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlazeSpawn(
            CreatureSpawnEvent event
    ) {

        /*
         * ========================================
         * ACTIVACIÓN DESDE EL DÍA 20
         * ========================================
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * ========================================
         * SOLO BLAZES
         * ========================================
         */
        if (event.getEntityType() != EntityType.BLAZE) {
            return;
        }

        /*
         * ========================================
         * MOTIVOS PERMITIDOS
         * ========================================
         *
         * NATURAL: aparición natural.
         * SPAWNER: generadores de mobs.
         *
         * No transformar los creados mediante:
         * - Comandos.
         * - Huevos.
         * - Plugins con SpawnReason.CUSTOM.
         */
        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        Blaze blaze = (Blaze) event.getEntity();

        /*
         * ========================================
         * EVITAR DOBLE ASIGNACIÓN
         * ========================================
         */
        if (registry.getMobId(blaze).isPresent()) {
            return;
        }

        Location location = blaze.getLocation();

        /*
         * ========================================
         * FORTALEZAS DEL NETHER
         * ========================================
         *
         * Las fortalezas tienen prioridad
         * sobre el bioma donde se encuentran.
         *
         * Distribución:
         *
         * Blaze Corrupto:   55%
         * Blaze Fantasmal:  10%
         * Rey Blaze:        35%
         */
        if (FortressLocationUtil.isInFortress(location)) {

            int roll = ThreadLocalRandom
                    .current()
                    .nextInt(100);

            /*
             * BLAZE CORRUPTO: 55%
             */
            if (roll < 55) {

                corruptedBlaze.apply(blaze);
                return;
            }

            /*
             * BLAZE FANTASMAL: 10%
             */
            if (roll < 65) {

                phantomBlaze.apply(blaze);
                return;
            }

            /*
             * REY BLAZE: 35%
             */
            blazeKing.apply(blaze);
            return;
        }

        /*
         * ========================================
         * DELTAS DE BASALTO
         * ========================================
         *
         * Fuera de fortalezas:
         *
         * 100% Blaze Corrupto.
         *
         * El nuevo sistema natural se encarga
         * de transformar los cubos de magma
         * en Blazes Corruptos.
         *
         * Este bloque también permite
         * transformar un Blaze natural
         * que aparezca en ese bioma.
         */
        if (location.getBlock().getBiome()
                == Biome.BASALT_DELTAS) {

            corruptedBlaze.apply(blaze);
        }

        /*
         * ========================================
         * OTROS LUGARES
         * ========================================
         *
         * No modificar Blazes fuera de
         * fortalezas ni Deltas de Basalto.
         */
    }
}