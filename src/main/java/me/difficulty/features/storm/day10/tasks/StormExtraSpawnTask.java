
package me.difficulty.features.storm.day10.tasks;

import me.difficulty.DifficultyPlugin;
import me.difficulty.features.storm.core.StormManager;

import org.bukkit.*;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.event.entity.CreatureSpawnEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class StormExtraSpawnTask extends BukkitRunnable {

    private final StormManager stormManager;
    private final NamespacedKey stormExtraMobKey;

    /*
     * ========================================
     * CONFIGURACIÓN DE SPAWNS
     * ========================================
     */

    // 20% por segundo y jugador.
    private static final int EXTRA_SPAWN_CHANCE = 20;

    // Control de mobs cercanos.
    private static final double LOCAL_RADIUS = 24.0;
    private static final int LOCAL_HOSTILE_LIMIT = 30;

    // Radio para buscar un mob original.
    private static final double SOURCE_SEARCH_RADIUS = 32.0;

    // Máximo de mobs extra por mundo.
    private static final int MAX_EXTRA_MOBS_PER_WORLD = 60;

    /*
     * ========================================
     * NUEVAS DISTANCIAS DE APARICIÓN
     * ========================================
     *
     * Los mobs extra aparecerán entre
     * 24 y 40 bloques del jugador.
     */

    private static final double MIN_SPAWN_DISTANCE = 24.0;
    private static final double MAX_SPAWN_DISTANCE = 40.0;

    private static final double MIN_DISTANCE_SQUARED =
            MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;

    private static final double MAX_DISTANCE_SQUARED =
            MAX_SPAWN_DISTANCE * MAX_SPAWN_DISTANCE;

    // Cantidad de posiciones horizontales a probar.
    private static final int LOCATION_ATTEMPTS = 32;

    // Buscar altura cerca del mob de referencia.
    private static final int VERTICAL_SEARCH = 6;


    public StormExtraSpawnTask(
            DifficultyPlugin plugin,
            StormManager stormManager
    ) {

        this.stormManager = stormManager;

        this.stormExtraMobKey = new NamespacedKey(
                plugin,
                "storm_extra_mob"
        );
    }


    /*
     * ========================================
     * EJECUCIÓN CADA SEGUNDO
     * ========================================
     */

    @Override
    public void run() {

        /*
         * Solo cuando la Tormenta está
         * activa y estamos en Día 10+.
         */

        if (!stormManager.hasDay10Effects()) {
            return;
        }

        /*
         * Contador temporal de extras
         * por cada mundo.
         */

        Map<UUID, Long> extraCounts = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {

            tryExtraSpawn(player, extraCounts);
        }
    }


    /*
     * ========================================
     * INTENTO DE SPAWN POR JUGADOR
     * ========================================
     */

    private void tryExtraSpawn(
            Player player,
            Map<UUID, Long> extraCounts
    ) {

        if (player.isDead()) {
            return;
        }

        /*
         * Creative y Spectator
         * no provocan spawns extra.
         */

        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        /*
         * ========================================
         * PROBABILIDAD DEL 20%
         * ========================================
         */

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        if (random.nextInt(100) >= EXTRA_SPAWN_CHANCE) {
            return;
        }

        World world = player.getWorld();

        /*
         * ========================================
         * LÍMITE LOCAL
         * ========================================
         */

        long nearbyHostiles = world.getNearbyEntities(
                        player.getLocation(),
                        LOCAL_RADIUS,
                        LOCAL_RADIUS,
                        LOCAL_RADIUS
                )
                .stream()
                .filter(entity -> entity instanceof Enemy)
                .count();

        if (nearbyHostiles >= LOCAL_HOSTILE_LIMIT) {
            return;
        }

        /*
         * ========================================
         * LÍMITE GLOBAL DE EXTRAS
         * ========================================
         */

        UUID worldId = world.getUID();

        long currentExtraCount =
                extraCounts.computeIfAbsent(
                        worldId,
                        id -> countStormExtraMobs(world)
                );

        if (currentExtraCount >= MAX_EXTRA_MOBS_PER_WORLD) {
            return;
        }

        /*
         * ========================================
         * BUSCAR UN MOB HOSTIL ORIGINAL
         * ========================================
         *
         * Conservamos el sistema anterior:
         *
         * Buscar un enemigo existente
         * para elegir su tipo.
         */

        Entity sourceHostile = findRandomHostile(player);

        if (sourceHostile == null) {
            return;
        }

        EntityType selectedType = sourceHostile.getType();

        /*
         * ========================================
         * BUSCAR UNA POSICIÓN SEGURA
         * ========================================
         *
         * Entre 24 y 40 bloques.
         *
         * No generar cerca de jugadores.
         * No generar dentro de bloques.
         * No cargar chunks nuevos.
         */

        Location spawnLocation = findSpawnLocation(
                player,
                sourceHostile
        );

        if (spawnLocation == null) {
            return;
        }

        /*
         * ========================================
         * GENERAR MOB
         * ========================================
         *
         * NATURAL permite procesarlo con
         * nuestros listeners de Día 10.
         */

        Entity spawnedEntity = world.spawnEntity(
                spawnLocation,
                selectedType,
                CreatureSpawnEvent.SpawnReason.NATURAL,
                entity -> entity.getPersistentDataContainer().set(
                        stormExtraMobKey,
                        PersistentDataType.BYTE,
                        (byte) 1
                )
        );

        // Si otro listener cancela la generacion,
        // no contar este mob como generado.
        if (!spawnedEntity.isValid()) {
            return;
        }

        /*
         * Actualizar el contador temporal.
         */

        extraCounts.put(
                worldId,
                currentExtraCount + 1
        );
    }


    /*
     * ========================================
     * BUSCAR UN ENEMIGO EXISTENTE
     * ========================================
     */

    private Entity findRandomHostile(Player player) {

        World world = player.getWorld();

        List<Entity> candidates = new ArrayList<>();

        for (Entity entity : world.getNearbyEntities(
                player.getLocation(),
                SOURCE_SEARCH_RADIUS,
                SOURCE_SEARCH_RADIUS,
                SOURCE_SEARCH_RADIUS
        )) {

            if (!(entity instanceof Enemy)) {
                continue;
            }

            if (!entity.isValid() || entity.isDead()) {
                continue;
            }

            if (!isAllowedHostile(entity.getType())) {
                continue;
            }

            candidates.add(entity);
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(
                ThreadLocalRandom.current()
                        .nextInt(candidates.size())
        );
    }


    /*
     * ========================================
     * TIPOS EXCLUIDOS
     * ========================================
     *
     * Evitar duplicar bosses y entidades
     * que necesitan condiciones especiales.
     */

    private boolean isAllowedHostile(EntityType type) {

        return switch (type) {

            case WITHER,
                 ENDER_DRAGON,
                 WARDEN,
                 CREAKING,
                 SHULKER,
                 GIANT -> false;

            default -> true;
        };
    }


    /*
     * ========================================
     * NUEVO BUSCADOR DE POSICIONES
     * ========================================
     *
     * Busca un punto entre 24 y 40
     * bloques horizontales del jugador.
     *
     * Utiliza una altura similar a la
     * del mob de referencia.
     */

    private Location findSpawnLocation(
            Player player,
            Entity source
    ) {

        World world = player.getWorld();

        Location playerLocation = player.getLocation();

        int baseY = source.getLocation().getBlockY();

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        for (int attempt = 0;
             attempt < LOCATION_ATTEMPTS;
             attempt++) {

            /*
             * Elegir dirección aleatoria.
             */

            double angle =
                    random.nextDouble(0.0, Math.PI * 2.0);

            /*
             * Distancia aleatoria entre
             * 24 y 40 bloques.
             *
             * La raíz permite repartir
             * los puntos sobre el área.
             */

            double distance = Math.sqrt(
                    random.nextDouble(
                            MIN_DISTANCE_SQUARED,
                            MAX_DISTANCE_SQUARED
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
             * No cargar chunks nuevos.
             */

            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            /*
             * Buscar desde la altura original:
             *
             *  0, +1, -1, +2, -2 ...
             *
             * Hasta 6 bloques de diferencia.
             */

            for (int step = 0;
                 step <= VERTICAL_SEARCH * 2;
                 step++) {

                int offset;

                if (step == 0) {
                    offset = 0;
                } else if (step % 2 == 1) {
                    offset = (step + 1) / 2;
                } else {
                    offset = -(step / 2);
                }

                int y = baseY + offset;

                if (y < world.getMinHeight() + 1
                        || y >= world.getMaxHeight() - 4) {
                    continue;
                }

                Location candidate = new Location(
                        world,
                        x + 0.5,
                        y,
                        z + 0.5
                );

                /*
                 * Respetar borde del mundo.
                 */

                if (!world.getWorldBorder()
                        .isInside(candidate)) {
                    continue;
                }

                /*
                 * Respetar la distancia
                 * exacta de 24-40 bloques
                 * del jugador principal.
                 */

                double distanceSquared =
                        horizontalDistanceSquared(
                                candidate,
                                playerLocation
                        );

                if (distanceSquared < MIN_DISTANCE_SQUARED
                        || distanceSquared > MAX_DISTANCE_SQUARED) {
                    continue;
                }

                /*
                 * Evitar aparecer cerca
                 * de cualquier otro jugador.
                 */

                if (!isFarFromAllPlayers(candidate)) {
                    continue;
                }

                /*
                 * Comprobar terreno y espacio.
                 */

                if (!isValidSpawnLocation(
                        candidate,
                        source
                )) {
                    continue;
                }

                /*
                 * Evitar generar directamente
                 * encima de otros seres vivos.
                 */

                boolean occupied = world.getNearbyEntities(
                                candidate,
                                1.5,
                                2.0,
                                1.5
                        )
                        .stream()
                        .anyMatch(
                                entity ->
                                        entity instanceof LivingEntity
                        );

                if (occupied) {
                    continue;
                }

                /*
                 * Ubicación aceptada.
                 */

                return candidate;
            }
        }

        /*
         * ========================================
         * NO ENCONTRAMOS UBICACIÓN SEGURA
         * ========================================
         *
         * Cancelamos el intento.
         *
         * IMPORTANTE:
         * No usar la posición del mob
         * original como alternativa.
         */

        return null;
    }


    /*
     * ========================================
     * DISTANCIA HORIZONTAL
     * ========================================
     */

    private double horizontalDistanceSquared(
            Location a,
            Location b
    ) {

        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();

        return dx * dx + dz * dz;
    }


    /*
     * ========================================
     * DISTANCIA DE SEGURIDAD
     * ========================================
     *
     * Ningún mob extra puede aparecer
     * a menos de 24 bloques horizontales
     * de otro jugador.
     */

    private boolean isFarFromAllPlayers(
            Location location
    ) {

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        for (Player other : world.getPlayers()) {

            if (other.isDead()) {
                continue;
            }

            double distanceSquared =
                    horizontalDistanceSquared(
                            location,
                            other.getLocation()
                    );

            if (distanceSquared < MIN_DISTANCE_SQUARED) {
                return false;
            }
        }

        return true;
    }


    /*
     * ========================================
     * VALIDAR POSICIÓN DE APARICIÓN
     * ========================================
     *
     * Diferenciamos mobs:
     *
     * - Acuáticos
     * - Voladores
     * - Terrestres
     */

    private boolean isValidSpawnLocation(
            Location location,
            Entity source
    ) {

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        // No permitir spawns extras debajo de la superficie.
        int surfaceY = world.getHighestBlockYAt(
                location.getBlockX(),
                location.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        if (location.getBlockY() <= surfaceY) {
            return false;
        }

        Block feet = location.getBlock();

        Block head = feet.getRelative(BlockFace.UP);

        Block above = head.getRelative(BlockFace.UP);

        Block floor = feet.getRelative(BlockFace.DOWN);

        EntityType type = source.getType();

        /*
         * ========================================
         * MOBS ACUÁTICOS
         * ========================================
         */

        boolean aquatic = switch (type) {

            case GUARDIAN,
                 ELDER_GUARDIAN -> true;

            /*
             * Ahogados:
             * si el original está en agua,
             * se genera también en agua.
             */

            case DROWNED ->
                    source.getLocation()
                            .getBlock()
                            .getType() == Material.WATER;

            default -> false;
        };

        if (aquatic) {

            return feet.getType() == Material.WATER
                    && head.getType() == Material.WATER;
        }

        /*
         * ========================================
         * MOBS VOLADORES
         * ========================================
         */

        boolean flying = switch (type) {

            case GHAST,
                 BLAZE,
                 PHANTOM,
                 VEX -> true;

            default -> false;
        };

        if (flying) {

            if (!feet.getType().isAir()
                    || !head.getType().isAir()
                    || !above.getType().isAir()) {
                return false;
            }

            /*
             * Ghasts necesitan un espacio
             * considerablemente mayor.
             */

            if (type == EntityType.GHAST) {

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = 0; dy <= 3; dy++) {

                            int blockX = feet.getX() + dx;
                            int blockZ = feet.getZ() + dz;

                            /*
                             * Nunca consultar bloques
                             * de chunks no cargados.
                             */

                            if (!world.isChunkLoaded(
                                    blockX >> 4,
                                    blockZ >> 4
                            )) {
                                return false;
                            }

                            Block space = feet.getRelative(
                                    dx,
                                    dy,
                                    dz
                            );

                            if (!space.getType().isAir()) {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        /*
         * ========================================
         * MOBS TERRESTRES
         * ========================================
         *
         * Necesitamos suelo sólido y
         * tres bloques de altura libres.
         */

        if (!feet.getType().isAir()
                || !head.getType().isAir()
                || !above.getType().isAir()) {
            return false;
        }

        if (!floor.getType().isSolid()) {
            return false;
        }

        /*
         * Evitar lugares con demasiada
         * iluminación artificial.
         */

        if (feet.getLightFromBlocks() > 7) {
            return false;
        }

        /*
         * Evitar suelos peligrosos.
         */

        Material floorType = floor.getType();

        return floorType != Material.MAGMA_BLOCK
                && floorType != Material.CAMPFIRE
                && floorType != Material.SOUL_CAMPFIRE;
    }


    /*
     * ========================================
     * CONTAR LOS MOBS EXTRA
     * ========================================
     *
     * Solo entidades creadas por nuestro
     * sistema de Tormenta.
     */

    private long countStormExtraMobs(World world) {

        return world.getEntities()
                .stream()

                .filter(
                        entity ->
                                entity instanceof Enemy
                )

                .filter(
                        entity ->
                                entity.getPersistentDataContainer()
                                        .has(
                                                stormExtraMobKey,
                                                PersistentDataType.BYTE
                                        )
                )

                .count();
    }
}
