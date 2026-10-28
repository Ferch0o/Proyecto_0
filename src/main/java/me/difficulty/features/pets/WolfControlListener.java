package me.difficulty.features.pets;

import me.difficulty.core.day.DayManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class WolfControlListener
        implements Listener {

    private final DayManager dayManager;


    public WolfControlListener(
            DayManager dayManager
    ) {

        this.dayManager =
                dayManager;
    }


    /*
     * ========================================
     * BLOQUEAR ENFADO VANILLA
     * ========================================
     *
     * Si Minecraft intenta asignar un jugador
     * como objetivo de un lobo salvaje,
     * lo cancelamos.
     *
     * Nuestro PetBehaviorTask se encarga
     * de perseguirlo manualmente.
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onWolfTargetPlayer(
            EntityTargetLivingEntityEvent event
    ) {

        /*
         * Solo Día 10+.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Solo lobos.
         */
        if (!(event.getEntity()
                instanceof Wolf wolf)) {

            return;
        }


        /*
         * Los lobos domesticados conservan
         * su comportamiento vanilla normal.
         */
        if (wolf.isTamed()) {
            return;
        }


        /*
         * Solo bloqueamos targets hacia jugadores.
         */
        if (!(event.getTarget()
                instanceof Player)) {

            return;
        }


        /*
         * ========================================
         * CANCELAR TARGET VANILLA
         * ========================================
         */

        event.setCancelled(
                true
        );


        wolf.setTarget(
                null
        );


        wolf.setAngry(
                false
        );
    }


    /*
     * ========================================
     * PERMITIR DOMESTICACIÓN
     * ========================================
     *
     * Cuando el jugador usa un hueso sobre
     * un lobo salvaje, limpiamos cualquier
     * estado de enfado justo antes de que
     * Minecraft procese la interacción.
     */

    @EventHandler(
            priority = EventPriority.LOWEST,
            ignoreCancelled = true
    )
    public void onPlayerUseBone(
            PlayerInteractEntityEvent event
    ) {

        /*
         * Solo Día 10+.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Debe hacer clic en un lobo.
         */
        if (!(event.getRightClicked()
                instanceof Wolf wolf)) {

            return;
        }


        /*
         * Si ya está domesticado,
         * no hacemos nada.
         */
        if (wolf.isTamed()) {
            return;
        }


        /*
         * Obtener objeto usado en esa mano.
         */
        ItemStack item =
                event.getPlayer()
                        .getInventory()
                        .getItem(
                                event.getHand()
                        );


        /*
         * Solo nos interesa el hueso.
         */
        if (item.getType()
                != Material.BONE) {

            return;
        }


        /*
         * ========================================
         * LIMPIAR ENFADO
         * ========================================
         */

        wolf.setTarget(
                null
        );

        wolf.setAngry(
                false
        );


        wolf.getPathfinder()
                .stopPathfinding();


        /*
         * NO cancelamos el evento.
         *
         * Después de esto Minecraft continúa
         * normalmente con el intento de
         * domesticación.
         */
    }
}