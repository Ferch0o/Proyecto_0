
package me.difficulty.features.day7.gigante;

import com.destroystokyo.paper.entity.Pathfinder;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class GiantAiTask implements Runnable {

    // ========================================
    // CONFIGURACION DE PERSECUCION
    // ========================================

    private static final double DETECTION_RANGE = 32.0;
    private static final double MAX_HEIGHT_DIFFERENCE = 16.0;
    private static final double CHASE_SPEED = 0.65;

    // ========================================
    // CONFIGURACION DE EXPLORACION
    // ========================================

    private static final double WANDER_SPEED = 0.45;

    private static final double MIN_WANDER_DISTANCE = 5.0;
    private static final double MAX_WANDER_DISTANCE = 12.0;

    private static final int WANDER_ATTEMPTS = 8;

    // Espacio suficiente para el cuerpo del Gigante
    private static final double HALF_WIDTH = 1.8;
    private static final int REQUIRED_HEIGHT = 13;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    // Estado de exploracion independiente por Gigante
    private final Map<UUID, WanderState> wanderStates =
            new HashMap<>();

    // Evitar mensajes repetidos de depuracion
    private final Set<UUID> reportedGiants =
            new HashSet<>();

    // La tarea se ejecuta una vez por segundo
    private long updateCounter = 0L;

    public GiantAiTask(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
    }

    @Override
    public void run() {

        updateCounter++;

        Set<UUID> activeGiants = new HashSet<>();

        for (World world : plugin.getServer().getWorlds()) {

            for (Giant giant :
                    world.getEntitiesByClass(Giant.class)) {

                if (!giant.isValid() || giant.isDead()) {
                    continue;
                }

                // Solo nuestros Gigantes personalizados
                if (!mobRegistry.is(
                        giant,
                        MobIds.GIANT_ZOMBIE
                )) {
                    continue;
                }

                UUID giantId = giant.getUniqueId();
                activeGiants.add(giantId);

                Pathfinder pathfinder =
                        giant.getPathfinder();

                // Solo desde el dia 7
                if (!dayManager.isAtLeast(7)) {
                    pathfinder.stopPathfinding();
                    wanderStates.remove(giantId);
                    continue;
                }

                WanderState state =
                        wanderStates.computeIfAbsent(
                                giantId,
                                id -> new WanderState(
                                        updateCounter
                                                + randomSeconds(2, 4)
                                )
                        );

                // ========================================
                // BUSCAR JUGADOR
                // ========================================

                Player target = findNearestPlayer(giant);

                if (target != null) {

                    // Abandonar exploracion inmediatamente
                    state.chasing = true;
                    state.wandering = false;

                    // Mantener la persecucion original
                    boolean started = pathfinder.moveTo(
                            target,
                            CHASE_SPEED
                    );

                    if (reportedGiants.add(giantId)) {
                        plugin.getLogger().info(
                                "[GiantAI] Navegacion iniciada: "
                                        + started
                                        + " | Objetivo: "
                                        + target.getName()
                        );
                    }

                    continue;
                }

                // ========================================
                // PERDIO AL JUGADOR
                // ========================================

                if (state.chasing) {

                    pathfinder.stopPathfinding();

                    state.chasing = false;
                    state.wandering = false;

                    // Descansar antes de explorar
                    state.nextActionUpdate =
                            updateCounter
                                    + randomSeconds(2, 4);

                    continue;
                }

                // ========================================
                // EXPLORACION NATURAL
                // ========================================

                handleWandering(
                        giant,
                        pathfinder,
                        state
                );
            }
        }

        // Limpiar datos de entidades descargadas
        wanderStates.keySet().retainAll(activeGiants);
        reportedGiants.retainAll(activeGiants);
    }

    // ========================================
    // MOVIMIENTO ALEATORIO
    // ========================================

    private void handleWandering(
            Giant giant,
            Pathfinder pathfinder,
            WanderState state
    ) {

        // Ya esta caminando hacia un destino
        if (state.wandering) {

            boolean finished = !pathfinder.hasPath();

            boolean timeExpired =
                    updateCounter >= state.nextActionUpdate;

            if (finished || timeExpired) {

                pathfinder.stopPathfinding();

                state.wandering = false;

                // Descanso entre recorridos
                state.nextActionUpdate =
                        updateCounter
                                + randomSeconds(2, 4);
            }

            return;
        }

        // Todavia esta descansando
        if (updateCounter < state.nextActionUpdate) {
            return;
        }

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        // 25% de probabilidad de descansar un poco mas
        if (random.nextInt(100) < 25) {

            state.nextActionUpdate =
                    updateCounter
                            + randomSeconds(2, 4);

            return;
        }

        // Buscar una posicion aleatoria segura
        Location destination =
                findWanderDestination(giant);

        if (destination == null) {

            state.nextActionUpdate =
                    updateCounter
                            + randomSeconds(2, 4);

            return;
        }

        // Iniciar desplazamiento natural
        boolean started = pathfinder.moveTo(
                destination,
                WANDER_SPEED
        );

        if (started) {

            state.wandering = true;

            // Mantener el recorrido de 6 a 12 segundos
            state.nextActionUpdate =
                    updateCounter
                            + randomSeconds(6, 12);

        } else {

            // Intentar otra direccion mas adelante
            state.wandering = false;

            state.nextActionUpdate =
                    updateCounter
                            + randomSeconds(2, 4);
        }
    }

    // ========================================
    // BUSCAR DESTINO ALEATORIO
    // ========================================

    private Location findWanderDestination(Giant giant) {

        World world = giant.getWorld();
        Location origin = giant.getLocation();

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        // No intentar explorar si esta bajo tierra
        int originSurfaceY = world.getHighestBlockYAt(
                origin.getBlockX(),
                origin.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        if (origin.getBlockY() < originSurfaceY) {
            return null;
        }

        for (int attempt = 0;
             attempt < WANDER_ATTEMPTS;
             attempt++) {

            double angle =
                    random.nextDouble(0, Math.PI * 2);

            double distance = random.nextDouble(
                    MIN_WANDER_DISTANCE,
                    MAX_WANDER_DISTANCE
            );

            int x = (int) Math.floor(
                    origin.getX()
                            + Math.cos(angle) * distance
            );

            int z = (int) Math.floor(
                    origin.getZ()
                            + Math.sin(angle) * distance
            );

            // No cargar chunks nuevos
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            // Obtener superficie natural
            int surfaceY = world.getHighestBlockYAt(
                    x,
                    z,
                    HeightMap.MOTION_BLOCKING_NO_LEAVES
            );

            int feetY = surfaceY + 1;

            // Evitar desniveles demasiado grandes
            if (Math.abs(
                    feetY - origin.getBlockY()
            ) > 2) {
                continue;
            }

            Location candidate = new Location(
                    world,
                    x + 0.5,
                    feetY,
                    z + 0.5
            );

            if (!isSafeWanderLocation(candidate)) {
                continue;
            }

            return candidate;
        }

        // Ningun destino valido
        return null;
    }

    // ========================================
    // VALIDAR TERRENO Y ESPACIO
    // ========================================

    private boolean isSafeWanderLocation(
            Location location
    ) {

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        int feetY = location.getBlockY();

        if (feetY <= world.getMinHeight()
                || feetY + REQUIRED_HEIGHT
                >= world.getMaxHeight()) {
            return false;
        }

        if (!world.getWorldBorder().isInside(location)) {
            return false;
        }

        // Suelo del destino
        Block floor = world.getBlockAt(
                location.getBlockX(),
                feetY - 1,
                location.getBlockZ()
        );

        Material floorType = floor.getType();

        if (!floorType.isSolid()) {
            return false;
        }

        // Evitar hojas y bloques peligrosos
        if (Tag.LEAVES.isTagged(floorType)
                || floorType == Material.MAGMA_BLOCK
                || floorType == Material.CAMPFIRE
                || floorType == Material.SOUL_CAMPFIRE
                || floorType == Material.CACTUS) {
            return false;
        }

        int minX = (int) Math.floor(
                location.getX() - HALF_WIDTH
        );

        int maxX = (int) Math.floor(
                location.getX() + HALF_WIDTH
        );

        int minZ = (int) Math.floor(
                location.getZ() - HALF_WIDTH
        );

        int maxZ = (int) Math.floor(
                location.getZ() + HALF_WIDTH
        );

        // Comprobar espacio para el Gigante
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                if (!world.isChunkLoaded(
                        x >> 4,
                        z >> 4
                )) {
                    return false;
                }

                for (int y = feetY;
                     y < feetY + REQUIRED_HEIGHT;
                     y++) {

                    Block block = world.getBlockAt(
                            x,
                            y,
                            z
                    );

                    if (!block.isPassable()
                            || block.isLiquid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // ========================================
    // DETECTAR JUGADORES
    // ========================================

    private Player findNearestPlayer(Giant giant) {

        Player nearest = null;

        double bestDistance =
                DETECTION_RANGE * DETECTION_RANGE;

        for (Player player :
                giant.getWorld().getPlayers()) {

            if (player.isDead()) {
                continue;
            }

            GameMode mode = player.getGameMode();

            // Ignorar creativo y espectador
            if (mode != GameMode.SURVIVAL
                    && mode != GameMode.ADVENTURE) {
                continue;
            }

            double heightDifference = Math.abs(
                    player.getLocation().getY()
                            - giant.getLocation().getY()
            );

            if (heightDifference >
                    MAX_HEIGHT_DIFFERENCE) {
                continue;
            }

            double distance = giant.getLocation()
                    .distanceSquared(player.getLocation());

            if (distance < bestDistance) {

                bestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    // ========================================
    // TIEMPOS ALEATORIOS
    // ========================================

    private int randomSeconds(int min, int max) {

        return ThreadLocalRandom.current()
                .nextInt(min, max + 1);
    }

    // ========================================
    // ESTADO INDIVIDUAL DE CADA GIGANTE
    // ========================================

    private static final class WanderState {

        private boolean chasing = false;
        private boolean wandering = false;

        private long nextActionUpdate;

        private WanderState(long nextActionUpdate) {
            this.nextActionUpdate = nextActionUpdate;
        }
    }
}