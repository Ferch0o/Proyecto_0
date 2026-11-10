
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    private static final int REQUIRED_DAY = 20;
    private static final int GOLD_COST = 2;

    private static final double TELEPORT_DISTANCE = 5.0;
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
     * ACTIVAR GUADAÑA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(
            PlayerInteractEvent event
    ) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!panicScythe.isItem(
                player.getInventory().getItemInMainHand()
        )) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * Evitar que la azada de netherite
         * active acciones vanilla.
         */
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

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
         * VERIFICAR BLOQUES DE ORO
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

        Location origin = player.getLocation();
        Vector direction = getHorizontalDirection(origin);

        Location destination = origin.clone().add(
                direction.clone().multiply(
                        TELEPORT_DISTANCE
                )
        );

        /*
         * ========================================
         * DESTINO SEGURO
         * ========================================
         */
        if (!isPathClear(origin, direction)
                || !isSafeDestination(destination)) {

            player.sendActionBar(
                    Component.text(
                            "No hay un destino seguro.",
                            NamedTextColor.RED
                    )
            );

            return;
        }

        /*
         * Teletransportar primero.
         * Si otro plugin cancela el evento,
         * no consumir materiales ni cooldown.
         */
        boolean teleported = player.teleport(
                destination,
                PlayerTeleportEvent.TeleportCause.PLUGIN
        );

        if (!teleported) {
            return;
        }

        /*
         * ========================================
         * DESCONTAR COSTO
         * ========================================
         */
        removeGoldBlocks(player, GOLD_COST);

        /*
         * ========================================
         * APLICAR COOLDOWN
         * ========================================
         */
        cooldowns.put(
                player.getUniqueId(),
                now + COOLDOWN_MS
        );

        player.setCooldown(
                panicScythe.getCooldownGroup(),
                PanicScythe.COOLDOWN_TICKS
        );

        /*
         * ========================================
         * EFECTO VISUAL Y SONIDO
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
     * DIRECCION HORIZONTAL
     * ========================================
     *
     * Usamos el yaw para que mirar
     * hacia arriba o abajo no altere
     * la altura del teletransporte.
     */
    private Vector getHorizontalDirection(
            Location origin
    ) {

        double yaw =
                Math.toRadians(origin.getYaw());

        return new Vector(
                -Math.sin(yaw),
                0.0,
                Math.cos(yaw)
        ).normalize();
    }

    /*
     * ========================================
     * EVITAR ATRAVESAR PAREDES
     * ========================================
     */
    private boolean isPathClear(
            Location origin,
            Vector direction
    ) {

        World world = origin.getWorld();

        /*
         * Revisar el recorrido a tres
         * posiciones laterales del jugador
         * y a dos alturas.
         */
        Vector sideways = new Vector(
                -direction.getZ(),
                0.0,
                direction.getX()
        );

        double[] offsets = {
                -0.25, 0.0, 0.25
        };

        for (double offset : offsets) {

            Location line = origin.clone().add(
                    sideways.clone().multiply(offset)
            );

            Location feet =
                    line.clone().add(0, 0.2, 0);

            Location head =
                    line.clone().add(0, 1.6, 0);

            if (world.rayTraceBlocks(
                    feet,
                    direction,
                    TELEPORT_DISTANCE,
                    FluidCollisionMode.NEVER,
                    true
            ) != null) {
                return false;
            }

            if (world.rayTraceBlocks(
                    head,
                    direction,
                    TELEPORT_DISTANCE,
                    FluidCollisionMode.NEVER,
                    true
            ) != null) {
                return false;
            }
        }

        return true;
    }

    /*
     * ========================================
     * VALIDAR DESTINO
     * ========================================
     */
    private boolean isSafeDestination(
            Location destination
    ) {

        World world = destination.getWorld();

        if (!world.getWorldBorder()
                .isInside(destination)) {
            return false;
        }

        Block feet = destination.getBlock();

        Block head = destination.clone()
                .add(0, 1, 0)
                .getBlock();

        Block floor = destination.clone()
                .add(0, -1, 0)
                .getBlock();

        /*
         * Espacio libre para el cuerpo.
         */
        if (!feet.isPassable()
                || !head.isPassable()) {
            return false;
        }

        if (feet.isLiquid()
                || head.isLiquid()) {
            return false;
        }

        /*
         * Evitar fuego y nieve en polvo.
         */
        if (isHazard(feet.getType())
                || isHazard(head.getType())) {
            return false;
        }

        /*
         * Necesitamos suelo sólido.
         */
        if (!floor.getType().isSolid()) {
            return false;
        }

        return !isHazard(floor.getType());
    }

    private boolean isHazard(Material type) {

        return type == Material.LAVA
                || type == Material.FIRE
                || type == Material.SOUL_FIRE
                || type == Material.POWDER_SNOW
                || type == Material.CACTUS
                || type == Material.MAGMA_BLOCK
                || type == Material.CAMPFIRE
                || type == Material.SOUL_CAMPFIRE
                || type == Material.SWEET_BERRY_BUSH;
    }

    /*
     * ========================================
     * BLOQUES DE ORO VANILLA
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

        for (ItemStack item : player
                .getInventory()
                .getStorageContents()) {

            if (isGoldBlock(item)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private void removeGoldBlocks(
            Player player,
            int amount
    ) {

        ItemStack[] storage = player
                .getInventory()
                .getStorageContents();

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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        cooldowns.remove(
                event.getPlayer().getUniqueId()
        );
    }
}
