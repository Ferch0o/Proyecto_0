
package me.difficulty.listeners;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;


public class SharedVillagerCureListener implements Listener {

    private static final int MAX_CURE_MINOR_POSITIVE = 25;

    private final JavaPlugin plugin;
    private final NamespacedKey curedByKey;


    public SharedVillagerCureListener(JavaPlugin plugin) {

        this.plugin = plugin;

        this.curedByKey = new NamespacedKey(
                plugin,
                "shared_villager_cure"
        );
    }


    /*
     * ========================================
     * CURACIÓN NUEVA
     * ========================================
     *
     * No depende del Día 10.
     * Funciona desde el primer día.
     */

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onVillagerCured(EntityTransformEvent event) {

        if (event.getTransformReason()
                != EntityTransformEvent.TransformReason.CURED) {
            return;
        }

        if (!(event.getEntity() instanceof ZombieVillager zombie)) {
            return;
        }

        if (!(event.getTransformedEntity() instanceof Villager villager)) {
            return;
        }

        OfflinePlayer curer = zombie.getConversionPlayer();

        UUID curerId = curer == null
                ? null
                : curer.getUniqueId();

        /*
         * Esperamos un tick para que Minecraft
         * termine de aplicar la reputación
         * de la curación.
         */

        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    if (!villager.isValid() || villager.isDead()) {
                        return;
                    }

                    UUID sourceId = curerId;

                    /*
                     * Comprobamos que exista
                     * reputación por curación.
                     */

                    if (sourceId == null
                            || villager.getReputation(sourceId)
                            .getReputation(ReputationType.MAJOR_POSITIVE)
                            <= 0) {

                        sourceId = findCurePlayer(villager);
                    }

                    if (sourceId == null) {
                        return;
                    }

                    markCured(villager, sourceId);

                    syncOnlinePlayers(villager);
                }
        );
    }


    /*
     * ========================================
     * COMPARTIR AL COMERCIAR
     * ========================================
     *
     * Sirve para jugadores que estaban
     * desconectados durante la curación.
     */

    @EventHandler(
            priority = EventPriority.LOWEST,
            ignoreCancelled = true
    )
    public void onVillagerInteract(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }

        shareDiscount(
                villager,
                event.getPlayer().getUniqueId()
        );
    }


    /*
     * ========================================
     * RECONOCER ALDEANOS ANTIGUOS
     * ========================================
     *
     * Este método lo utiliza:
     *
     * /sharedcure add
     *
     * Solo acepta aldeanos con evidencia
     * de una curación previa.
     */

    public boolean registerLegacyCure(Villager villager) {

        if (isShared(villager)) {
            return true;
        }

        UUID curePlayer = findCurePlayer(villager);

        if (curePlayer == null) {
            return false;
        }

        markCured(villager, curePlayer);

        syncOnlinePlayers(villager);

        return true;
    }


    /*
     * ========================================
     * COMPROBAR MARCA PERSISTENTE
     * ========================================
     */

    public boolean isShared(Villager villager) {

        return getStoredCurer(villager) != null;
    }


    /*
     * ========================================
     * BUSCAR REPUTACIÓN DE CURACIÓN
     * ========================================
     *
     * MAJOR_POSITIVE es el indicador
     * de curación que utilizamos.
     *
     * No aceptamos MINOR_POSITIVE solo,
     * porque puede llegar por rumores
     * de aldeanos cercanos.
     */

    private UUID findCurePlayer(Villager villager) {

        UUID bestPlayer = null;
        int bestValue = 0;

        Map<UUID, Reputation> reputations =
                villager.getReputations();

        for (Map.Entry<UUID, Reputation> entry :
                reputations.entrySet()) {

            int value = entry.getValue().getReputation(
                    ReputationType.MAJOR_POSITIVE
            );

            if (value > bestValue) {

                bestValue = value;
                bestPlayer = entry.getKey();
            }
        }

        return bestPlayer;
    }


    /*
     * ========================================
     * MARCAR ALDEANO CURADO
     * ========================================
     */

    private void markCured(
            Villager villager,
            UUID curerId
    ) {

        villager.getPersistentDataContainer().set(
                curedByKey,
                PersistentDataType.STRING,
                curerId.toString()
        );
    }


    /*
     * ========================================
     * RECUPERAR JUGADOR QUE CURÓ
     * ========================================
     */

    private UUID getStoredCurer(Villager villager) {

        String stored = villager.getPersistentDataContainer().get(
                curedByKey,
                PersistentDataType.STRING
        );

        if (stored == null) {
            return null;
        }

        try {
            return UUID.fromString(stored);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }


    /*
     * ========================================
     * SINCRONIZAR JUGADORES CONECTADOS
     * ========================================
     */

    private void syncOnlinePlayers(Villager villager) {

        for (Player player :
                plugin.getServer().getOnlinePlayers()) {

            shareDiscount(
                    villager,
                    player.getUniqueId()
            );
        }
    }


    /*
     * ========================================
     * COMPARTIR DESCUENTO DE CURACIÓN
     * ========================================
     *
     * No modificamos:
     *
     * - TRADING
     * - MAJOR_NEGATIVE
     * - MINOR_NEGATIVE
     * - efectos de raids
     * - precios base de intercambios
     */

    private void shareDiscount(
            Villager villager,
            UUID playerId
    ) {

        UUID curerId = getStoredCurer(villager);

        if (curerId == null) {
            return;
        }

        Reputation cureReputation =
                villager.getReputation(curerId);

        int majorCure = cureReputation.getReputation(
                ReputationType.MAJOR_POSITIVE
        );

        /*
         * Seguridad adicional:
         *
         * si ya no hay evidencia de
         * curación, no concedemos nada.
         */

        if (majorCure <= 0) {
            return;
        }

        /*
         * Minor positive también puede
         * incluir rumores de otros aldeanos.
         *
         * Limitamos el valor al máximo
         * que concede una curación normal.
         */

        int minorCure = Math.min(
                MAX_CURE_MINOR_POSITIVE,
                Math.max(
                        0,
                        cureReputation.getReputation(
                                ReputationType.MINOR_POSITIVE
                        )
                )
        );

        Reputation targetReputation =
                villager.getReputation(playerId);

        boolean changed = false;

        int currentMajor = targetReputation.getReputation(
                ReputationType.MAJOR_POSITIVE
        );

        if (majorCure > currentMajor) {

            targetReputation.setReputation(
                    ReputationType.MAJOR_POSITIVE,
                    majorCure
            );

            changed = true;
        }

        int currentMinor = targetReputation.getReputation(
                ReputationType.MINOR_POSITIVE
        );

        if (minorCure > currentMinor) {

            targetReputation.setReputation(
                    ReputationType.MINOR_POSITIVE,
                    minorCure
            );

            changed = true;
        }

        if (changed) {

            villager.setReputation(
                    playerId,
                    targetReputation
            );
        }
    }
}
