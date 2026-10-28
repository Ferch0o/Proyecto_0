package me.difficulty.features.day10.mobs.creepers;

import me.difficulty.core.day.DayManager;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public class CreeperSpawnListener
        implements Listener {

    private final DayManager dayManager;
    private final FuturisticCreeper futuristicCreeper;

    public CreeperSpawnListener(
            DayManager dayManager,
            FuturisticCreeper futuristicCreeper
    ) {

        this.dayManager = dayManager;
        this.futuristicCreeper = futuristicCreeper;
    }


    @EventHandler
    public void onCreeperSpawn(
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
         * aparece normalmente en el mundo.
         *
         * SPAWNER:
         * por si existe algún spawner de Creeper
         * creado mediante mapas, comandos, etc.
         *
         * No incluimos:
         *
         * - /summon
         * - spawn eggs
         * - nuestros comandos de prueba
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
         * SOLO CREEPERS
         * ========================================
         */

        if (event.getEntityType()
                != EntityType.CREEPER) {

            return;
        }


        Creeper creeper =
                (Creeper) event.getEntity();


        /*
         * ========================================
         * PROBABILIDAD
         * ========================================
         *
         * 0 - 69
         * → Futurista
         * → 70%
         *
         * 70 - 99
         * → Vanilla
         * → 30%
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll < 70) {

            futuristicCreeper.apply(
                    creeper
            );
        }

        /*
         * Si roll es 70 - 99,
         * simplemente no hacemos nada.
         *
         * El Creeper se mantiene vanilla.
         */
    }
}