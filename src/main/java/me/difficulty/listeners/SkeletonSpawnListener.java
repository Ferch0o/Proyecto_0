package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.day10.ElementalSkeleton;
import me.difficulty.mobs.day10.Gladiator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonSpawnListener
        implements Listener {

    private final DayManager dayManager;
    private final ElementalSkeleton elementalSkeleton;
    private final Gladiator gladiator;

    public SkeletonSpawnListener(
            DayManager dayManager,
            ElementalSkeleton elementalSkeleton,
            Gladiator gladiator
    ) {

        this.dayManager = dayManager;
        this.elementalSkeleton = elementalSkeleton;
        this.gladiator = gladiator;
    }


    @EventHandler
    public void onSkeletonSpawn(
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
         */

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        boolean validSpawn =
                reason == CreatureSpawnEvent.SpawnReason.NATURAL
                        || reason == CreatureSpawnEvent.SpawnReason.JOCKEY
                        || reason == CreatureSpawnEvent.SpawnReason.SPAWNER;

        if (!validSpawn) {
            return;
        }


        /*
         * ========================================
         * SOLO ESQUELETO NORMAL
         * ========================================
         *
         * No modificamos:
         *
         * - Wither Skeleton
         * - Stray
         * - Bogged
         * etc.
         */

        if (event.getEntityType()
                != EntityType.SKELETON) {

            return;
        }


        Skeleton skeleton =
                (Skeleton) event.getEntity();


        /*
         * ========================================
         * SELECCIÓN DE VARIANTE
         * ========================================
         *
         * 0 - 59
         * → Elemental
         * → 60%
         *
         * 60 - 99
         * → Gladiador
         * → 40%
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll < 85) {
            elementalSkeleton.apply(skeleton);
            return;
        }


        gladiator.apply(skeleton);
    }
}