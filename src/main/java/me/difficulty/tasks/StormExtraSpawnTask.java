package me.difficulty.tasks;

import me.difficulty.DifficultyPlugin;
import me.difficulty.storm.StormManager;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.bukkit.event.entity.CreatureSpawnEvent;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class StormExtraSpawnTask
        extends BukkitRunnable {


    private final StormManager stormManager;

    private final NamespacedKey stormExtraMobKey;


    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    /*
     * 20% cada segundo por jugador.
     */
    private static final int EXTRA_SPAWN_CHANCE =
            20;


    /*
     * Radio para controlar cuántos
     * hostiles existen alrededor.
     */
    private static final double LOCAL_RADIUS =
            24.0;


    /*
     * Máximo de hostiles alrededor
     * del jugador antes de bloquear
     * nuevos spawns extra.
     */
    private static final int LOCAL_HOSTILE_LIMIT =
            30;


    /*
     * Radio donde buscamos un mob hostil
     * existente para usar su tipo.
     */
    private static final double SOURCE_SEARCH_RADIUS =
            32.0;


    /*
     * Máximo de mobs creados específicamente
     * por nuestro sistema de Tormenta
     * dentro de un mundo.
     */
    private static final int MAX_EXTRA_MOBS_PER_WORLD =
            60;


    public StormExtraSpawnTask(
            DifficultyPlugin plugin,
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;


        this.stormExtraMobKey =
                new NamespacedKey(
                        plugin,
                        "storm_extra_mob"
                );
    }


    /*
     * ========================================
     * CADA 1 SEGUNDO
     * ========================================
     */

    @Override
    public void run() {


        if (!stormManager.isActive()) {
            return;
        }


        /*
         * Guardamos temporalmente el número
         * de extras de cada mundo.
         *
         * Así no recorremos todas las entidades
         * del mundo una vez por cada jugador.
         */
        Map<UUID, Long> extraCounts =
                new HashMap<>();


        for (Player player :
                Bukkit.getOnlinePlayers()) {


            tryExtraSpawn(
                    player,
                    extraCounts
            );
        }
    }


    /*
     * ========================================
     * INTENTO POR JUGADOR
     * ========================================
     */

    private void tryExtraSpawn(
            Player player,
            Map<UUID, Long> extraCounts
    ) {


        /*
         * Jugador inválido.
         */
        if (player.isDead()) {
            return;
        }


        /*
         * Creative y Spectator
         * no generan hostiles extra.
         */
        if (player.getGameMode()
                == GameMode.CREATIVE
                ||
                player.getGameMode()
                        == GameMode.SPECTATOR) {

            return;
        }


        /*
         * ========================================
         * 20%
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll >= EXTRA_SPAWN_CHANCE) {
            return;
        }


        World world =
                player.getWorld();


        /*
         * ========================================
         * LÍMITE LOCAL
         * ========================================
         */

        long nearbyHostiles =
                world.getNearbyEntities(
                                player.getLocation(),
                                LOCAL_RADIUS,
                                LOCAL_RADIUS,
                                LOCAL_RADIUS
                        )
                        .stream()

                        /*
                         * Enemy representa
                         * entidades hostiles.
                         *
                         * Es mejor que Monster
                         * para nuestro objetivo,
                         * porque cubre más tipos
                         * hostiles.
                         */
                        .filter(
                                entity ->
                                        entity instanceof Enemy
                        )

                        .count();


        if (nearbyHostiles
                >= LOCAL_HOSTILE_LIMIT) {

            return;
        }


        /*
         * ========================================
         * LÍMITE GLOBAL DE EXTRAS
         * ========================================
         */

        UUID worldId =
                world.getUID();


        long currentExtraCount =
                extraCounts.computeIfAbsent(
                        worldId,
                        id ->
                                countStormExtraMobs(
                                        world
                                )
                );


        if (currentExtraCount
                >= MAX_EXTRA_MOBS_PER_WORLD) {

            return;
        }


        /*
         * ========================================
         * BUSCAR HOSTIL REAL CERCANO
         * ========================================
         *
         * Ya no elegimos:
         *
         * Zombie
         * Skeleton
         * Spider
         * Creeper
         *
         * manualmente.
         *
         * Ahora buscamos cualquier enemigo
         * válido que ya exista alrededor.
         */

        Entity sourceHostile =
                findRandomHostile(
                        player
                );


        if (sourceHostile == null) {
            return;
        }


        /*
         * ========================================
         * MISMO TIPO DE MOB
         * ========================================
         */

        EntityType selectedType =
                sourceHostile.getType();


        /*
         * ========================================
         * BUSCAR POSICIÓN CERCA DEL MOB ORIGINAL
         * ========================================
         *
         * Esto es importante para mobs como:
         *
         * Ghast
         * Guardian
         * Blaze
         * Enderman
         * etc.
         *
         * En lugar de inventar un ambiente,
         * usamos la zona donde ya existe
         * naturalmente ese mob.
         */

        Location spawnLocation =
                findLocationNearSource(
                        sourceHostile
                );


        if (spawnLocation == null) {
            return;
        }


        /*
         * ========================================
         * CREAR MOB
         * ========================================
         *
         * NATURAL permite que nuestros
         * listeners existentes sigan
         * procesando el mob.
         */

        Entity spawnedEntity =
                world.spawnEntity(
                        spawnLocation,
                        selectedType,
                        CreatureSpawnEvent
                                .SpawnReason
                                .NATURAL
                );


        /*
         * ========================================
         * MARCAR MOB EXTRA
         * ========================================
         */

        spawnedEntity
                .getPersistentDataContainer()
                .set(
                        stormExtraMobKey,
                        PersistentDataType.BYTE,
                        (byte) 1
                );


        /*
         * Actualizar nuestro contador
         * temporal de este segundo.
         */
        extraCounts.put(
                worldId,
                currentExtraCount + 1
        );
    }


    /*
     * ========================================
     * BUSCAR UN HOSTIL CERCANO
     * ========================================
     */

    private Entity findRandomHostile(
            Player player
    ) {


        World world =
                player.getWorld();


        List<Entity> candidates =
                new ArrayList<>();


        for (Entity entity :
                world.getNearbyEntities(
                        player.getLocation(),
                        SOURCE_SEARCH_RADIUS,
                        SOURCE_SEARCH_RADIUS,
                        SOURCE_SEARCH_RADIUS
                )) {


            /*
             * Solo enemigos.
             */
            if (!(entity instanceof Enemy)) {
                continue;
            }


            /*
             * Entidad inválida o muerta.
             */
            if (!entity.isValid()
                    || entity.isDead()) {

                continue;
            }


            /*
             * No queremos duplicar
             * ciertos mobs especiales.
             */
            if (!isAllowedHostile(
                    entity.getType()
            )) {

                continue;
            }


            candidates.add(
                    entity
            );
        }


        /*
         * No hay hostiles cerca.
         *
         * En este caso no generamos nada.
         */
        if (candidates.isEmpty()) {
            return null;
        }


        /*
         * Elegir uno al azar.
         */
        return candidates.get(
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                candidates.size()
                        )
        );
    }


    /*
     * ========================================
     * MOBS EXCLUIDOS
     * ========================================
     */

    private boolean isAllowedHostile(
            EntityType type
    ) {


        return switch (type) {

            /*
             * Bosses / mobs especiales.
             *
             * No queremos que un 20%
             * pueda duplicarlos.
             */

            case WITHER,
                 ENDER_DRAGON,
                 WARDEN
                    -> false;


            default
                    -> true;
        };
    }


    /*
     * ========================================
     * POSICIÓN CERCA DEL MOB ORIGINAL
     * ========================================
     */

    private Location findLocationNearSource(
            Entity source
    ) {


        World world =
                source.getWorld();


        Location base =
                source.getLocation();


        /*
         * Probamos varias posiciones
         * pequeñas alrededor.
         *
         * Mantenernos cerca del mob original
         * ayuda a conservar su ambiente:
         *
         * Ghast → aire del Nether
         * Guardian → agua
         * Blaze → fortaleza / Nether
         * Enderman → End
         * etc.
         */
        for (int attempt = 0;
             attempt < 8;
             attempt++) {


            double offsetX =
                    ThreadLocalRandom
                            .current()
                            .nextDouble(
                                    -2.5,
                                    2.5
                            );


            double offsetY =
                    ThreadLocalRandom
                            .current()
                            .nextDouble(
                                    -1.0,
                                    1.0
                            );


            double offsetZ =
                    ThreadLocalRandom
                            .current()
                            .nextDouble(
                                    -2.5,
                                    2.5
                            );


            Location candidate =
                    base.clone()
                            .add(
                                    offsetX,
                                    offsetY,
                                    offsetZ
                            );


            /*
             * Respetar borde del mundo.
             */
            if (!world.getWorldBorder()
                    .isInside(candidate)) {

                continue;
            }


            /*
             * Evitar meterlo directamente
             * dentro de un bloque sólido.
             *
             * Agua, lava y aire pueden ser
             * válidos dependiendo del mob.
             */
            if (candidate.getBlock()
                    .getType()
                    .isSolid()) {

                continue;
            }


            return candidate;
        }


        /*
         * Si no encontramos una posición
         * desplazada, usamos la ubicación
         * del mob original.
         *
         * Sabemos que ahí ya existe
         * correctamente una entidad
         * de ese tipo.
         */
        if (world.getWorldBorder()
                .isInside(base)) {

            return base;
        }


        return null;
    }


    /*
     * ========================================
     * CONTAR EXTRAS
     * ========================================
     */

    private long countStormExtraMobs(
            World world
    ) {


        return world.getEntities()
                .stream()

                .filter(
                        entity ->
                                entity instanceof Enemy
                )

                .filter(
                        entity ->

                                entity
                                        .getPersistentDataContainer()
                                        .has(
                                                stormExtraMobKey,
                                                PersistentDataType.BYTE
                                        )
                )

                .count();
    }
}