
package me.difficulty.features.day7.gigante;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.entity.Giant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class GiantDropListener implements Listener {

    private static final int NETHERITE_CHANCE = 80;

    private final DayManager dayManager;
    private final GiantZombie giantZombie;

    public GiantDropListener(
            DayManager dayManager,
            GiantZombie giantZombie
    ) {
        this.dayManager = dayManager;
        this.giantZombie = giantZombie;
    }

    @EventHandler
    public void onGiantDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Giant giant)) {
            return;
        }

        // Solo nuestros Gigantes personalizados
        if (!giantZombie.isMob(giant)) {
            return;
        }

        int day = dayManager.getCurrentDay();

        // Recompensa disponible desde el dia 7 al 9
        if (day < 7 || day >= 10) {
            return;
        }

        // Probabilidad del 80 %
        if (ThreadLocalRandom.current()
                .nextInt(100) >= NETHERITE_CHANCE) {
            return;
        }

        event.getDrops().add(
                new ItemStack(
                        Material.NETHERITE_INGOT,
                        1
                )
        );
    }
}