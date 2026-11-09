
package me.difficulty.features.day20.items;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ElementalMeatConsumeListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACION - DIA 20
     * ========================================
     *
     * Regeneracion V: 4 segundos.
     * Hambre X: 5 segundos.
     * Cooldown: 12 segundos.
     */

    private static final int COOLDOWN_TICKS =
            ElementalMeat.COOLDOWN_SECONDS * 20;

    private static final long COOLDOWN_MS =
            ElementalMeat.COOLDOWN_SECONDS * 1000L;

    // 4 segundos = 80 ticks.
    private static final int REGEN_DURATION = 80;

    // 5 segundos = 100 ticks.
    private static final int HUNGER_DURATION = 100;

    private final JavaPlugin plugin;
    private final ElementalMeat elementalMeat;
    private final NamespacedKey cooldownKey;

    public ElementalMeatConsumeListener(
            JavaPlugin plugin,
            ElementalMeat elementalMeat
    ) {

        this.plugin = plugin;
        this.elementalMeat = elementalMeat;

        cooldownKey = new NamespacedKey(
                plugin,
                "elemental_meat_cooldown_until"
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
    public void onConsume(
            PlayerItemConsumeEvent event
    ) {

        /*
         * Solo nuestra Carne Elemental.
         * No afectar la carne cocinada vanilla.
         */
        if (!elementalMeat.isItem(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * ========================================
         * VERIFICAR COOLDOWN PERSISTENTE
         * ========================================
         */
        long remaining = getRemainingCooldown(player);

        if (remaining > 0L) {
            event.setCancelled(true);
            return;
        }

        /*
         * ========================================
         * REGENERACION V - 4 SEGUNDOS
         * ========================================
         *
         * Amplificador 4 = nivel V.
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
         * HAMBRE X - 5 SEGUNDOS
         * ========================================
         *
         * Amplificador 9 = nivel X.
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
         * GUARDAR COOLDOWN - 12 SEGUNDOS
         * ========================================
         */
        player.getPersistentDataContainer().set(
                cooldownKey,
                PersistentDataType.LONG,
                System.currentTimeMillis() + COOLDOWN_MS
        );

        /*
         * ========================================
         * ACTIVAR COOLDOWN VISUAL
         * ========================================
         */
        player.setCooldown(
                elementalMeat.getCooldownGroup(),
                COOLDOWN_TICKS
        );

        /*
         * Minecraft consume normalmente
         * una unidad de Carne Elemental.
         */
    }

    /*
     * ========================================
     * RESTAURAR COOLDOWN AL RECONECTAR
     * ========================================
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    long remaining =
                            getRemainingCooldown(player);

                    if (remaining <= 0L) {

                        player.getPersistentDataContainer()
                                .remove(cooldownKey);

                        return;
                    }

                    int ticks = (int) Math.ceil(
                            remaining / 50.0
                    );

                    player.setCooldown(
                            elementalMeat.getCooldownGroup(),
                            ticks
                    );
                }
        );
    }

    /*
     * ========================================
     * OBTENER TIEMPO RESTANTE
     * ========================================
     */
    private long getRemainingCooldown(
            Player player
    ) {

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