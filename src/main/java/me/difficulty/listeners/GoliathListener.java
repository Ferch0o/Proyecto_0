package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class GoliathListener
        implements Listener {

    private final CustomMobRegistry mobRegistry;
    private final DayManager dayManager;


    public GoliathListener(
            CustomMobRegistry mobRegistry,
            DayManager dayManager
    ) {

        this.mobRegistry =
                mobRegistry;

        this.dayManager =
                dayManager;
    }


    /*
     * ========================================
     * GOLIAT GOLPEA AL JUGADOR
     * ========================================
     */

    @EventHandler(ignoreCancelled = true)
    public void onGoliathHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * La víctima debe ser un jugador.
         */
        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * El atacante debe ser una araña.
         */
        if (!(event.getDamager()
                instanceof Spider spider)) {

            return;
        }


        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Debe ser específicamente Goliat.
         */
        if (!mobRegistry.is(
                spider,
                MobIds.GOLIATH
        )) {

            return;
        }


        /*
         * ========================================
         * 20%
         *
         * NÁUSEA V
         * +
         * VENENO V
         *
         * durante 10 segundos.
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 20) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.NAUSEA,
                            10 * 20,
                            4
                    )
            );

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.POISON,
                            10 * 20,
                            4
                    )
            );
        }


        /*
         * ========================================
         * 10% DE TELARAÑA
         * ========================================
         *
         * Es una tirada independiente.
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 10) {

            placeCobweb(
                    player
            );
        }
    }


    /*
     * ========================================
     * DROPS DE GOLIAT
     * ========================================
     */

    @EventHandler
    public void onGoliathDeath(
            EntityDeathEvent event
    ) {
        if (!dayManager.isAtLeast(10)) {
            return;
        }

        if (!(event.getEntity() instanceof Spider spider)) {
            return;
        }


        if (!mobRegistry.is(
                spider,
                MobIds.GOLIATH
        )) {

            return;
        }

        /*
         * ========================================
         * ELIMINAR DROPS VANILLA
         * ========================================
         */

        event.getDrops().clear();

        /*
         * ========================================
         * OJO DE ARAÑA FERMENTADO
         *
         * 2 - 5
         * ========================================
         */

        int fermentedEyes =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                2,
                                6
                        );


        event.getDrops().add(
                new ItemStack(
                        Material.FERMENTED_SPIDER_EYE,
                        fermentedEyes
                )
        );


        /*
         * ========================================
         * TELARAÑA
         *
         * 2 - 4
         * ========================================
         */

        int cobwebs =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                2,
                                5
                        );


        event.getDrops().add(
                new ItemStack(
                        Material.COBWEB,
                        cobwebs
                )
        );
    }


    /*
     * ========================================
     * COLOCAR TELARAÑA
     * ========================================
     */

    private void placeCobweb(
            Player player
    ) {

        Block block =
                player.getLocation()
                        .getBlock();


        /*
         * No reemplazamos ningún bloque.
         */
        if (!block.getType().isAir()) {
            return;
        }


        block.setType(
                Material.COBWEB,
                false
        );
    }
}