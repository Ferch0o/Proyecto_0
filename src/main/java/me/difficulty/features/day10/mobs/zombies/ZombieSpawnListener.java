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

    @EventHandler(ignoreCancelled = true)
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
         * DISTRIBUCION DIA 10
         * ========================================
         *
         * El Zombie Gigante ya tiene reservado
         * el 5% de los spawns naturales elegibles.
         *
         * El 95% restante se distribuye:
         *
         * 40 partes -> Infrazombie
         * 30 partes -> Zombie Corrupto
         * 25 partes -> Carroñero
         *
         * TOTAL: 95 partes
         */

        int roll = ThreadLocalRandom
                .current()
                .nextInt(95);

        // 0 - 39: 40 partes
        if (roll < 40) {
            infrazombie.apply(zombie);
            return;
        }

        // 40 - 69: 30 partes
        if (roll < 70) {
            corruptedZombie.apply(zombie);
            return;
        }

        // 70 - 94: 25 partes
        scavenger.apply(zombie);
    }
}