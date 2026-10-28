package me.difficulty.features.day10.mobs.zombies;

import me.difficulty.core.day.DayManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public class ZombieSpawnListener implements Listener {

    private final DayManager dayManager;
    private final Infrazombie infrazombie;
    private final CorruptedZombie corruptedZombie;
    private final Scavenger scavenger;

    public ZombieSpawnListener(
            DayManager dayManager,
            Infrazombie infrazombie,
            CorruptedZombie corruptedZombie,
            Scavenger scavenger
    ) {

        this.dayManager = dayManager;
        this.infrazombie = infrazombie;
        this.corruptedZombie = corruptedZombie;
        this.scavenger = scavenger;
    }

    @EventHandler
    public void onZombieSpawn(
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
         * SOLO SPAWN NATURAL
         * ========================================
         *
         * Esto evita afectar zombies creados con:
         *
         * /summon
         * comandos de prueba
         * plugins
         * spawn eggs
         * etc.
         */

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        boolean validSpawn =
                reason == CreatureSpawnEvent.SpawnReason.NATURAL
                        || reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                        || reason == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                        || reason == CreatureSpawnEvent.SpawnReason.JOCKEY
                        || reason == CreatureSpawnEvent.SpawnReason.SPAWNER;

        if (!validSpawn) {
            return;
        }

        /*
         * ========================================
         * SOLO ZOMBIE NORMAL
         * ========================================
         *
         * No queremos modificar:
         *
         * Drowned
         * Husk
         * Zombie Villager
         * etc.
         */

        if (event.getEntityType()
                != EntityType.ZOMBIE) {

            return;
        }


        Zombie zombie =
                (Zombie) event.getEntity();


        /*
         * ========================================
         * SELECCIÓN DE VARIANTE
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        /*
         * 0 - 39
         *
         * 40 números = 40 %
         */

        if (roll < 40) {

            infrazombie.apply(
                    zombie
            );

            return;
        }


        /*
         * 40 - 74
         *
         * 35 números = 35 %
         */

        if (roll < 75) {

            corruptedZombie.apply(
                    zombie
            );

            return;
        }

        /*
         * 75 - 99
         *
         * 25 números = 25 %
         */

        scavenger.apply(
                zombie
        );
    }
}