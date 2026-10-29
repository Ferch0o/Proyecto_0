package me.difficulty.features.day7.gigante;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GiantCombatTask implements Runnable {

    // CONFIGURACION DEL COMBATE

    private static final double ATTACK_DAMAGE = 8.0;
    private static final double ATTACK_RANGE = 4.0;
    private static final double MAX_HEIGHT_DIFFERENCE = 3.0;

    // Dos segundos entre ataques
    private static final long ATTACK_COOLDOWN_NANOS =
            2_000_000_000L;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    // Control individual del tiempo entre golpes
    private final Map<UUID, Long> lastAttackTimes =
            new HashMap<>();

    public GiantCombatTask(
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

        // El Gigante solo ataca desde el dia 7
        if (!dayManager.isAtLeast(7)) {
            lastAttackTimes.clear();
            return;
        }

        for (World world : plugin.getServer().getWorlds()) {

            for (Giant giant :
                    world.getEntitiesByClass(Giant.class)) {

                if (!giant.isValid() || giant.isDead()) {
                    continue;
                }

                // Ignorar Gigantes vanilla
                if (!mobRegistry.is(
                        giant,
                        MobIds.GIANT_ZOMBIE
                )) {
                    continue;
                }

                UUID giantId = giant.getUniqueId();

                activeGiants.add(giantId);

                long now = System.nanoTime();

                Long lastAttack =
                        lastAttackTimes.get(giantId);

                // Comprobar tiempo entre golpes
                if (lastAttack != null
                        && now - lastAttack
                        < ATTACK_COOLDOWN_NANOS) {
                    continue;
                }

                // Buscar un jugador al que pueda atacar
                Player target = findAttackTarget(giant);

                if (target == null) {
                    continue;
                }

                // Animacion de ataque, si la entidad la admite
                giant.swingMainHand();

                // Aplicar dano con el Gigante como atacante
                target.damage(
                        ATTACK_DAMAGE,
                        giant
                );

                lastAttackTimes.put(
                        giantId,
                        now
                );
            }
        }

        // Limpiar el registro de Gigantes eliminados
        lastAttackTimes.keySet().retainAll(activeGiants);
    }

    private Player findAttackTarget(Giant giant) {

        Player nearest = null;

        double bestDistance =
                ATTACK_RANGE * ATTACK_RANGE;

        Location giantLocation = giant.getLocation();

        for (Player player :
                giant.getWorld().getPlayers()) {

            if (player.isDead()) {
                continue;
            }

            GameMode gameMode = player.getGameMode();

            // No atacar creativo ni espectador
            if (gameMode != GameMode.SURVIVAL
                    && gameMode != GameMode.ADVENTURE) {
                continue;
            }

            Location playerLocation =
                    player.getLocation();

            // Evitar golpes a jugadores muy arriba o abajo
            double heightDifference = Math.abs(
                    playerLocation.getY()
                            - giantLocation.getY()
            );

            if (heightDifference >
                    MAX_HEIGHT_DIFFERENCE) {
                continue;
            }

            // Calcular distancia horizontal
            double dx = playerLocation.getX()
                    - giantLocation.getX();

            double dz = playerLocation.getZ()
                    - giantLocation.getZ();

            double distanceSquared =
                    dx * dx + dz * dz;

            if (distanceSquared >= bestDistance) {
                continue;
            }

            // Impedir ataques a traves de paredes
            if (!hasClearPath(giant, player)) {
                continue;
            }

            bestDistance = distanceSquared;
            nearest = player;
        }

        return nearest;
    }

    private boolean hasClearPath(
            Giant giant,
            Player player
    ) {

        // Punto aproximado desde donde golpea el Gigante
        Location start = giant.getLocation()
                .clone()
                .add(0, 2.0, 0);

        // Posicion de los ojos del jugador
        Location end = player.getEyeLocation();

        Vector direction = end.toVector()
                .subtract(start.toVector());

        double distance = direction.length();

        if (distance < 0.001) {
            return true;
        }

        direction.normalize();

        // Si el rayo encuentra un bloque, no puede atacar
        return giant.getWorld().rayTraceBlocks(
                start,
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true
        ) == null;
    }
}
