
package me.difficulty.features.day20.items;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public final class ElementalPearlTeleportListener
        implements Listener {

    private static final int SPEED_CHANCE = 80;
    private static final int SPEED_DURATION = 100;

    private final JavaPlugin plugin;
    private final ElementalPearl elementalPearl;
    private final NamespacedKey projectileKey;

    public ElementalPearlTeleportListener(
            JavaPlugin plugin,
            ElementalPearl elementalPearl
    ) {
        this.plugin = plugin;
        this.elementalPearl = elementalPearl;

        projectileKey = new NamespacedKey(
                plugin,
                "elemental_pearl_projectile"
        );
    }

    /*
     * ========================================
     * MARCAR PERLA AL LANZARLA
     * ========================================
     *
     * No modificamos el proyectil vanilla,
     * su velocidad ni su consumo.
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onLaunch(
            PlayerLaunchProjectileEvent event
    ) {

        if (!(event.getProjectile()
                instanceof EnderPearl pearl)) {
            return;
        }

        if (!elementalPearl.isItem(
                event.getItemStack()
        )) {
            return;
        }

        pearl.getPersistentDataContainer().set(
                projectileKey,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    /*
     * ========================================
     * 5% VELOCIDAD II POR 5 SEGUNDOS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onTeleport(
            PlayerTeleportEvent event
    ) {

        if (!ElementalPearlTeleportUtil
                .isElementalTeleport(
                        event,
                        projectileKey
                )) {
            return;
        }

        if (ThreadLocalRandom.current()
                .nextInt(100) >= SPEED_CHANCE) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Aplicar después del evento
         * para no interferir con el
         * teletransporte vanilla.
         */
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    if (!player.isOnline()
                            || player.isDead()) {
                        return;
                    }

                    player.addPotionEffect(
                            new PotionEffect(
                                    PotionEffectType.SPEED,
                                    SPEED_DURATION,
                                    1,
                                    false,
                                    true,
                                    true
                            )
                    );
                }
        );
    }
}