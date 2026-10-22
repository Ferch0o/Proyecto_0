package me.difficulty.tasks;

import me.difficulty.day.DayManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HorseEffectTask extends BukkitRunnable {

    private final DayManager dayManager;


    public HorseEffectTask(
            DayManager dayManager
    ) {

        this.dayManager = dayManager;
    }


    @Override
    public void run() {

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
         * REGENERACIÓN II AL JINETE
         * ========================================
         */

        updateRiders();


        /*
         * ========================================
         * RESISTENCIA V CON ARMADURA
         * ========================================
         */

        updateHorseArmor();
    }


    /*
     * ========================================
     * JUGADORES MONTANDO CABALLO
     * ========================================
     */

    private void updateRiders() {

        for (Player player :
                Bukkit.getOnlinePlayers()) {


            /*
             * ¿El jugador está montando
             * un caballo normal?
             */
            if (!(player.getVehicle()
                    instanceof Horse)) {

                continue;
            }


            /*
             * ====================================
             * REGENERACIÓN II
             * ====================================
             *
             * 30 ticks = 1.5 segundos.
             *
             * Como esta tarea se ejecutará
             * cada 10 ticks, el efecto se
             * renueva constantemente mientras
             * el jugador siga montado.
             */

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.REGENERATION,
                            30,
                            1,
                            false,
                            true
                    )
            );
        }
    }


    /*
     * ========================================
     * ARMADURA DEL CABALLO
     * ========================================
     */

    private void updateHorseArmor() {

        for (World world :
                Bukkit.getWorlds()) {

            for (Horse horse :
                    world.getEntitiesByClass(
                            Horse.class
                    )) {


                ItemStack armor =
                        horse.getInventory()
                                .getArmor();


                /*
                 * No tiene armadura.
                 */
                if (armor == null
                        || armor.getType()
                        == Material.AIR) {

                    continue;
                }


                /*
                 * =================================
                 * RESISTENCIA V
                 * =================================
                 *
                 * Funciona con cualquier
                 * armadura de caballo:
                 *
                 * - cuero
                 * - hierro
                 * - oro
                 * - diamante
                 */

                horse.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.RESISTANCE,
                                30,
                                1,
                                false,
                                true
                        )
                );
            }
        }
    }
}