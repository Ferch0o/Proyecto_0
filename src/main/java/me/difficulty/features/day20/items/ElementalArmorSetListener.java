
package me.difficulty.features.day20.items;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;

import me.difficulty.core.day.DayManager;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ElementalArmorSetListener
        implements Listener {

    /*
     * ========================================
     * BONUS DEL CONJUNTO
     * ========================================
     *
     * Solo desde el día 20.
     *
     * Vida máxima: +6 puntos.
     *
     * Overworld: Resistencia II.
     * Nether: Fuerza II.
     * End: Velocidad II.
     */

    private static final int REQUIRED_DAY = 20;
    private static final double HEALTH_BONUS = 6.0;

    /*
     * Nivel II = amplificador 1.
     * Duración corta, renovada periódicamente.
     */
    private static final int EFFECT_DURATION = 60;
    private static final int EFFECT_AMPLIFIER = 1;

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    private final Map<ElementalArmorPiece.Kind,
            ElementalArmorPiece> pieces =
            new EnumMap<>(ElementalArmorPiece.Kind.class);

    private final NamespacedKey healthModifierKey;

    /*
     * Seguimiento de efectos aplicados
     * por nuestra propia armadura.
     */
    private final Map<UUID, PotionEffectType>
            appliedEffects = new HashMap<>();

    public ElementalArmorSetListener(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalArmorPiece helmet,
            ElementalArmorPiece chestplate,
            ElementalArmorPiece leggings,
            ElementalArmorPiece boots
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;

        pieces.put(
                ElementalArmorPiece.Kind.HELMET,
                helmet
        );

        pieces.put(
                ElementalArmorPiece.Kind.CHESTPLATE,
                chestplate
        );

        pieces.put(
                ElementalArmorPiece.Kind.LEGGINGS,
                leggings
        );

        pieces.put(
                ElementalArmorPiece.Kind.BOOTS,
                boots
        );

        /*
         * Modificador propio.
         *
         * No usar "curse_health".
         * Esa clave pertenece al día 17.
         */
        healthModifierKey = new NamespacedKey(
                plugin,
                "elemental_armor_health"
        );
    }

    /*
     * ========================================
     * INICIAR ACTUALIZACIÓN
     * ========================================
     *
     * Un segundo entre comprobaciones.
     * También escuchamos eventos de
     * equipo y cambios de dimensión.
     */
    public void start() {

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {

                    for (Player player :
                            Bukkit.getOnlinePlayers()) {

                        synchronize(player);
                    }
                },
                2L,
                20L
        );
    }

    /*
     * ========================================
     * COMPROBAR CONJUNTO COMPLETO
     * ========================================
     */
    private boolean hasFullSet(
            Player player
    ) {

        return pieces.get(
                ElementalArmorPiece.Kind.HELMET
        ).isItem(
                player.getInventory().getHelmet()
        )

                && pieces.get(
                ElementalArmorPiece.Kind.CHESTPLATE
        ).isItem(
                player.getInventory().getChestplate()
        )

                && pieces.get(
                ElementalArmorPiece.Kind.LEGGINGS
        ).isItem(
                player.getInventory().getLeggings()
        )

                && pieces.get(
                ElementalArmorPiece.Kind.BOOTS
        ).isItem(
                player.getInventory().getBoots()
        );
    }

    /*
     * ========================================
     * SINCRONIZAR BONIFICACIONES
     * ========================================
     */
    private void synchronize(
            Player player
    ) {

        if (!player.isOnline()) {
            return;
        }

        boolean active =
                dayManager.isAtLeast(REQUIRED_DAY)
                        && hasFullSet(player);

        /*
         * La salud usa su propio
         * modificador independiente.
         */
        updateHealth(
                player,
                active ? HEALTH_BONUS : 0.0
        );

        PotionEffectType desired = null;

        if (active) {

            World.Environment world =
                    player.getWorld().getEnvironment();

            switch (world) {

                case NORMAL ->
                        desired = PotionEffectType.RESISTANCE;

                case NETHER ->
                        desired = PotionEffectType.STRENGTH;

                case THE_END ->
                        desired = PotionEffectType.SPEED;

                default -> {
                }
            }
        }

        updateEffect(player, desired);
    }

    /*
     * ========================================
     * BONUS DE VIDA
     * ========================================
     *
     * El modificador de la maldición
     * permanece independiente.
     */
    private void updateHealth(
            Player player,
            double amount
    ) {

        AttributeInstance health =
                player.getAttribute(
                        Attribute.MAX_HEALTH
                );

        if (health == null) {
            return;
        }

        AttributeModifier existing =
                health.getModifiers()
                        .stream()
                        .filter(modifier ->
                                modifier.getKey().equals(
                                        healthModifierKey
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (existing != null
                && existing.getAmount() == amount) {
            return;
        }

        if (existing != null) {
            health.removeModifier(existing);
        }

        if (amount > 0.0) {

            health.addModifier(
                    new AttributeModifier(
                            healthModifierKey,
                            amount,
                            AttributeModifier.Operation.ADD_NUMBER
                    )
            );
        }

        /*
         * Evitar que al retirar el conjunto
         * la vida actual supere su nuevo máximo.
         */
        double maximum = health.getValue();

        if (player.getHealth() > maximum) {

            player.setHealth(
                    Math.max(0.1, maximum)
            );
        }
    }

    /*
     * ========================================
     * IDENTIFICAR EFECTO PROPIO
     * ========================================
     */
    private boolean isOurEffect(
            PotionEffect effect
    ) {

        return effect != null
                && !effect.isInfinite()
                && effect.getAmplifier()
                == EFFECT_AMPLIFIER
                && effect.isAmbient()
                && !effect.hasParticles()
                && effect.hasIcon()
                && effect.getDuration() > 0
                && effect.getDuration()
                <= EFFECT_DURATION;
    }

    /*
     * ========================================
     * APLICAR EFECTO SEGÚN DIMENSIÓN
     * ========================================
     */
    private void updateEffect(
            Player player,
            PotionEffectType desired
    ) {

        UUID uuid = player.getUniqueId();

        PotionEffectType previous =
                appliedEffects.get(uuid);

        /*
         * Quitar nuestro efecto anterior
         * cuando cambia la dimensión
         * o se pierde el conjunto.
         */
        if (previous != null
                && !previous.equals(desired)) {

            PotionEffect current =
                    player.getPotionEffect(previous);

            if (isOurEffect(current)) {
                player.removePotionEffect(previous);
            }

            appliedEffects.remove(uuid);
        }

        if (desired == null) {
            return;
        }

        PotionEffect current =
                player.getPotionEffect(desired);

        /*
         * No sobrescribir efectos de otras
         * fuentes: pociones, beacons o
         * futuras habilidades.
         */
        if (current != null
                && !desired.equals(
                appliedEffects.get(uuid)
        )) {
            return;
        }

        if (current != null
                && !isOurEffect(current)) {

            appliedEffects.remove(uuid);
            return;
        }

        /*
         * Si todavía dura suficiente,
         * no renovar innecesariamente.
         */
        if (current != null
                && current.getDuration() > 30) {
            return;
        }

        player.addPotionEffect(
                new PotionEffect(
                        desired,
                        EFFECT_DURATION,
                        EFFECT_AMPLIFIER,
                        true,
                        false,
                        true
                )
        );

        appliedEffects.put(uuid, desired);
    }

    /*
     * ========================================
     * CAMBIOS DE EQUIPO
     * ========================================
     */
    @EventHandler
    public void onEquipmentChange(
            EntityEquipmentChangedEvent event
    ) {

        if (!(event.getEntity()
                instanceof Player player)) {
            return;
        }

        synchronizeSoon(player);
    }

    /*
     * ========================================
     * CAMBIO DE DIMENSIÓN
     * ========================================
     */
    @EventHandler
    public void onWorldChange(
            PlayerChangedWorldEvent event
    ) {

        synchronizeSoon(event.getPlayer());
    }

    @EventHandler
    public void onJoin(
            PlayerJoinEvent event
    ) {

        synchronizeSoon(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(
            PlayerRespawnEvent event
    ) {

        synchronizeSoon(event.getPlayer());
    }

    /*
     * ========================================
     * SINCRONIZAR AL SIGUIENTE TICK
     * ========================================
     */
    private void synchronizeSoon(
            Player player
    ) {

        Bukkit.getScheduler().runTask(
                plugin,
                () -> synchronize(player)
        );
    }

    @EventHandler
    public void onQuit(
            PlayerQuitEvent event
    ) {

        appliedEffects.remove(
                event.getPlayer().getUniqueId()
        );
    }
}