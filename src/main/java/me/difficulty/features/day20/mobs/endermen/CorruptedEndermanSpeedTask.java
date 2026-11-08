
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class CorruptedEndermanSpeedTask
        extends BukkitRunnable {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     *
     * 20 ticks = 1 segundo.
     *
     * La tarea se ejecutará cada
     * 10 ticks para renovar el efecto
     * mientras el Enderman esté enfadado.
     */
    private static final int SPEED_DURATION = 20;
    private static final int SPEED_AMPLIFIER = 0;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public CorruptedEndermanSpeedTask(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    @Override
    public void run() {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Revisar Endermen de los mundos
         * que estén cargados.
         */
        for (World world : Bukkit.getWorlds()) {

            for (Enderman enderman :
                    world.getEntitiesByClass(
                            Enderman.class
                    )) {

                if (!enderman.isValid()
                        || enderman.isDead()) {
                    continue;
                }

                /*
                 * Solo nuestra variante.
                 */
                if (!registry.is(
                        enderman,
                        MobIds.CORRUPTED_ENDERMAN
                )) {
                    continue;
                }

                /*
                 * ========================================
                 * ESTADO DE ENFADO
                 * ========================================
                 *
                 * Un objetivo activo indica
                 * que está persiguiendo
                 * o combatiendo.
                 */
                if (enderman.getTarget() == null) {
                    continue;
                }

                /*
                 * ========================================
                 * VELOCIDAD I
                 * ========================================
                 *
                 * No reemplazar una Velocidad
                 * de nivel superior.
                 */
                PotionEffect current =
                        enderman.getPotionEffect(
                                PotionEffectType.SPEED
                        );

                if (current != null) {

                    if (current.getAmplifier()
                            > SPEED_AMPLIFIER) {
                        continue;
                    }

                    /*
                     * Evitar aplicar el efecto
                     * innecesariamente si aún
                     * tiene duración suficiente.
                     */
                    if (current.getDuration() > 10) {
                        continue;
                    }
                }

                enderman.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.SPEED,
                                SPEED_DURATION,
                                SPEED_AMPLIFIER,
                                false,
                                false,
                                true
                        )
                );
            }
        }
    }
}
