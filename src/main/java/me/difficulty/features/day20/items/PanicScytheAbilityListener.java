
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import org.bukkit.entity.Player;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PanicScytheAbilityListener
        implements Listener {

    /*
     * ========================================
     * GUADAÑA DEL PANICO - DIA 20
     * ========================================
     *
     * Distancia: 7 bloques.
     * Direccion: mirada del jugador.
     * Atraviesa paredes y bloques.
     *
     * Costo: 2 bloques de oro.
     * Cooldown: 1 segundo.
     */

    private static final int REQUIRED_DAY = 20;

    private static final int GOLD_COST = 2;

    private static final double TELEPORT_DISTANCE = 7.0;

    private static final long COOLDOWN_MS = 1000L;

    private final DayManager dayManager;
    private final PanicScythe panicScythe;

    private final Map<UUID, Long> cooldowns =
            new HashMap<>();

    public PanicScytheAbilityListener(
            DayManager dayManager,
            PanicScythe panicScythe
    ) {

        this.dayManager = dayManager;
        this.panicScythe = panicScythe;
    }

    /*
     * ========================================
     * ACTIVAR HABILIDAD
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(
            PlayerInteractEvent event
    ) {

        /*
         * Solo mano principal.
         */
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Solo Guadaña del Pánico auténtica.
         */
        if (!panicScythe.isItem(
                player.getInventory().getItemInMainHand()
        )) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * Bloquear uso vanilla de la azada.
         */
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        /*
         * ========================================
         * COMPROBAR COOLDOWN
         * ========================================
         */
        long now = System.currentTimeMillis();

        long nextUse = cooldowns.getOrDefault(
                player.getUniqueId(),
                0L
        );

        if (now < nextUse) {
            return;
        }

        /*
         * ========================================
         * COMPROBAR BLOQUES DE ORO
         * ========================================
         */
        if (countGoldBlocks(player) < GOLD_COST) {

            player.sendActionBar(
                    Component.text(
                            "Necesitas 2 bloques de oro.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        /*
         * ========================================
         * DESTINO DE TELETRANSPORTE
         * ========================================
         *
         * La direccion incluye:
         *
         * - Horizontal.
         * - Hacia arriba.
         * - Hacia abajo.
         *
         * No ignoramos la inclinacion
         * de la mirada del jugador.
         */
        Location origin = player.getLocation();

        Vector direction =
                origin.getDirection().normalize();

        Location destination = origin.clone().add(
                direction.multiply(TELEPORT_DISTANCE)
        );

        /*
         * ========================================
         * LIMITES DEL MUNDO
         * ========================================
         *
         * Permitimos atravesar paredes y
         * aparecer dentro de bloques.
         *
         * Solo evitamos coordenadas fuera
         * de los limites del mundo.
         */
        World world = origin.getWorld();

        if (destination.getY() < world.getMinHeight()
                || destination.getY() + 1.8
                >= world.getMaxHeight()
                || !world.getWorldBorder()
                .isInside(destination)) {

            player.sendActionBar(
                    Component.text(
                            "Destino fuera de los límites del mundo.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        /*
         * ========================================
         * TELETRANSPORTE
         * ========================================
         *
         * No comprobar:
         *
         * - Paredes.
         * - Bloques sólidos.
         * - Suelo debajo del jugador.
         * - Espacio libre para el cuerpo.
         *
         * El jugador puede quedar
         * atrapado dentro de bloques.
         */
        boolean teleported = player.teleport(
                destination,
                PlayerTeleportEvent.TeleportCause.PLUGIN
        );

        /*
         * Si otro sistema cancela el
         * teletransporte, no gastar oro.
         */
        if (!teleported) {
            return;
        }

        /*
         * ========================================
         * DESCONTAR BLOQUES DE ORO
         * ========================================
         */
        removeGoldBlocks(
                player,
                GOLD_COST
        );

        /*
         * ========================================
         * APLICAR COOLDOWN
         * ========================================
         */
        cooldowns.put(
                player.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_MS
        );

        player.setCooldown(
                panicScythe.getCooldownGroup(),
                PanicScythe.COOLDOWN_TICKS
        );

        /*
         * ========================================
         * PARTICULAS Y SONIDO
         * ========================================
         */
        origin.getWorld().spawnParticle(
                Particle.PORTAL,
                origin.clone().add(0, 1, 0),
                25,
                0.3, 0.5, 0.3,
                0.05
        );

        destination.getWorld().spawnParticle(
                Particle.PORTAL,
                destination.clone().add(0, 1, 0),
                25,
                0.3, 0.5, 0.3,
                0.05
        );

        player.getWorld().playSound(
                destination,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                0.9f,
                1.2f
        );
    }

    /*
     * ========================================
     * CONTAR BLOQUES DE ORO VANILLA
     * ========================================
     */
    private boolean isGoldBlock(
            ItemStack item
    ) {

        return item != null
                && item.isSimilar(
                new ItemStack(Material.GOLD_BLOCK)
        );
    }

    private int countGoldBlocks(
            Player player
    ) {

        int total = 0;

        for (ItemStack item :
                player.getInventory().getStorageContents()) {

            if (isGoldBlock(item)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    /*
     * ========================================
     * CONSUMIR BLOQUES DE ORO
     * ========================================
     */
    private void removeGoldBlocks(
            Player player,
            int amount
    ) {

        ItemStack[] storage =
                player.getInventory().getStorageContents();

        int remaining = amount;

        for (int i = 0;
             i < storage.length && remaining > 0;
             i++) {

            ItemStack item = storage[i];

            if (!isGoldBlock(item)) {
                continue;
            }

            int removed = Math.min(
                    remaining,
                    item.getAmount()
            );

            int left =
                    item.getAmount() - removed;

            if (left <= 0) {

                storage[i] = null;

            } else {

                item.setAmount(left);
            }

            remaining -= removed;
        }

        player.getInventory()
                .setStorageContents(storage);
    }

    /*
     * ========================================
     * LIMPIAR COOLDOWN AL DESCONECTAR
     * ========================================
     */
    @EventHandler
    public void onQuit(
            PlayerQuitEvent event
    ) {

        cooldowns.remove(
                event.getPlayer().getUniqueId()
        );
    }
}