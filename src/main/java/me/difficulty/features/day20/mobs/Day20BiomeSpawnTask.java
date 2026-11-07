
package me.difficulty.features.day20.mobs;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public final class Day20BiomeSpawnTask
        extends BukkitRunnable {

    /*
     * ========================================
     * CONFIGURACIÓN PROVISIONAL
     * ========================================
     *
     * Se ejecuta cada 10 segundos,
     * mediante el registro en Bootstrap.
     *
     * 20% = probabilidad de INTENTO.
     * 100% = variante corrupta elegida
     *        cuando el spawn tiene éxito.
     */
    private static final int SPAWN_CHANCE = 20;

    private static final int MIN_DISTANCE = 24;
    private static final int MAX_DISTANCE = 40;

    private static final int LOCATION_ATTEMPTS = 32;
    private static final int VERTICAL_SEARCH = 8;

    private static final int MAX_NEARBY_HOSTILES = 30;
    private static final int MAX_NEARBY_SAME_TYPE = 6;

    private final DayManager dayManager;

    private final CustomMob corruptedBlaze;
    private final CustomMob corruptedWither;

    public Day20BiomeSpawnTask(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {

        this.dayManager = dayManager;

        this.corruptedBlaze = registry.get(
                MobIds.CORRUPTED_BLAZE
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Blaze Corrupto no registrado."
                )
        );

        this.corruptedWither = registry.get(
                MobIds.CORRUPTED_WITHER_SKELETON
        ).orElseThrow(
                () -> new IllegalStateException(
                        "Wither Corrupto no registrado."
                )
        );
    }

    /*
     * ========================================
     * TAREA DE GENERACIÓN
     * ========================================
     */
    @Override
    public void run() {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.isDead()
                    || player.getGameMode()
                    == GameMode.CREATIVE
                    || player.getGameMode()
                    == GameMode.SPECTATOR) {
                continue;
            }

            World world = player.getWorld();

            if (world.getEnvironment()
                    != World.Environment.NETHER) {
                continue;
            }

            Biome biome = player.getLocation()
                    .getBlock()
                    .getBiome();

            boolean basalt =
                    biome == Biome.BASALT_DELTAS;

            boolean souls =
                    biome == Biome.SOUL_SAND_VALLEY;

            if (!basalt && !souls) {
                continue;
            }

            /*
             * Probabilidad provisional
             * de un intento de generación.
             */
            if (ThreadLocalRandom.current()
                    .nextInt(100) >= SPAWN_CHANCE) {
                continue;
            }

            /*
             * Respetar un límite razonable
             * de enemigos alrededor del jugador.
             */
            long hostiles = player.getNearbyEntities(
                            24.0, 24.0, 24.0
                    ).stream()
                    .filter(e -> e instanceof Enemy)
                    .count();

            if (hostiles >= MAX_NEARBY_HOSTILES) {
                continue;
            }

            /*
             * Evitar acumular demasiados
             * enemigos del mismo tipo.
             */
            long sameType = player.getNearbyEntities(
                            40.0, 20.0, 40.0
                    ).stream()
                    .filter(entity ->
                            basalt
                                    ? entity instanceof Blaze
                                    : entity instanceof WitherSkeleton
                    )
                    .count();

            if (sameType >= MAX_NEARBY_SAME_TYPE) {
                continue;
            }

            Location location = findSpawnLocation(
                    player,
                    biome,
                    basalt
            );

            if (location == null) {
                continue;
            }

            /*
             * ========================================
             * DELTAS DE BASALTO
             * ========================================
             *
             * Siempre Blaze Corrupto.
             */
            if (basalt) {

                world.spawn(
                        location,
                        Blaze.class,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        true,
                        blaze -> corruptedBlaze.apply(blaze)
                );

                continue;
            }

            /*
             * ========================================
             * VALLE DE ARENA DE ALMAS
             * ========================================
             *
             * Siempre Wither Corrupto.
             */
            world.spawn(
                    location,
                    WitherSkeleton.class,
                    CreatureSpawnEvent.SpawnReason.CUSTOM,
                    true,
                    skeleton -> corruptedWither.apply(skeleton)
            );
        }
    }

    /*
     * ========================================
     * BUSCAR POSICIÓN SEGURA
     * ========================================
     */
    private Location findSpawnLocation(
            Player player,
            Biome requiredBiome,
            boolean flying
    ) {

        World world = player.getWorld();
        Location playerLocation = player.getLocation();

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        double minSquared =
                MIN_DISTANCE * MIN_DISTANCE;

        double maxSquared =
                MAX_DISTANCE * MAX_DISTANCE;

        for (int attempt = 0;
             attempt < LOCATION_ATTEMPTS;
             attempt++) {

            double angle = random.nextDouble(
                    0.0, Math.PI * 2.0
            );

            double distance = Math.sqrt(
                    random.nextDouble(
                            minSquared,
                            maxSquared
                    )
            );

            int x = (int) Math.floor(
                    playerLocation.getX()
                            + Math.cos(angle) * distance
            );

            int z = (int) Math.floor(
                    playerLocation.getZ()
                            + Math.sin(angle) * distance
            );

            /*
             * No cargar nuevos chunks.
             */
            if (!world.isChunkLoaded(
                    x >> 4, z >> 4
            )) {
                continue;
            }

            /*
             * Buscar posiciones cerca
             * de la altura del jugador.
             */
            for (int offset = -VERTICAL_SEARCH;
                 offset <= VERTICAL_SEARCH;
                 offset++) {

                int y = playerLocation.getBlockY()
                        + offset;

                if (y < world.getMinHeight() + 2
                        || y > world.getMaxHeight() - 4) {
                    continue;
                }

                if (world.getBiome(x, y, z)
                        != requiredBiome) {
                    continue;
                }

                Location candidate = new Location(
                        world,
                        x + 0.5,
                        y,
                        z + 0.5
                );

                if (!world.getWorldBorder()
                        .isInside(candidate)) {
                    continue;
                }

                if (!isFarFromAllPlayers(candidate)) {
                    continue;
                }

                if (!hasSafeSpace(candidate, flying)) {
                    continue;
                }

                /*
                 * No generar el nuevo mob
                 * encima de otro ser vivo.
                 */
                boolean occupied = world.getNearbyEntities(
                                candidate,
                                1.5,
                                3.0,
                                1.5
                        ).stream()
                        .anyMatch(e ->
                                e instanceof LivingEntity
                        );

                if (occupied) {
                    continue;
                }

                return candidate;
            }
        }

        /*
         * Si no existe un punto seguro,
         * descartamos el intento.
         */
        return null;
    }

    /*
     * ========================================
     * DISTANCIA DE SEGURIDAD
     * ========================================
     */
    private boolean isFarFromAllPlayers(
            Location candidate
    ) {

        World world = candidate.getWorld();

        if (world == null) {
            return false;
        }

        for (Player other : world.getPlayers()) {

            if (other.isDead()) {
                continue;
            }

            double dx = candidate.getX()
                    - other.getLocation().getX();

            double dz = candidate.getZ()
                    - other.getLocation().getZ();

            double distanceSquared =
                    dx * dx + dz * dz;

            if (distanceSquared
                    < MIN_DISTANCE * MIN_DISTANCE) {
                return false;
            }
        }

        return true;
    }

    /*
     * ========================================
     * VALIDAR ESPACIO DEL MOB
     * ========================================
     */
    private boolean hasSafeSpace(
            Location location,
            boolean flying
    ) {

        Block feet = location.getBlock();

        Block head = feet.getRelative(0, 1, 0);
        Block above = feet.getRelative(0, 2, 0);

        if (!feet.getType().isAir()
                || !head.getType().isAir()
                || !above.getType().isAir()) {
            return false;
        }

        /*
         * Blaze: puede aparecer flotando
         * en una zona despejada.
         */
        if (flying) {
            return true;
        }

        /*
         * Wither Skeleton:
         * necesita suelo sólido.
         */
        Block floor = feet.getRelative(0, -1, 0);

        Material material = floor.getType();

        if (!material.isSolid()) {
            return false;
        }

        /*
         * Evitar superficies peligrosas.
         */
        return material != Material.MAGMA_BLOCK
                && material != Material.CAMPFIRE
                && material != Material.SOUL_CAMPFIRE;
    }
}
