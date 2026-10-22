package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.day10.AbyssSpider;
import me.difficulty.mobs.day10.Goliath;
import me.difficulty.mobs.day10.SpeedSpider;
import me.difficulty.mobs.day10.Weaver;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public class SpiderSpawnListener
        implements Listener {

    private final DayManager dayManager;

    private final SpeedSpider speedSpider;
    private final AbyssSpider abyssSpider;
    private final Weaver weaver;
    private final Goliath goliath;


    public SpiderSpawnListener(
            DayManager dayManager,
            SpeedSpider speedSpider,
            AbyssSpider abyssSpider,
            Weaver weaver,
            Goliath goliath
    ) {

        this.dayManager = dayManager;

        this.speedSpider = speedSpider;
        this.abyssSpider = abyssSpider;
        this.weaver = weaver;
        this.goliath = goliath;
    }


    @EventHandler
    public void onSpiderSpawn(
            CreatureSpawnEvent event
    ) {

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
         * SPAWNS PERMITIDOS
         * ========================================
         *
         * NATURAL:
         * arañas generadas normalmente.
         *
         * SPAWNER:
         * arañas generadas por un spawner.
         *
         * No modificamos:
         *
         * - /summon
         * - spawn eggs
         * - nuestros comandos de prueba
         * - mobs creados internamente por el plugin
         */

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        boolean validSpawn =
                reason == CreatureSpawnEvent.SpawnReason.NATURAL
                        || reason == CreatureSpawnEvent.SpawnReason.SPAWNER;

        if (!validSpawn) {
            return;
        }


        /*
         * ========================================
         * SOLO ARAÑA NORMAL
         * ========================================
         *
         * Las Cave Spiders NO entran aquí.
         */

        if (event.getEntityType()
                != EntityType.SPIDER) {

            return;
        }


        Spider spider =
                (Spider) event.getEntity();


        /*
         * ========================================
         * SELECCIÓN DE VARIANTE
         * ========================================
         *
         * 0 - 34
         * → Velocista
         * → 35%
         *
         * 35 - 54
         * → Abismo
         * → 20%
         *
         * 55 - 89
         * → Tejedora
         * → 35%
         *
         * 90 - 99
         * → Goliat
         * → 10%
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        /*
         * ========================================
         * 35% → ARAÑA VELOCISTA
         * ========================================
         */

        if (roll < 35) {

            speedSpider.apply(
                    spider
            );

            return;
        }


        /*
         * ========================================
         * 20% → ARAÑA DEL ABISMO
         * ========================================
         *
         * 35 - 54
         */

        if (roll < 55) {

            abyssSpider.apply(
                    spider
            );

            return;
        }


        /*
         * ========================================
         * 35% → TEJEDORA
         * ========================================
         *
         * 55 - 89
         */

        if (roll < 90) {

            weaver.apply(
                    spider
            );

            return;
        }


        /*
         * ========================================
         * 10% → GOLIAT
         * ========================================
         *
         * 90 - 99
         */

        goliath.apply(
                spider
        );
    }
}