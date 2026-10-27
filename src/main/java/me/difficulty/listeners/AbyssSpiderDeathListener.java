package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import me.difficulty.mobs.day10.Goliath;
import org.bukkit.Location;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.concurrent.ThreadLocalRandom;

public class AbyssSpiderDeathListener
        implements Listener {

    private final CustomMobRegistry mobRegistry;
    private final DayManager dayManager;
    private final Goliath goliath;


    public AbyssSpiderDeathListener(
            CustomMobRegistry mobRegistry,
            DayManager dayManager,
            Goliath goliath
    ) {

        this.mobRegistry = mobRegistry;
        this.dayManager = dayManager;
        this.goliath = goliath;
    }


    @EventHandler
    public void onAbyssSpiderDeath(
            EntityDeathEvent event
    ) {

        /*
         * ========================================
         * SOLO ARAÑAS
         * ========================================
         */

        if (!(event.getEntity()
                instanceof Spider spider)) {

            return;
        }


        /*
         * ========================================
         * SOLO DESDE EL DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * ========================================
         * SOLO ARAÑA DEL ABISMO
         * ========================================
         */

        if (!mobRegistry.is(
                spider,
                MobIds.ABYSS_SPIDER
        )) {

            return;
        }


        /*
         * ========================================
         * PROBABILIDAD
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll < 90) {

            spawnCaveSpiders(
                    spider.getLocation()
            );

            return;
        }


        spawnGoliath(
                spider.getLocation()
        );
    }


    /*
     * ========================================
     * 95% → 3 ARAÑAS DE CUEVA
     * ========================================
     */

    private void spawnCaveSpiders(
            Location location
    ) {

        /*
         * Las separamos un poco para que
         * no aparezcan las tres exactamente
         * dentro de la misma entidad.
         */

        spawnCaveSpider(
                location.clone()
                        .add(
                                0.5,
                                0.0,
                                0.0
                        )
        );

        spawnCaveSpider(
                location.clone()
                        .add(
                                -0.5,
                                0.0,
                                0.0
                        )
        );

        spawnCaveSpider(
                location.clone()
                        .add(
                                0.0,
                                0.0,
                                0.5
                        )
        );
    }


    private void spawnCaveSpider(
            Location location
    ) {

        location.getWorld().spawn(
                location,
                CaveSpider.class
        );
    }


    /*
     * ========================================
     * 5% → GOLIAT
     * ========================================
     */

    private void spawnGoliath(
            Location location
    ) {

        Spider spider =
                location.getWorld().spawn(
                        location,
                        Spider.class
                );


        /*
         * Convertimos inmediatamente
         * la araña recién creada en Goliat.
         */
        goliath.apply(
                spider
        );
    }
}