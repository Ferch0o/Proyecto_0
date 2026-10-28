package me.difficulty.features.storm.day10.listeners;

import me.difficulty.DifficultyPlugin;
import me.difficulty.mobs.MobIds;
import me.difficulty.features.storm.core.StormManager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


public class StormDiamondDropListener
        implements Listener {


    private final StormManager stormManager;

    private final NamespacedKey customMobIdKey;


    /*
     * ========================================
     * PROBABILIDAD
     * ========================================
     */

    private static final int DIAMOND_CHANCE =
            50;


    /*
     * ========================================
     * CANTIDAD
     * ========================================
     */

    private static final int MIN_DIAMONDS = 2;

    private static final int MAX_DIAMONDS = 7;


    /*
     * ========================================
     * MOBS CUSTOM DEL DÍA 10
     * ========================================
     */

    private static final Set<String> DAY_10_MOBS =
            Set.of(

                    MobIds.INFRAZOMBIE,

                    MobIds.CORRUPTED_ZOMBIE,

                    MobIds.SCAVENGER,

                    MobIds.ELEMENTAL_SKELETON,

                    MobIds.GLADIATOR,

                    MobIds.FUTURISTIC_CREEPER,

                    MobIds.SPEED_SPIDER,

                    MobIds.ABYSS_SPIDER,

                    MobIds.WEAVER,

                    MobIds.GOLIATH
            );


    public StormDiamondDropListener(
            DifficultyPlugin plugin,
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;


        this.customMobIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_mob_id"
                );
    }


    /*
     * ========================================
     * DROP DE DIAMANTES
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onMobDeath(
            EntityDeathEvent event
    ) {


        /*
         * ========================================
         * SOLO DURANTE NUESTRA TORMENTA
         * ========================================
         */

        if (!stormManager.hasDay10Effects()) {
            return;
        }


        /*
         * ========================================
         * OBTENER CUSTOM MOB ID
         * ========================================
         */

        String customMobId =
                event.getEntity()
                        .getPersistentDataContainer()
                        .get(
                                customMobIdKey,
                                PersistentDataType.STRING
                        );


        /*
         * Mob vanilla.
         */
        if (customMobId == null) {
            return;
        }


        /*
         * ========================================
         * SOLO CUSTOM MOBS DEL DÍA 10
         * ========================================
         */

        if (!DAY_10_MOBS.contains(
                customMobId
        )) {

            return;
        }


        /*
         * ========================================
         * 50%
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll >= DIAMOND_CHANCE) {
            return;
        }


        /*
         * ========================================
         * 3 - 7 DIAMANTES
         * ========================================
         */

        int amount =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                MIN_DIAMONDS,
                                MAX_DIAMONDS + 1
                        );


        /*
         * Añadimos los diamantes sin
         * eliminar sus drops normales
         * o personalizados.
         */

        event.getDrops()
                .add(
                        new ItemStack(
                                Material.DIAMOND,
                                amount
                        )
                );
    }
}