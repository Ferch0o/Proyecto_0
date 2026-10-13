package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.items.day7.CorruptedStar;
import me.difficulty.utils.InteractionPriority;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class CorruptedStarListener implements Listener {

    private final CorruptedStar corruptedStar;
    private final DayManager dayManager;

    public CorruptedStarListener(
            CorruptedStar corruptedStar,
            DayManager dayManager
    ) {
        this.corruptedStar = corruptedStar;
        this.dayManager = dayManager;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!corruptedStar.isCorruptedStar(event.getItem())) {
            return;
        }

        /*
         * Si estamos haciendo clic derecho sobre
         * una mesa de crafteo, la mesa tiene prioridad.
         */
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && InteractionPriority.shouldBlockHavePriority(
                event.getClickedBlock()
        )) {

            return;
        }

        event.setUseItemInHand(Event.Result.DENY);

        Player player = event.getPlayer();

        if (!dayManager.isAtLeast(7)) {

            player.sendMessage(
                    ChatColor.RED
                            + "La Estrella Corrupta todavía no puede utilizarse."
            );

            return;
        }

        // Comprobar cooldown
        if (player.hasCooldown(event.getItem())) {

            int ticks = player.getCooldown(event.getItem());

            int seconds = (int) Math.ceil(ticks / 20.0);

            player.sendMessage(
                    ChatColor.RED
                            + "Debes esperar "
                            + seconds
                            + " segundos para volver a usarla."
            );

            return;
        }

        // Velocidad III durante 30 segundos
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        30 * 20,
                        2
                )
        );

        // 5% de probabilidad de Debilidad V
        boolean weakness =
                ThreadLocalRandom.current().nextInt(100) < 5;

        if (weakness) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.WEAKNESS,
                            60 * 20,
                            4
                    )
            );

            player.sendMessage(
                    ChatColor.DARK_RED
                            + "¡La energía corrupta te ha debilitado!"
            );
        }

        // Cooldown de 60 segundos
        player.setCooldown(
                event.getItem(),
                60 * 20
        );

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_BEACON_ACTIVATE,
                1.0f,
                1.4f
        );

        player.sendMessage(
                ChatColor.LIGHT_PURPLE
                        + "Has utilizado la Estrella Corrupta."
        );
    }
}