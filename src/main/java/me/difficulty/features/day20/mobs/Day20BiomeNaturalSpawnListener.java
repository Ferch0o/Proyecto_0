
package me.difficulty.features.day20.mobs;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkeleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class Day20BiomeNaturalSpawnListener
        implements Listener {

    private static final int WITHER_REPLACE_CHANCE = 50;
    private static final int BLAZE_REPLACE_CHANCE = 50;

    private static final int MAX_NEARBY_CORRUPTED = 15;

    private static final double HORIZONTAL_RADIUS = 48.0;
    private static final double VERTICAL_RADIUS = 24.0;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CustomMob corruptedBlaze;
    private final CustomMob corruptedWither;

    private final NamespacedKey stormExtraMobKey;

    public Day20BiomeNaturalSpawnListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry registry
    ) {

        this.dayManager = dayManager;
        this.registry = registry;

        corruptedBlaze = registry.get(
                MobIds.CORRUPTED_BLAZE
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Blaze Corrupto no registrado."
                )
        );

        corruptedWither = registry.get(
                MobIds.CORRUPTED_WITHER_SKELETON
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Wither Corrupto no registrado."
                )
        );

        stormExtraMobKey = new NamespacedKey(
                plugin,
                "storm_extra_mob"
        );
    }

    /*
     * ========================================
     * REEMPLAZO DE SPAWNS NATURALES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onNaturalSpawn(
            CreatureSpawnEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo generación natural.
         */
        if (event.getSpawnReason()
                != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        /*
         * No reemplazar los mobs extras
         * creados por nuestro sistema
         * de Tormenta.
         */
        if (event.getEntity()
                .getPersistentDataContainer()
                .has(
                        stormExtraMobKey,
                        PersistentDataType.BYTE
                )) {
            return;
        }

        EntityType type = event.getEntityType();

        if (type != EntityType.SKELETON
                && type != EntityType.MAGMA_CUBE) {
            return;
        }

        Location location = event.getLocation().clone();
        World world = location.getWorld();

        if (world == null
                || world.getEnvironment()
                != World.Environment.NETHER) {
            return;
        }

        Biome biome = location.getBlock().getBiome();

        boolean spawnWither =
                type == EntityType.SKELETON
                        && biome == Biome.SOUL_SAND_VALLEY;

        boolean spawnBlaze =
                type == EntityType.MAGMA_CUBE
                        && biome == Biome.BASALT_DELTAS;

        if (!spawnWither && !spawnBlaze) {
            return;
        }

        /*
         * ========================================
         * LAS FORTALEZAS TIENEN PRIORIDAD
         * ========================================
         */
        if (FortressLocationUtil.isInFortress(
                location
        )) {
            return;
        }

        /*
         * ========================================
         * SORTEO DE REEMPLAZO
         * ========================================
         *
         * 10% de los Skeletons naturales
         * se convierten en Wither Corrupto.
         *
         * 10% de los Magma Cubes naturales
         * se convierten en Blaze Corrupto.
         *
         * El 90% restante continúa con
         * el comportamiento habitual.
         */
        int chance = spawnWither
                ? WITHER_REPLACE_CHANCE
                : BLAZE_REPLACE_CHANCE;

        if (ThreadLocalRandom.current()
                .nextInt(100) >= chance) {
            return;
        }

        /*
         * ========================================
         * SEGURIDAD DEL SPAWN
         * ========================================
         */
        if (!hasEnoughSpace(
                location,
                spawnWither
        )) {
            return;
        }

        /*
         * ========================================
         * LÍMITE LOCAL DE CORRUPTOS
         * ========================================
         *
         * No reemplazar más mobs cuando
         * ya existen cinco variantes
         * corruptas cercanas.
         *
         * El mob vanilla sigue apareciendo.
         */
        if (countNearbyCorrupted(
                location,
                spawnWither
        ) >= MAX_NEARBY_CORRUPTED) {
            return;
        }

        /*
         * ========================================
         * VALLE DE ARENA DE ALMAS
         * ========================================
         */
        if (spawnWither) {

            world.spawn(
                    location,
                    WitherSkeleton.class,
                    CreatureSpawnEvent.SpawnReason.CUSTOM,
                    false,
                    wither -> {

                        corruptedWither.apply(wither);

                        /*
                         * Solicitar que pueda
                         * desaparecer al alejarse
                         * de los jugadores.
                         */
                        wither.setRemoveWhenFarAway(true);
                    }
            );

            event.setCancelled(true);
            return;
        }

        /*
         * ========================================
         * DELTAS DE BASALTO
         * ========================================
         */
        world.spawn(
                location,
                Blaze.class,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                false,
                blaze -> {

                    corruptedBlaze.apply(blaze);
                    blaze.setRemoveWhenFarAway(true);
                }
        );

        event.setCancelled(true);
    }

    /*
     * ========================================
     * CONTAR CORRUPTOS CERCANOS
     * ========================================
     */
    private long countNearbyCorrupted(
            Location location,
            boolean wither
    ) {

        World world = location.getWorld();

        if (world == null) {
            return 0L;
        }

        return world.getNearbyEntities(
                        location,
                        HORIZONTAL_RADIUS,
                        VERTICAL_RADIUS,
                        HORIZONTAL_RADIUS
                )
                .stream()
                .filter(entity -> {

                    if (wither) {

                        return entity
                                instanceof WitherSkeleton skeleton
                                && registry.is(
                                skeleton,
                                MobIds.CORRUPTED_WITHER_SKELETON
                        );
                    }

                    return entity instanceof Blaze blaze
                            && registry.is(
                            blaze,
                            MobIds.CORRUPTED_BLAZE
                    );
                })
                .count();
    }

    /*
     * ========================================
     * VERIFICAR ESPACIO LIBRE
     * ========================================
     */
    private boolean hasEnoughSpace(
            Location location,
            boolean wither
    ) {

        if (!location.getBlock()
                .getType().isAir()) {
            return false;
        }

        if (!location.getBlock()
                .getRelative(0, 1, 0)
                .getType().isAir()) {
            return false;
        }

        /*
         * Los Wither Skeletons requieren
         * mayor altura que los Skeletons.
         */
        if (wither) {

            return location.getBlock()
                    .getRelative(0, 2, 0)
                    .getType().isAir();
        }

        return true;
    }
}