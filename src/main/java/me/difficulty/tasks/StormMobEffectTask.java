package me.difficulty.tasks;

import me.difficulty.storm.StormManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class StormMobEffectTask extends BukkitRunnable {

    private final StormManager stormManager;


    /*
     * ========================================
     * MARCA GENERAL
     * ========================================
     */

    private final NamespacedKey stormBoostKey;


    /*
     * ========================================
     * DATOS ORIGINALES DE FUERZA
     * ========================================
     */

    private final NamespacedKey strengthPresentKey;
    private final NamespacedKey strengthAmplifierKey;
    private final NamespacedKey strengthDurationKey;
    private final NamespacedKey strengthAmbientKey;
    private final NamespacedKey strengthParticlesKey;
    private final NamespacedKey strengthIconKey;


    /*
     * ========================================
     * DATOS ORIGINALES DE RESISTENCIA
     * ========================================
     */

    private final NamespacedKey resistancePresentKey;
    private final NamespacedKey resistanceAmplifierKey;
    private final NamespacedKey resistanceDurationKey;
    private final NamespacedKey resistanceAmbientKey;
    private final NamespacedKey resistanceParticlesKey;
    private final NamespacedKey resistanceIconKey;


    public StormMobEffectTask(
            JavaPlugin plugin,
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;


        /*
         * ========================================
         * CREAR KEYS
         * ========================================
         */

        stormBoostKey =
                new NamespacedKey(
                        plugin,
                        "storm_mob_boost"
                );


        /*
         * Fuerza.
         */

        strengthPresentKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_present"
                );

        strengthAmplifierKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_amplifier"
                );

        strengthDurationKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_duration"
                );

        strengthAmbientKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_ambient"
                );

        strengthParticlesKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_particles"
                );

        strengthIconKey =
                new NamespacedKey(
                        plugin,
                        "storm_strength_icon"
                );


        /*
         * Resistencia.
         */

        resistancePresentKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_present"
                );

        resistanceAmplifierKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_amplifier"
                );

        resistanceDurationKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_duration"
                );

        resistanceAmbientKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_ambient"
                );

        resistanceParticlesKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_particles"
                );

        resistanceIconKey =
                new NamespacedKey(
                        plugin,
                        "storm_resistance_icon"
                );
    }


    @Override
    public void run() {

        /*
         * ========================================
         * RECORRER TODOS LOS MUNDOS
         * ========================================
         */

        for (World world :
                Bukkit.getWorlds()) {


            /*
             * Recorremos entidades vivas.
             */
            for (LivingEntity entity :
                    world.getLivingEntities()) {


                /*
                 * ====================================
                 * SOLO MOBS
                 * ====================================
                 *
                 * Esto excluye jugadores y otras
                 * LivingEntity que no son mobs.
                 */

                if (!(entity instanceof Mob mob)) {
                    continue;
                }


                /*
                 * ====================================
                 * TORMENTA ACTIVA
                 * ====================================
                 */

                if (stormManager.isActive()) {

                    applyStormBoost(
                            mob
                    );

                } else {

                    /*
                     * =================================
                     * TORMENTA INACTIVA
                     * =================================
                     *
                     * Si este mob fue modificado
                     * anteriormente, restauramos
                     * sus efectos originales.
                     */

                    restoreStormBoost(
                            mob
                    );
                }
            }
        }
    }


    /*
     * ========================================
     * APLICAR BONUS DE TORMENTA
     * ========================================
     */

    private void applyStormBoost(
            Mob mob
    ) {

        PersistentDataContainer data =
                mob.getPersistentDataContainer();


        /*
         * ========================================
         * PRIMERA VEZ
         * ========================================
         *
         * Guardamos los efectos originales
         * antes de modificarlos.
         */

        if (!data.has(
                stormBoostKey,
                PersistentDataType.BYTE
        )) {

            saveOriginalStrength(
                    mob
            );


            saveOriginalResistance(
                    mob
            );


            data.set(
                    stormBoostKey,
                    PersistentDataType.BYTE,
                    (byte) 1
            );
        }


        /*
         * ========================================
         * MANTENER FUERZA
         * ========================================
         */

        maintainBoostedEffect(
                mob,
                PotionEffectType.STRENGTH,
                strengthPresentKey,
                strengthAmplifierKey
        );


        /*
         * ========================================
         * MANTENER RESISTENCIA
         * ========================================
         */

        maintainBoostedEffect(
                mob,
                PotionEffectType.RESISTANCE,
                resistancePresentKey,
                resistanceAmplifierKey
        );
    }


    /*
     * ========================================
     * GUARDAR FUERZA ORIGINAL
     * ========================================
     */

    private void saveOriginalStrength(
            Mob mob
    ) {

        saveOriginalEffect(
                mob,
                PotionEffectType.STRENGTH,

                strengthPresentKey,
                strengthAmplifierKey,
                strengthDurationKey,
                strengthAmbientKey,
                strengthParticlesKey,
                strengthIconKey
        );
    }


    /*
     * ========================================
     * GUARDAR RESISTENCIA ORIGINAL
     * ========================================
     */

    private void saveOriginalResistance(
            Mob mob
    ) {

        saveOriginalEffect(
                mob,
                PotionEffectType.RESISTANCE,

                resistancePresentKey,
                resistanceAmplifierKey,
                resistanceDurationKey,
                resistanceAmbientKey,
                resistanceParticlesKey,
                resistanceIconKey
        );
    }


    /*
     * ========================================
     * GUARDAR EFECTO ORIGINAL
     * ========================================
     */

    private void saveOriginalEffect(
            Mob mob,
            PotionEffectType type,

            NamespacedKey presentKey,
            NamespacedKey amplifierKey,
            NamespacedKey durationKey,
            NamespacedKey ambientKey,
            NamespacedKey particlesKey,
            NamespacedKey iconKey
    ) {

        PersistentDataContainer data =
                mob.getPersistentDataContainer();


        PotionEffect effect =
                mob.getPotionEffect(
                        type
                );


        /*
         * ========================================
         * NO TENÍA EL EFECTO
         * ========================================
         */

        if (effect == null) {

            data.set(
                    presentKey,
                    PersistentDataType.BYTE,
                    (byte) 0
            );


            return;
        }


        /*
         * ========================================
         * SÍ TENÍA EL EFECTO
         * ========================================
         */

        data.set(
                presentKey,
                PersistentDataType.BYTE,
                (byte) 1
        );


        data.set(
                amplifierKey,
                PersistentDataType.INTEGER,
                effect.getAmplifier()
        );


        data.set(
                durationKey,
                PersistentDataType.INTEGER,
                effect.getDuration()
        );


        data.set(
                ambientKey,
                PersistentDataType.BYTE,
                booleanToByte(
                        effect.isAmbient()
                )
        );


        data.set(
                particlesKey,
                PersistentDataType.BYTE,
                booleanToByte(
                        effect.hasParticles()
                )
        );


        data.set(
                iconKey,
                PersistentDataType.BYTE,
                booleanToByte(
                        effect.hasIcon()
                )
        );
    }


    /*
     * ========================================
     * MANTENER EFECTO +1
     * ========================================
     */

    private void maintainBoostedEffect(
            Mob mob,
            PotionEffectType type,

            NamespacedKey presentKey,
            NamespacedKey amplifierKey
    ) {

        PersistentDataContainer data =
                mob.getPersistentDataContainer();


        boolean originallyPresent =
                data.getOrDefault(
                        presentKey,
                        PersistentDataType.BYTE,
                        (byte) 0
                ) == (byte) 1;


        /*
         * ========================================
         * CALCULAR NIVEL DE TORMENTA
         * ========================================
         *
         * Si NO tenía efecto:
         *
         * originalAmplifier = -1
         * -1 + 1 = 0
         * amplifier 0 = nivel I
         *
         *
         * Si tenía Fuerza III:
         *
         * amplifier original = 2
         * 2 + 1 = 3
         * amplifier 3 = Fuerza IV
         */

        int originalAmplifier;


        if (originallyPresent) {

            originalAmplifier =
                    data.getOrDefault(
                            amplifierKey,
                            PersistentDataType.INTEGER,
                            0
                    );

        } else {

            originalAmplifier =
                    -1;
        }


        int stormAmplifier =
                originalAmplifier + 1;


        PotionEffect current =
                mob.getPotionEffect(
                        type
                );


        /*
         * ========================================
         * ¿NECESITA ACTUALIZARSE?
         * ========================================
         *
         * Esto también corrige casos donde
         * otra tarea del plugin vuelva a aplicar
         * el nivel normal.
         *
         * Ejemplo:
         *
         * gato Fuerza III
         * ↓
         * Tormenta → Fuerza IV
         *
         * PetBehaviorTask vuelve a poner III
         * ↓
         * esta tarea vuelve a poner IV
         */

        if (current == null
                ||
                current.getAmplifier()
                        < stormAmplifier
                ||
                current.getDuration()
                        != PotionEffect.INFINITE_DURATION) {


            mob.addPotionEffect(
                    new PotionEffect(
                            type,
                            PotionEffect.INFINITE_DURATION,
                            stormAmplifier,
                            false,
                            true,
                            true
                    ),
                    true
            );
        }
    }


    /*
     * ========================================
     * RESTAURAR MOB
     * ========================================
     */

    private void restoreStormBoost(
            Mob mob
    ) {

        PersistentDataContainer data =
                mob.getPersistentDataContainer();


        /*
         * Este mob nunca recibió
         * el bonus de Tormenta.
         */
        if (!data.has(
                stormBoostKey,
                PersistentDataType.BYTE
        )) {

            return;
        }


        /*
         * Restaurar Fuerza.
         */
        restoreEffect(
                mob,
                PotionEffectType.STRENGTH,

                strengthPresentKey,
                strengthAmplifierKey,
                strengthDurationKey,
                strengthAmbientKey,
                strengthParticlesKey,
                strengthIconKey
        );


        /*
         * Restaurar Resistencia.
         */
        restoreEffect(
                mob,
                PotionEffectType.RESISTANCE,

                resistancePresentKey,
                resistanceAmplifierKey,
                resistanceDurationKey,
                resistanceAmbientKey,
                resistanceParticlesKey,
                resistanceIconKey
        );


        /*
         * Eliminar marca general.
         */
        data.remove(
                stormBoostKey
        );
    }


    /*
     * ========================================
     * RESTAURAR EFECTO ORIGINAL
     * ========================================
     */

    private void restoreEffect(
            Mob mob,
            PotionEffectType type,

            NamespacedKey presentKey,
            NamespacedKey amplifierKey,
            NamespacedKey durationKey,
            NamespacedKey ambientKey,
            NamespacedKey particlesKey,
            NamespacedKey iconKey
    ) {

        PersistentDataContainer data =
                mob.getPersistentDataContainer();


        boolean originallyPresent =
                data.getOrDefault(
                        presentKey,
                        PersistentDataType.BYTE,
                        (byte) 0
                ) == (byte) 1;


        /*
         * Primero eliminamos el efecto
         * otorgado por la Tormenta.
         */

        mob.removePotionEffect(
                type
        );


        /*
         * ========================================
         * EL MOB YA TENÍA EL EFECTO
         * ========================================
         */

        if (originallyPresent) {

            int amplifier =
                    data.getOrDefault(
                            amplifierKey,
                            PersistentDataType.INTEGER,
                            0
                    );


            int duration =
                    data.getOrDefault(
                            durationKey,
                            PersistentDataType.INTEGER,
                            PotionEffect.INFINITE_DURATION
                    );


            boolean ambient =
                    byteToBoolean(
                            data.getOrDefault(
                                    ambientKey,
                                    PersistentDataType.BYTE,
                                    (byte) 0
                            )
                    );


            boolean particles =
                    byteToBoolean(
                            data.getOrDefault(
                                    particlesKey,
                                    PersistentDataType.BYTE,
                                    (byte) 1
                            )
                    );


            boolean icon =
                    byteToBoolean(
                            data.getOrDefault(
                                    iconKey,
                                    PersistentDataType.BYTE,
                                    (byte) 1
                            )
                    );


            mob.addPotionEffect(
                    new PotionEffect(
                            type,
                            duration,
                            amplifier,
                            ambient,
                            particles,
                            icon
                    ),
                    true
            );
        }


        /*
         * ========================================
         * LIMPIAR DATOS GUARDADOS
         * ========================================
         */

        data.remove(
                presentKey
        );

        data.remove(
                amplifierKey
        );

        data.remove(
                durationKey
        );

        data.remove(
                ambientKey
        );

        data.remove(
                particlesKey
        );

        data.remove(
                iconKey
        );
    }


    /*
     * ========================================
     * BOOLEAN ↔ BYTE
     * ========================================
     */

    private byte booleanToByte(
            boolean value
    ) {

        return value
                ? (byte) 1
                : (byte) 0;
    }


    private boolean byteToBoolean(
            byte value
    ) {

        return value
                == (byte) 1;
    }
}