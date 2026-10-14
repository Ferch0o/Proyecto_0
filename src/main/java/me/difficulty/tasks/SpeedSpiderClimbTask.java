package me.difficulty.tasks;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Spider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpeedSpiderClimbTask
        extends BukkitRunnable {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;


    public SpeedSpiderClimbTask(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
    }


    @Override
    public void run() {

        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        for (World world : Bukkit.getWorlds()) {

            for (Spider spider :
                    world.getEntitiesByClass(
                            Spider.class
                    )) {


                /*
                 * Solo nuestra Araña Velocista.
                 */
                if (!mobRegistry.is(
                        spider,
                        MobIds.SPEED_SPIDER
                )) {

                    continue;
                }


                /*
                 * Ignoramos arañas muertas
                 * o inválidas.
                 */
                if (spider.isDead()
                        || !spider.isValid()) {

                    continue;
                }


                /*
                 * Paper puede decirnos directamente
                 * si la araña está trepando.
                 */
                if (!spider.isClimbing()) {
                    continue;
                }


                /*
                 * Aumentamos únicamente
                 * la velocidad vertical.
                 */
                Vector velocity =
                        spider.getVelocity();


                /*
                 * No queremos reducir una velocidad
                 * vertical que ya sea superior.
                 */
                if (velocity.getY() < 0.40) {

                    velocity.setY(
                            0.40
                    );

                    spider.setVelocity(
                            velocity
                    );
                }
            }
        }
    }
}