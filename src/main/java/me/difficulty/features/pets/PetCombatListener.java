package me.difficulty.features.pets;

import me.difficulty.day.DayManager;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PetCombatListener implements Listener {

    private final DayManager dayManager;


    public PetCombatListener(
            DayManager dayManager
    ) {

        this.dayManager =
                dayManager;
    }


    /*
     * ========================================
     * DEBILIDAD IV
     * ========================================
     *
     * Si un gato o lobo SALVAJE golpea
     * al jugador:
     *
     * → Debilidad IV
     * → 10 segundos
     */

    @EventHandler(ignoreCancelled = true)
    public void onWildPetHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * Solo desde Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * La víctima debe ser un jugador.
         */
        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * ====================================
         * LOBO SALVAJE
         * ====================================
         */

        if (event.getDamager()
                instanceof Wolf wolf) {

            /*
             * Domesticado:
             * no aplica Debilidad.
             */
            if (wolf.isTamed()) {
                return;
            }


            applyWeakness(
                    player
            );


            return;
        }


        /*
         * ====================================
         * GATO SALVAJE
         * ====================================
         */

        if (event.getDamager()
                instanceof Cat cat) {

            /*
             * Domesticado:
             * no aplica Debilidad.
             */
            if (cat.isTamed()) {
                return;
            }


            applyWeakness(
                    player
            );
        }
    }


    /*
     * ========================================
     * APLICAR DEBILIDAD IV
     * ========================================
     */

    private void applyWeakness(
            Player player
    ) {

        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        10 * 20,
                        3,
                        false,
                        true
                )
        );
    }


    /*
     * ========================================
     * GATO INMUNE A EXPLOSIONES
     * ========================================
     */

    @EventHandler(ignoreCancelled = true)
    public void onCatExplosionDamage(
            EntityDamageEvent event
    ) {

        /*
         * Solo desde Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Solo gatos.
         */
        if (!(event.getEntity()
                instanceof Cat)) {

            return;
        }


        EntityDamageEvent.DamageCause cause =
                event.getCause();


        /*
         * ====================================
         * INMUNIDAD
         * ====================================
         *
         * Creeper / TNT / otras explosiones.
         */

        if (cause
                == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                ||
                cause
                        == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            event.setCancelled(
                    true
            );
        }
    }
}