
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day20.mobs.FortressLocationUtil;

import org.bukkit.Location;
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

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
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
     * GENERACIÓN DE WITHER SKELETONS - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(
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
         * SOLO WITHER SKELETONS
         * ========================================
         */
        if (event.getEntityType()
                != EntityType.WITHER_SKELETON) {
            return;
        }

        /*
         * ========================================
         * MOTIVOS PERMITIDOS
         * ========================================
         *
         * NATURAL: aparición natural.
         * SPAWNER: generadores.
         *
         * No transformar los creados mediante
         * comandos, huevos o plugins.
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
         * ========================================
         * EVITAR DOBLE ASIGNACIÓN
         * ========================================
         */
        if (registry.getMobId(skeleton).isPresent()) {
            return;
        }

        Location location = skeleton.getLocation();

        /*
         * ========================================
         * FORTALEZAS DEL NETHER
         * ========================================
         *
         * Las fortalezas tienen prioridad.
         *
         * Distribución:
         *
         * Wither Corrupto:        70%
         * Guardián de Fortaleza:  30%
         */
        if (FortressLocationUtil.isInFortress(location)) {

            int roll = ThreadLocalRandom
                    .current()
                    .nextInt(100);

            /*
             * WITHER CORRUPTO: 70%
             */
            if (roll < 70) {

                corrupted.apply(skeleton);
                return;
            }

            /*
             * GUARDIÁN: 30%
             */
            guardian.apply(skeleton);
            return;
        }

        /*
         * ========================================
         * VALLE DE ARENA DE ALMAS
         * ========================================
         *
         * Fuera de fortalezas:
         *
         * 100% Wither Corrupto.
         *
         * El sistema natural adicional
         * transforma esqueletos normales
         * en Wither Corruptos.
         *
         * También transforma cualquier
         * Wither Skeleton natural que
         * aparezca en este bioma.
         */
        if (location.getBlock().getBiome()
                == Biome.SOUL_SAND_VALLEY) {

            corrupted.apply(skeleton);
        }

        /*
         * ========================================
         * OTROS LUGARES
         * ========================================
         *
         * No modificar Wither Skeletons
         * fuera de fortalezas o Valle
         * de Arena de Almas.
         */
    }
}