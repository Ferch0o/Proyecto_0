package me.difficulty.tasks;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import me.difficulty.mobs.day10.Gladiator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;

public class GladiatorWeaponTask extends BukkitRunnable {

    private final Gladiator gladiator;
    /*
     * 4 bloques.
     *
     * Usamos distancia al cuadrado para evitar
     * calcular raíces cuadradas innecesariamente.
     *
     * 4 × 4 = 16
     */
    private static final double SWORD_DISTANCE_SQUARED =
            16.0;


    /*
     * 6 bloques.
     *
     * 6 × 6 = 36
     */
    private static final double BOW_DISTANCE_SQUARED =
            36.0;


    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;


    /*
     * Estos métodos de reflexión se buscarán
     * una sola vez y después se reutilizarán.
     */
    private Method getHandleMethod;
    private Method reassessWeaponGoalMethod;

    private boolean reflectionErrorLogged = false;


    public GladiatorWeaponTask(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Gladiator gladiator
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
        this.gladiator = gladiator;
    }

    @Override
    public void run() {

        /*
         * Solo funciona desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Revisamos los mundos cargados.
         */
        for (World world : Bukkit.getWorlds()) {

            /*
             * Solo buscamos esqueletos.
             */
            for (Skeleton skeleton :
                    world.getEntitiesByClass(
                            Skeleton.class
                    )) {


                /*
                 * Debe ser exactamente
                 * nuestro Gladiador.
                 */
                if (!mobRegistry.is(
                        skeleton,
                        MobIds.GLADIATOR
                )) {

                    continue;
                }


                /*
                 * Ignoramos mobs muertos
                 * o inválidos.
                 */
                if (skeleton.isDead()
                        || !skeleton.isValid()) {

                    continue;
                }


                updateWeapon(
                        skeleton
                );
            }
        }
    }


    private void updateWeapon(
            Skeleton skeleton
    ) {

        /*
         * Obtenemos el objetivo actual
         * del Gladiador.
         */
        LivingEntity target =
                skeleton.getTarget();


        /*
         * Si no está persiguiendo a nadie,
         * vuelve al arco.
         */
        if (target == null
                || target.isDead()
                || !target.isValid()) {

            switchToBow(
                    skeleton
            );

            return;
        }


        double distanceSquared =
                skeleton.getLocation()
                        .distanceSquared(
                                target.getLocation()
                        );


        /*
         * ========================================
         * 4 BLOQUES O MENOS
         *
         * → ESPADA
         * ========================================
         */

        if (distanceSquared
                <= SWORD_DISTANCE_SQUARED) {

            switchToSword(
                    skeleton
            );

            return;
        }


        /*
         * ========================================
         * 6 BLOQUES O MÁS
         *
         * → ARCO
         * ========================================
         */

        if (distanceSquared
                >= BOW_DISTANCE_SQUARED) {

            switchToBow(
                    skeleton
            );
        }


        /*
         * Entre 4 y 6 bloques:
         *
         * no hacemos nada.
         *
         * Mantiene su arma actual.
         */
    }


    /*
     * ========================================
     * CAMBIAR A ESPADA
     * ========================================
     */

    private void switchToSword(
            Skeleton skeleton
    ) {

        EntityEquipment equipment =
                skeleton.getEquipment();

        if (equipment == null) {
            return;
        }


        /*
         * Si ya tiene espada,
         * no hacemos nada.
         */
        if (equipment
                .getItemInMainHand()
                .getType()
                == Material.NETHERITE_SWORD) {

            return;
        }


        /*
         * Ponemos la espada directamente
         * en la mano principal.
         */
        equipment.setItemInMainHand(
                gladiator.createSword()
        );


        /*
         * MUY IMPORTANTE:
         *
         * Eliminamos completamente el arco
         * de la mano secundaria.
         *
         * Si dejamos un arco ahí,
         * Minecraft puede seguir considerando
         * que el esqueleto está usando un arco.
         */
        equipment.setItemInOffHand(
                new ItemStack(
                        Material.AIR
                )
        );


        /*
         * Ahora sí reevaluamos
         * la IA de combate.
         */
        refreshCombatGoal(
                skeleton
        );
    }


    /*
     * ========================================
     * CAMBIAR A ARCO
     * ========================================
     */

    private void switchToBow(
            Skeleton skeleton
    ) {

        EntityEquipment equipment =
                skeleton.getEquipment();

        if (equipment == null) {
            return;
        }


        /*
         * Ya tiene arco.
         */
        if (equipment
                .getItemInMainHand()
                .getType()
                == Material.BOW) {

            return;
        }


        /*
         * Volvemos a crear el arco
         * original del Gladiador.
         */
        equipment.setItemInMainHand(
                gladiator.createBow()
        );


        /*
         * La mano secundaria
         * permanece vacía.
         */
        equipment.setItemInOffHand(
                new ItemStack(
                        Material.AIR
                )
        );


        /*
         * Reevaluamos nuevamente
         * la IA del esqueleto.
         */
        refreshCombatGoal(
                skeleton
        );
    }


    /*
     * ========================================
     * ACTUALIZAR IA DEL ESQUELETO
     * ========================================
     *
     * Minecraft utiliza una IA diferente
     * dependiendo de si el esqueleto tiene
     * arco o arma cuerpo a cuerpo.
     *
     * reassessWeaponGoal()
     * vuelve a comprobar qué IA necesita.
     */

    private void refreshCombatGoal(
            Skeleton skeleton
    ) {

        try {

            /*
             * Obtenemos CraftSkeleton#getHandle().
             *
             * Solo buscamos el Method
             * la primera vez.
             */
            if (getHandleMethod == null) {

                getHandleMethod =
                        skeleton.getClass()
                                .getMethod(
                                        "getHandle"
                                );
            }


            Object handle =
                    getHandleMethod.invoke(
                            skeleton
                    );


            /*
             * Buscamos reassessWeaponGoal()
             * solamente la primera vez.
             */
            if (reassessWeaponGoalMethod
                    == null) {

                reassessWeaponGoalMethod =
                        findMethod(
                                handle.getClass(),
                                "reassessWeaponGoal"
                        );

                reassessWeaponGoalMethod
                        .setAccessible(
                                true
                        );
            }


            reassessWeaponGoalMethod.invoke(
                    handle
            );

        } catch (Exception exception) {

            /*
             * No llenamos la consola de errores
             * cada medio segundo.
             *
             * Si falla, mostramos el aviso
             * solamente una vez.
             */
            if (!reflectionErrorLogged) {

                reflectionErrorLogged = true;

                plugin.getLogger().warning(
                        "No se pudo actualizar la IA "
                                + "del Gladiador al cambiar de arma: "
                                + exception.getMessage()
                );
            }
        }
    }


    /*
     * Busca un método incluso si está
     * declarado en una superclase.
     */
    private Method findMethod(
            Class<?> startingClass,
            String methodName
    ) throws NoSuchMethodException {

        Class<?> currentClass =
                startingClass;


        while (currentClass != null) {

            try {

                return currentClass
                        .getDeclaredMethod(
                                methodName
                        );

            } catch (NoSuchMethodException ignored) {

                currentClass =
                        currentClass.getSuperclass();
            }
        }


        throw new NoSuchMethodException(
                methodName
        );
    }
}