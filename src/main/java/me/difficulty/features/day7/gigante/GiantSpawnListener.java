
package me.difficulty.features.day7.gigante;

import me.difficulty.core.day.DayManager;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class GiantSpawnListener implements Listener {

    private static final int SPAWN_CHANCE = 5;

    // Espacio conservador para el cuerpo del Gigante
    private static final double HALF_WIDTH = 1.8;
    private static final int REQUIRED_HEIGHT = 13;

    private final DayManager dayManager;
    private final GiantZombie giantZombie;
    private final NamespacedKey stormExtraMobKey;

    public GiantSpawnListener(
            JavaPlugin plugin,
            DayManager dayManager,
            GiantZombie giantZombie
    ) {
        this.dayManager = dayManager;
        this.giantZombie = giantZombie;

        this.stormExtraMobKey = new NamespacedKey(
                plugin,
                "storm_extra_mob"
        );
    }

    @EventHandler(
            priority = EventPriority.LOW,
            ignoreCancelled = true
    )
    public void onZombieSpawn(CreatureSpawnEvent event) {

        // Disponible desde el dia 7
        if (!dayManager.isAtLeast(7)) {
            return;
        }

        // Solo zombis normales
        if (event.getEntityType() != EntityType.ZOMBIE) {
            return;
        }

        // Solo generacion natural
        if (event.getSpawnReason()
                != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        // Excluir enemigos extra de nuestra Tormenta
        if (event.getEntity()
                .getPersistentDataContainer()
                .has(
                        stormExtraMobKey,
                        PersistentDataType.BYTE
                )) {
            return;
        }

        Location location = event.getEntity().getLocation();
        World world = location.getWorld();

        if (world == null
                || world.getEnvironment()
                != World.Environment.NORMAL) {
            return;
        }

        // Probabilidad del 5 %
        if (ThreadLocalRandom.current()
                .nextInt(100) >= SPAWN_CHANCE) {
            return;
        }

        // Comprobar superficie, terreno y espacio
        if (!isSafeLocation(location)) {
            return;
        }

        // Crear el Gigante antes de cancelar el zombi.
        // El callback lo identifica antes del evento de spawn.
        Giant giant = world.spawn(
                location,
                Giant.class,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                false,
                giantZombie::apply
        );

        // Evitar que existan ambos mobs simultaneamente.
        if (giant.isValid()) {
            event.setCancelled(true);
        }
    }

    private boolean isSafeLocation(Location location) {

        World world = location.getWorld();

        if (world == null) {
            return false;
        }

        int baseY = location.getBlockY();
        int centerX = location.getBlockX();
        int centerZ = location.getBlockZ();

        if (baseY <= world.getMinHeight()
                || baseY + REQUIRED_HEIGHT
                >= world.getMaxHeight()) {
            return false;
        }

        if (!world.getWorldBorder().isInside(location)) {
            return false;
        }

        // El zombi debe estar en la superficie.
        // Esto descarta cuevas y espacios bajo tierra.
        int surfaceY = world.getHighestBlockYAt(
                centerX,
                centerZ,
                HeightMap.MOTION_BLOCKING_NO_LEAVES
        );

        if (baseY < surfaceY) {
            return false;
        }

        // El Gigante necesita un suelo solido.
        Block floor = world.getBlockAt(
                centerX,
                baseY - 1,
                centerZ
        );

        if (!floor.getType().isSolid()) {
            return false;
        }

        // Comprobar el volumen completo del Gigante.
        int minX = (int) Math.floor(
                location.getX() - HALF_WIDTH
        );
        int maxX = (int) Math.floor(
                location.getX() + HALF_WIDTH
        );
        int minZ = (int) Math.floor(
                location.getZ() - HALF_WIDTH
        );
        int maxZ = (int) Math.floor(
                location.getZ() + HALF_WIDTH
        );

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                // No cargar chunks adicionales
                if (!world.isChunkLoaded(
                        x >> 4,
                        z >> 4
                )) {
                    return false;
                }

                for (int y = baseY;
                     y < baseY + REQUIRED_HEIGHT;
                     y++) {

                    Block block = world.getBlockAt(
                            x, y, z
                    );

                    if (!block.isPassable()
                            || block.isLiquid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}