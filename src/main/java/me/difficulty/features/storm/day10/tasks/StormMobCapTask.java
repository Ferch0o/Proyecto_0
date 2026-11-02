
package me.difficulty.features.storm.day10.tasks;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.storm.core.StormManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class StormMobCapTask extends BukkitRunnable {

    private final DayManager dayManager;
    private final StormManager stormManager;

    /*
     * ========================================
     * MOB CAP ORIGINAL
     * ========================================
     *
     * Guardamos el limite original
     * de cada mundo para evitar:
     *
     * 70 -> 140 -> 280 -> 560...
     */
    private final Map<UUID, Integer> originalMonsterCaps =
            new HashMap<>();

    /*
     * Ultimo multiplicador aplicado
     * a cada mundo.
     */
    private final Map<UUID, Integer> appliedMultipliers =
            new HashMap<>();

    public StormMobCapTask(
            DayManager dayManager,
            StormManager stormManager
    ) {
        this.dayManager = dayManager;
        this.stormManager = stormManager;
    }

    /*
     * ========================================
     * EJECUCION CADA SEGUNDO
     * ========================================
     */
    @Override
    public void run() {

        int multiplier = getCurrentMultiplier();

        /*
         * Multiplicador x1:
         * restaurar el valor original.
         */
        if (multiplier == 1) {

            restoreOriginalMobCaps();
            return;
        }

        /*
         * Multiplicador x2 o x3.
         */
        applyMultiplier(multiplier);
    }

    /*
     * ========================================
     * MULTIPLICADOR SEGUN DIA Y TORMENTA
     * ========================================
     */
    private int getCurrentMultiplier() {

        /*
         * Desde el dia 17:
         *
         * Sin Tormenta = x2
         * Con Tormenta = x3
         */
        if (dayManager.isAtLeast(17)) {

            return stormManager.hasDay10Effects()
                    ? 3
                    : 2;
        }

        /*
         * Dias 10-16:
         *
         * Sin Tormenta = x1
         * Con Tormenta = x2
         *
         * Dias 0-9 = x1.
         */
        if (stormManager.hasDay10Effects()) {
            return 2;
        }

        return 1;
    }

    /*
     * ========================================
     * APLICAR MULTIPLICADOR
     * ========================================
     */
    private void applyMultiplier(int multiplier) {

        for (World world : Bukkit.getWorlds()) {

            UUID worldId = world.getUID();

            /*
             * Obtener el limite ORIGINAL.
             *
             * Si ya lo guardamos, reutilizarlo
             * en vez del valor multiplicado.
             */
            Integer original =
                    originalMonsterCaps.get(worldId);

            if (original == null) {

                original = world.getSpawnLimit(
                        SpawnCategory.MONSTER
                );

                originalMonsterCaps.put(
                        worldId,
                        original
                );
            }

            /*
             * Un valor -1 significa que
             * el mundo utiliza el limite
             * global del servidor.
             */
            int baseLimit = original;

            if (baseLimit < 0) {

                baseLimit = Bukkit.getSpawnLimit(
                        SpawnCategory.MONSTER
                );
            }

            /*
             * Si la generacion de monstruos
             * esta deshabilitada, respetarla.
             */
            if (baseLimit <= 0) {

                if (world.getSpawnLimit(
                        SpawnCategory.MONSTER
                ) != original) {

                    world.setSpawnLimit(
                            SpawnCategory.MONSTER,
                            original
                    );
                }

                appliedMultipliers.remove(worldId);
                continue;
            }

            /*
             * ====================================
             * CALCULAR EL NUEVO LIMITE
             * ====================================
             *
             * Siempre usar el limite original.
             *
             * Ejemplo:
             *
             * 70 x 2 = 140
             * 70 x 3 = 210
             */
            long calculated =
                    (long) baseLimit * multiplier;

            int newLimit = (int) Math.min(
                    Integer.MAX_VALUE,
                    calculated
            );

            /*
             * Actualizar solamente si
             * el valor es diferente.
             */
            if (world.getSpawnLimit(
                    SpawnCategory.MONSTER
            ) != newLimit) {

                world.setSpawnLimit(
                        SpawnCategory.MONSTER,
                        newLimit
                );
            }

            /*
             * Evitar imprimir el mismo mensaje
             * cada segundo.
             */
            Integer previousMultiplier =
                    appliedMultipliers.put(
                            worldId,
                            multiplier
                    );

            if (previousMultiplier == null
                    || previousMultiplier != multiplier) {

                Bukkit.getLogger().info(
                        "[DifficultyPlugin] MobCap "
                                + world.getName()
                                + ": "
                                + baseLimit
                                + " -> "
                                + newLimit
                                + " (x"
                                + multiplier
                                + ")"
                );
            }
        }
    }

    /*
     * ========================================
     * RESTAURAR LIMITE ORIGINAL
     * ========================================
     */
    public void restoreOriginalMobCaps() {

        if (originalMonsterCaps.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator =
                originalMonsterCaps.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, Integer> entry =
                    iterator.next();

            World world = Bukkit.getWorld(
                    entry.getKey()
            );

            /*
             * Si el mundo esta descargado,
             * conservar el registro para
             * restaurarlo cuando vuelva
             * a estar disponible.
             */
            if (world == null) {
                continue;
            }

            int originalLimit = entry.getValue();

            if (world.getSpawnLimit(
                    SpawnCategory.MONSTER
            ) != originalLimit) {

                world.setSpawnLimit(
                        SpawnCategory.MONSTER,
                        originalLimit
                );

                Bukkit.getLogger().info(
                        "[DifficultyPlugin] MobCap restaurado: "
                                + world.getName()
                                + " -> "
                                + originalLimit
                );
            }

            appliedMultipliers.remove(
                    entry.getKey()
            );

            iterator.remove();
        }
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