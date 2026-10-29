
package me.difficulty.features.day7.gigante;

import com.destroystokyo.paper.entity.Pathfinder;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GiantAiTask implements Runnable {

    private static final double DETECTION_RANGE = 32.0;
    private static final double MAX_HEIGHT_DIFFERENCE = 8.0;
    private static final double CHASE_SPEED = 0.65;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    // Evitar repetir mensajes de depuracion
    private final Set<UUID> reportedGiants = new HashSet<>();

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

                activeGiants.add(giant.getUniqueId());

                Pathfinder pathfinder =
                        giant.getPathfinder();

                // La mecanica se habilita desde el dia 7
                if (!dayManager.isAtLeast(7)) {
                    pathfinder.stopPathfinding();
                    continue;
                }

                Player target = findNearestPlayer(giant);

                if (target == null) {
                    pathfinder.stopPathfinding();
                    continue;
                }

                // Intentar calcular una ruta hacia el jugador
                boolean started = pathfinder.moveTo(
                        target,
                        CHASE_SPEED
                );

                // Registrar solamente el primer resultado
                if (reportedGiants.add(
                        giant.getUniqueId()
                )) {

                    plugin.getLogger().info(
                            "[GiantAI] Navegacion iniciada: "
                                    + started
                                    + " | Objetivo: "
                                    + target.getName()
                    );
                }
            }
        }

        // Limpiar identificadores de entidades descargadas
        reportedGiants.retainAll(activeGiants);
    }

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
}
