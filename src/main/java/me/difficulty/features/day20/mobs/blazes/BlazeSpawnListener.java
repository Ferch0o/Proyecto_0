
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class BlazeSpawnListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

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
            CustomMobRegistry mobRegistry,
            CorruptedBlaze corruptedBlaze,
            PhantomBlaze phantomBlaze,
            BlazeKing blazeKing
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;

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
         * ACTIVACIÓN
         * ========================================
         *
         * Antes del día 20:
         * Blazes vanilla.
         *
         * Desde el día 20:
         * Blazes personalizados.
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
         * MOTIVOS DE SPAWN PERMITIDOS
         * ========================================
         *
         * NATURAL:
         * Aparición natural y spawns extra
         * de nuestro sistema de dificultad.
         *
         * SPAWNER:
         * Aparición desde generadores.
         *
         * No transformar los creados por
         * /summon, huevos o comandos de prueba.
         */
        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        boolean validSpawn =
                reason == CreatureSpawnEvent.SpawnReason.NATURAL
                        || reason == CreatureSpawnEvent.SpawnReason.SPAWNER;

        if (!validSpawn) {
            return;
        }

        Blaze blaze = (Blaze) event.getEntity();

        /*
         * ========================================
         * PROTECCIÓN CONTRA DOBLE ASIGNACIÓN
         * ========================================
         *
         * Si el Blaze ya tiene una identidad
         * personalizada, no modificarla.
         */
        if (mobRegistry.getMobId(blaze).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DISTRIBUCIÓN
         * ========================================
         *
         * 0 - 49: Blaze Corrupto  (50%)
         * 50 - 54: Blaze Fantasmal (5%)
         * 55 - 99: Rey Blaze     (45%)
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        /*
         * ========================================
         * BLAZE CORRUPTO - 50%
         * ========================================
         */
        if (roll < 55) {

            corruptedBlaze.apply(blaze);
            return;
        }

        /*
         * ========================================
         * BLAZE FANTASMAL - 5%
         * ========================================
         */
        if (roll < 70) {

            phantomBlaze.apply(blaze);
            return;
        }

        /*
         * ========================================
         * REY BLAZE - 45%
         * ========================================
         */
        blazeKing.apply(blaze);
    }
}
