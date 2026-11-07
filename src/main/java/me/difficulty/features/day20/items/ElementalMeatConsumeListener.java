
package me.difficulty.features.day20.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.NamespacedKey;

import org.bukkit.entity.Player;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ElementalMeatConsumeListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */
    private static final long COOLDOWN_MS = 15_000L;

    private static final int REGEN_DURATION = 40;
    private static final int HUNGER_DURATION = 80;
    private static final int EFFECT_DURATION = 60;

    private final ElementalMeat elementalMeat;
    private final NamespacedKey cooldownKey;

    public ElementalMeatConsumeListener(
            JavaPlugin plugin,
            ElementalMeat elementalMeat
    ) {
        this.elementalMeat = elementalMeat;

        this.cooldownKey = new NamespacedKey(
                plugin,
                "elemental_meat_cooldown_until"
        );
    }

    /*
     * ========================================
     * BLOQUEAR USO DURANTE COOLDOWN
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInteract(PlayerInteractEvent event) {

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!elementalMeat.isItem(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();

        long remaining = getRemainingCooldown(player);

        if (remaining <= 0L) {
            return;
        }

        /*
         * Impedir iniciar el consumo.
         * No bloqueamos la interacción
         * normal con cofres o puertas.
         */
        event.setUseItemInHand(Event.Result.DENY);

        long seconds = (remaining + 999L) / 1000L;

        player.sendActionBar(
                Component.text(
                        "Carne Elemental: espera "
                                + seconds + " s.",
                        NamedTextColor.RED
                )
        );
    }

    /*
     * ========================================
     * CONSUMIR CARNE ELEMENTAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onConsume(PlayerItemConsumeEvent event) {

        if (!elementalMeat.isItem(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Segunda comprobación para evitar
         * cualquier consumo durante cooldown.
         */
        if (getRemainingCooldown(player) > 0L) {

            event.setCancelled(true);
            return;
        }

        /*
         * ========================================
         * REGENERACIÓN V - 2 SEGUNDOS
         * ========================================
         */
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.REGENERATION,
                        REGEN_DURATION,
                        4,
                        false,
                        true,
                        true
                )
        );

        /*
         * ========================================
         * HAMBRE X - 4 SEGUNDOS
         * ========================================
         */
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.HUNGER,
                        HUNGER_DURATION,
                        9,
                        false,
                        true,
                        true
                )
        );

        /*
         * ========================================
         * ACTIVAR COOLDOWN - 5 SEGUNDOS
         * ========================================
         */
        player.getPersistentDataContainer().set(
                cooldownKey,
                PersistentDataType.LONG,
                System.currentTimeMillis() + COOLDOWN_MS
        );

        /*
         * Dejar que Minecraft consuma
         * una unidad normalmente.
         */
    }

    /*
     * ========================================
     * TIEMPO RESTANTE
     * ========================================
     */
    private long getRemainingCooldown(Player player) {

        long until = player.getPersistentDataContainer()
                .getOrDefault(
                        cooldownKey,
                        PersistentDataType.LONG,
                        0L
                );

        return Math.max(
                0L,
                until - System.currentTimeMillis()
        );
    }
}
