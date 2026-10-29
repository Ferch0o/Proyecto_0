
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

    // Probabilidad de soltar fragmentos
    private static final int NETHERITE_CHANCE = 80;

    // Cantidad aleatoria
    private static final int MIN_SCRAP = 4;
    private static final int MAX_SCRAP = 8;

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

        // Recompensa disponible en dias 7, 8 y 9
        if (day < 7 || day >= 10) {
            return;
        }

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        // Probabilidad del 80%
        if (random.nextInt(100) >= NETHERITE_CHANCE) {
            return;
        }

        // Cantidad entre 4 y 7, inclusive
        int amount = random.nextInt(
                MIN_SCRAP,
                MAX_SCRAP + 1
        );

        event.getDrops().add(
                new ItemStack(
                        Material.NETHERITE_SCRAP,
                        amount
                )
        );
    }
}