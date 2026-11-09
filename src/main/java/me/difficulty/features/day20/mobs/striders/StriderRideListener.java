
package me.difficulty.features.day20.mobs.striders;

import me.difficulty.core.day.DayManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StriderRideListener
        extends BukkitRunnable implements Listener {

    /*
     * ========================================
     * CONFIGURACION - DIA 20
     * ========================================
     */

    private static final int REQUIRED_DAY = 20;

    // Resistencia III = amplificador 2.
    private static final int RESISTANCE_III_AMPLIFIER = 2;

    // Efectos temporales mientras se monta.
    private static final int MOUNT_EFFECT_TICKS = 40;
    private static final int REFRESH_AT_TICKS = 20;

    // 3 minutos = 3600 ticks.
    private static final int DISMOUNT_FIRE_TICKS = 3600;

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    /*
     * Guardamos los efectos de Resistencia
     * anteriores para poder restaurarlos.
     */
    private final Map<UUID, RiderState> activeRiders =
            new HashMap<>();

    private final Map<UUID, ResistanceState> activeStriders =
            new HashMap<>();

    private record ResistanceState(
            PotionEffect previous,
            int startTick
    ) {
    }

    private record RiderState(
            UUID striderId,
            ResistanceState resistance
    ) {
    }

    public StriderRideListener(
            JavaPlugin plugin,
            DayManager dayManager
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
    }

    /*
     * ========================================
     * TAREA - VERIFICAR MONTURAS
     * ========================================
     */

    @Override
    public void run() {

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            clearActiveStates();
            return;
        }

        Set<UUID> ridingPlayers = new HashSet<>();
        Set<UUID> riddenStriders = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!(player.getVehicle() instanceof Strider strider)) {
                continue;
            }

            ridingPlayers.add(player.getUniqueId());
            riddenStriders.add(strider.getUniqueId());

            applyWhileMounted(player, strider);
        }

        /*
         * Detectar jugadores que ya no
         * estan montados.
         */
        for (UUID playerId :
                new HashSet<>(activeRiders.keySet())) {

            if (ridingPlayers.contains(playerId)) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                activeRiders.remove(playerId);
            } else {
                endRide(player, true);
            }
        }

        /*
         * Retirar la Resistencia especial
         * de los Striders sin jugador.
         */
        for (UUID striderId :
                new HashSet<>(activeStriders.keySet())) {

            if (!riddenStriders.contains(striderId)) {
                endStriderBuff(striderId);
            }
        }
    }

    /*
     * ========================================
     * JUGADOR MONTA UN STRIDER
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onMount(EntityMountEvent event) {

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || !(event.getEntity() instanceof Player player)
                || !(event.getMount() instanceof Strider strider)) {
            return;
        }

        /*
         * Verificar al siguiente tick
         * que la montura se completo.
         */
        Bukkit.getScheduler().runTask(plugin, () -> {

            if (player.isOnline()
                    && dayManager.isAtLeast(REQUIRED_DAY)
                    && isRiding(player, strider)) {

                applyWhileMounted(player, strider);
            }
        });
    }

    /*
     * ========================================
     * JUGADOR BAJA DE UN STRIDER
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onDismount(EntityDismountEvent event) {

        if (!(event.getEntity() instanceof Player player)
                || !(event.getDismounted() instanceof Strider strider)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!player.isOnline()
                    || isRiding(player, strider)) {
                return;
            }

            RiderState current =
                    activeRiders.get(player.getUniqueId());

            if (current != null
                    && current.striderId()
                    .equals(strider.getUniqueId())) {

                endRide(
                        player,
                        dayManager.isAtLeast(REQUIRED_DAY)
                );

            } else if (dayManager.isAtLeast(REQUIRED_DAY)
                    && current == null) {

                /*
                 * Cubrir monturas muy breves.
                 */
                giveFireResistanceAfterDismount(player);
            }

            if (!hasPlayerRider(strider)) {
                endStriderBuff(strider.getUniqueId());
            }
        });
    }

    /*
     * ========================================
     * INMUNIDAD A EXPLOSIONES
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onStriderExplosion(
            EntityDamageEvent event
    ) {

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || !(event.getEntity() instanceof Strider strider)) {
            return;
        }

        EntityDamageEvent.DamageCause cause =
                event.getCause();

        if (cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                && cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        /*
         * Solo es inmune mientras
         * tenga un jugador montado.
         */
        if (hasPlayerRider(strider)) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * DESCONEXION DEL JUGADOR
     * ========================================
     */

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        /*
         * Desconectarse no entrega
         * automaticamente los 3 minutos.
         */
        endRide(event.getPlayer(), false);
    }

    /*
     * ========================================
     * APLICAR EFECTOS AL MONTAR
     * ========================================
     */

    private void applyWhileMounted(
            Player player,
            Strider strider
    ) {

        UUID playerId = player.getUniqueId();
        UUID striderId = strider.getUniqueId();

        RiderState previousRide =
                activeRiders.get(playerId);

        if (previousRide != null
                && !previousRide.striderId().equals(striderId)) {

            endRide(player, true);
        }

        activeRiders.computeIfAbsent(
                playerId,
                ignored -> new RiderState(
                        striderId,
                        captureResistance(player)
                )
        );

        activeStriders.computeIfAbsent(
                striderId,
                ignored -> captureResistance(strider)
        );

        /*
         * Jugador:
         * Resistencia III y Resistencia al Fuego.
         *
         * Strider:
         * Resistencia III.
         */
        refreshResistance(player);
        refreshResistance(strider);

        refreshFireResistance(
                player,
                MOUNT_EFFECT_TICKS
        );
    }

    /*
     * ========================================
     * GUARDAR RESISTENCIA ANTERIOR
     * ========================================
     */

    private ResistanceState captureResistance(
            LivingEntity entity
    ) {

        return new ResistanceState(
                entity.getPotionEffect(
                        PotionEffectType.RESISTANCE
                ),
                Bukkit.getCurrentTick()
        );
    }

    /*
     * ========================================
     * MANTENER RESISTENCIA III
     * ========================================
     */

    private void refreshResistance(
            LivingEntity entity
    ) {

        PotionEffect current =
                entity.getPotionEffect(
                        PotionEffectType.RESISTANCE
                );

        /*
         * No reducir Resistencia IV
         * o de nivel superior.
         */
        if (current != null
                && current.getAmplifier()
                > RESISTANCE_III_AMPLIFIER) {
            return;
        }

        /*
         * Evitar renovaciones innecesarias.
         */
        if (current != null
                && current.getAmplifier()
                == RESISTANCE_III_AMPLIFIER
                && (current.getDuration() > REFRESH_AT_TICKS
                || current.getDuration()
                == PotionEffect.INFINITE_DURATION)) {
            return;
        }

        entity.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        MOUNT_EFFECT_TICKS,
                        RESISTANCE_III_AMPLIFIER,
                        false,
                        false,
                        true
                )
        );
    }

    /*
     * ========================================
     * RESISTENCIA AL FUEGO MONTADO
     * ========================================
     */

    private void refreshFireResistance(
            Player player,
            int duration
    ) {

        PotionEffect current =
                player.getPotionEffect(
                        PotionEffectType.FIRE_RESISTANCE
                );

        if (current != null
                && (current.getDuration() > REFRESH_AT_TICKS
                || current.getDuration()
                == PotionEffect.INFINITE_DURATION)) {
            return;
        }

        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        duration,
                        0,
                        false,
                        false,
                        true
                )
        );
    }

    /*
     * ========================================
     * RESISTENCIA AL FUEGO POR 3 MINUTOS
     * ========================================
     */

    private void giveFireResistanceAfterDismount(
            Player player
    ) {

        PotionEffect current =
                player.getPotionEffect(
                        PotionEffectType.FIRE_RESISTANCE
                );

        /*
         * No acortar una pocion
         * de mayor duracion.
         */
        if (current != null
                && (current.getDuration() >= DISMOUNT_FIRE_TICKS
                || current.getDuration()
                == PotionEffect.INFINITE_DURATION)) {
            return;
        }

        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        DISMOUNT_FIRE_TICKS,
                        0,
                        false,
                        false,
                        true
                )
        );
    }

    /*
     * ========================================
     * FINALIZAR MONTURA DEL JUGADOR
     * ========================================
     */

    private void endRide(
            Player player,
            boolean grantFire
    ) {

        RiderState state =
                activeRiders.remove(player.getUniqueId());

        if (state != null) {
            restoreResistance(
                    player,
                    state.resistance()
            );
        }

        if (grantFire
                && dayManager.isAtLeast(REQUIRED_DAY)) {

            giveFireResistanceAfterDismount(player);
        }
    }

    /*
     * ========================================
     * RETIRAR BONUS DEL STRIDER
     * ========================================
     */

    private void endStriderBuff(UUID striderId) {

        ResistanceState state =
                activeStriders.remove(striderId);

        Entity entity = Bukkit.getEntity(striderId);

        if (state != null
                && entity instanceof Strider strider
                && !hasPlayerRider(strider)) {

            restoreResistance(strider, state);
        }
    }

    /*
     * ========================================
     * RESTAURAR RESISTENCIA ANTERIOR
     * ========================================
     */

    private void restoreResistance(
            LivingEntity entity,
            ResistanceState state
    ) {

        PotionEffect current =
                entity.getPotionEffect(
                        PotionEffectType.RESISTANCE
                );

        /*
         * No sobrescribir efectos ajenos
         * mas fuertes o duraderos.
         */
        if (current != null
                && (current.getAmplifier()
                != RESISTANCE_III_AMPLIFIER
                || current.getDuration() > MOUNT_EFFECT_TICKS
                || current.getDuration()
                == PotionEffect.INFINITE_DURATION)) {
            return;
        }

        if (current != null) {
            entity.removePotionEffect(
                    PotionEffectType.RESISTANCE
            );
        }

        PotionEffect previous = state.previous();

        if (previous == null) {
            return;
        }

        int duration = previous.getDuration();

        /*
         * Restar el tiempo transcurrido.
         */
        if (duration != PotionEffect.INFINITE_DURATION) {

            duration -= Math.max(
                    0,
                    Bukkit.getCurrentTick() - state.startTick()
            );

            if (duration <= 0) {
                return;
            }
        }

        entity.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        duration,
                        previous.getAmplifier(),
                        previous.isAmbient(),
                        previous.hasParticles(),
                        previous.hasIcon()
                )
        );
    }

    /*
     * ========================================
     * COMPROBAR SI TIENE JUGADOR
     * ========================================
     */

    private boolean hasPlayerRider(Strider strider) {

        for (Entity passenger : strider.getPassengers()) {

            if (passenger instanceof Player) {
                return true;
            }
        }

        return false;
    }

    private boolean isRiding(
            Player player,
            Strider strider
    ) {

        return player.getVehicle() instanceof Strider current
                && current.getUniqueId()
                .equals(strider.getUniqueId());
    }

    /*
     * ========================================
     * LIMPIAR ESTADOS
     * ========================================
     */

    private void clearActiveStates() {

        for (UUID playerId :
                new HashSet<>(activeRiders.keySet())) {

            Player player = Bukkit.getPlayer(playerId);

            if (player != null) {
                endRide(player, false);
            } else {
                activeRiders.remove(playerId);
            }
        }

        for (UUID striderId :
                new HashSet<>(activeStriders.keySet())) {

            endStriderBuff(striderId);
        }
    }
}