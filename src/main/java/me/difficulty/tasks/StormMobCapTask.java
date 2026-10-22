package me.difficulty.tasks;

import me.difficulty.storm.StormManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StormMobCapTask
        extends BukkitRunnable {

    private final StormManager stormManager;


    /*
     * ========================================
     * MULTIPLICADOR DE LA TORMENTA
     * ========================================
     *
     * 2 = doble mob cap
     *
     * En el futuro podremos cambiarlo por:
     *
     * 3 = triple
     * 4 = cuádruple
     * etc.
     */
    private static final int MOB_CAP_MULTIPLIER = 2;


    /*
     * ========================================
     * LÍMITES ORIGINALES
     * ========================================
     *
     * Guardamos el valor que tenía cada mundo
     * ANTES de modificarlo.
     *
     * UUID del mundo -> límite original
     */
    private final Map<UUID, Integer> originalMonsterCaps =
            new HashMap<>();


    public StormMobCapTask(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    @Override
    public void run() {

        /*
         * ========================================
         * TORMENTA ACTIVA
         * ========================================
         */

        if (stormManager.isActive()) {

            applyStormMobCap();

            return;
        }


        /*
         * ========================================
         * TORMENTA INACTIVA
         * ========================================
         */

        restoreOriginalMobCaps();
    }


    /*
     * ========================================
     * APLICAR MOB CAP ×2
     * ========================================
     */

    private void applyStormMobCap() {

        for (World world :
                Bukkit.getWorlds()) {


            UUID worldId =
                    world.getUID();


            /*
             * Si ya modificamos este mundo,
             * NO volvemos a multiplicarlo.
             *
             * Evitamos:
             *
             * 70 -> 140 -> 280 -> 560...
             */
            if (originalMonsterCaps.containsKey(
                    worldId
            )) {

                continue;
            }


            /*
             * Obtener el límite configurado
             * en este mundo.
             */
            int originalLimit =
                    world.getSpawnLimit(
                            SpawnCategory.MONSTER
                    );


            /*
             * Guardamos exactamente el valor
             * original.
             *
             * Puede ser -1 si el mundo está
             * heredando el límite general
             * del servidor.
             */
            originalMonsterCaps.put(
                    worldId,
                    originalLimit
            );


            /*
             * ====================================
             * OBTENER LÍMITE EFECTIVO
             * ====================================
             */

            int effectiveLimit =
                    originalLimit;


            /*
             * Un valor negativo significa:
             *
             * "usar límite global del servidor".
             *
             * Por eso obtenemos ese valor.
             */
            if (effectiveLimit < 0) {

                effectiveLimit =
                        Bukkit.getSpawnLimit(
                                SpawnCategory.MONSTER
                        );
            }


            /*
             * Si el spawning está deshabilitado,
             * no queremos activarlo accidentalmente.
             */
            if (effectiveLimit <= 0) {
                continue;
            }


            /*
             * ====================================
             * MULTIPLICAR ×2
             * ====================================
             */

            int stormLimit =
                    effectiveLimit
                            * MOB_CAP_MULTIPLIER;


            world.setSpawnLimit(
                    SpawnCategory.MONSTER,
                    stormLimit
            );


            /*
             * Mensaje de consola para comprobar.
             */
            Bukkit.getLogger()
                    .info(
                            "[DifficultyPlugin] Tormenta: "
                                    + world.getName()
                                    + " MobCap MONSTER: "
                                    + effectiveLimit
                                    + " -> "
                                    + stormLimit
                    );
        }
    }


    /*
     * ========================================
     * RESTAURAR MOB CAP
     * ========================================
     */

    public void restoreOriginalMobCaps() {

        if (originalMonsterCaps.isEmpty()) {
            return;
        }


        for (Map.Entry<UUID, Integer> entry :
                originalMonsterCaps.entrySet()) {


            World world =
                    Bukkit.getWorld(
                            entry.getKey()
                    );


            /*
             * El mundo podría haberse
             * descargado.
             */
            if (world == null) {
                continue;
            }


            int originalLimit =
                    entry.getValue();


            /*
             * Restauramos exactamente
             * lo que tenía antes.
             *
             * Incluso si era -1.
             */
            world.setSpawnLimit(
                    SpawnCategory.MONSTER,
                    originalLimit
            );


            Bukkit.getLogger()
                    .info(
                            "[DifficultyPlugin] MobCap restaurado: "
                                    + world.getName()
                                    + " -> "
                                    + originalLimit
                    );
        }


        originalMonsterCaps.clear();
    }


    /*
     * ========================================
     * APAGADO DEL PLUGIN
     * ========================================
     */

    public void shutdown() {

        restoreOriginalMobCaps();
    }
}