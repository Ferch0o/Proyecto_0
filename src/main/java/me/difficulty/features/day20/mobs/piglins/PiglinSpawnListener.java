
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class PiglinSpawnListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedPiglin corrupted;
    private final ExplosivePiglin explosive;
    private final KingPiglin king;

    public PiglinSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedPiglin corrupted,
            ExplosivePiglin explosive,
            KingPiglin king
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
        this.corrupted = corrupted;
        this.explosive = explosive;
        this.king = king;
    }

    /*
     * ========================================
     * APARICIONES - DÍA 20
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(CreatureSpawnEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        /*
         * Solo apariciones naturales
         * y mediante spawners.
         */
        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        /*
         * ========================================
         * PIGLIN BRUTO: 100% REY
         * ========================================
         */
        if (event.getEntityType()
                == EntityType.PIGLIN_BRUTE) {

            PiglinBrute brute =
                    (PiglinBrute) event.getEntity();

            if (registry.getMobId(brute).isEmpty()) {
                king.apply(brute);
            }

            return;
        }

        /*
         * ========================================
         * PIGLIN NORMAL
         * ========================================
         */
        if (event.getEntityType() != EntityType.PIGLIN) {
            return;
        }

        Piglin piglin = (Piglin) event.getEntity();

        /*
         * ========================================
         * ELIMINAR PIGLINS BEBÉS
         * ========================================
         *
         * Todo Piglin generado naturalmente
         * será adulto desde su aparición.
         */
        piglin.setAdult();

        if (registry.getMobId(piglin).isPresent()) {
            return;
        }

        /*
         * ========================================
         * DISTRIBUCIÓN
         *
         * 0 - 49: Corrupto  (50%)
         * 50 - 89: Explosivo (40%)
         * 90 - 99: Rey      (10%)
         * ========================================
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < 50) {
            corrupted.apply(piglin);
            return;
        }

        if (roll < 90) {
            explosive.apply(piglin);
            return;
        }

        /*
         * ========================================
         * REY PIGLIN: 10%
         * ========================================
         *
         * Sustituimos al Piglin normal
         * por un Piglin Bruto real.
         *
         * De esta manera, todos los Reyes
         * comparten la misma apariencia.
         */
        Location location = piglin
                .getLocation()
                .clone();

        /*
         * Cancelar la aparición original
         * para evitar duplicar entidades.
         */
        event.setCancelled(true);

        /*
         * SpawnReason.CUSTOM evita procesar
         * otra vez al Rey como aparición
         * natural.
         *
         * Aplicamos la configuración antes
         * de incorporar la entidad al mundo.
         */
        location.getWorld().spawn(
                location,
                PiglinBrute.class,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                king::apply
        );
    }

    /*
     * ========================================
     * ENTIDADES DE CHUNKS CARGADOS
     * ========================================
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        for (Entity entity : event.getEntities()) {

            /*
             * Los Piglins normales que
             * ya existían como bebés
             * también se vuelven adultos.
             */
            if (entity instanceof Piglin piglin) {

                if (!piglin.isAdult()) {
                    piglin.setAdult();
                }

                continue;
            }

            /*
             * Convertir en Rey a cualquier
             * Piglin Bruto sin identidad
             * personalizada que se cargue
             * desde un chunk.
             */
            if (!(entity instanceof PiglinBrute brute)) {
                continue;
            }

            if (registry.getMobId(brute).isPresent()) {
                continue;
            }

            king.apply(brute);
        }
    }
}