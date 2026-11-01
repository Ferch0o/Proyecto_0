package me.difficulty.features.day17.curse;

import me.difficulty.core.day.DayManager;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CurseManager {
    private final DayManager dayManager;
    private final CursePersistence persistence;
    private final NamespacedKey healthModifierKey;

    public CurseManager(JavaPlugin plugin, DayManager dayManager) {
        this.dayManager = dayManager;
        this.persistence = new CursePersistence(plugin);
        this.healthModifierKey = new NamespacedKey(plugin, "curse_health");
    }

    public int currentDay() {
        return dayManager.getCurrentDay();
    }

    public CurseState getState(Player player) {
        return persistence.getState(player);
    }

    public boolean isActive(Player player) {
        return dayManager.isAtLeast(17) && getState(player).isCursed();
    }

    public boolean cure(Player player) {
        if (!isActive(player) || !getState(player).canBeCured()) return false;
        persistence.saveState(player, CurseState.CURED);
        refresh(player);
        // Curación completa: 12 corazones de vida máxima y salud actual restaurada.
        AttributeInstance health = player.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) player.setHealth(health.getValue());
        return true;
    }


    /*
     * ========================================
     * REAPLICAR MALDICIÓN PARA PRUEBAS
     * ========================================
     *
     * Permite que un administrador vuelva
     * a maldecir a un jugador previamente
     * curado, sin reiniciar el mundo.
     */
    public void reapplyCurseForTesting(
            Player player
    ) {

        /*
         * Guardar nuevamente el estado
         * como maldito.
         */
        persistence.saveState(
                player,
                CurseState.CURSED
        );

        /*
         * Volver a aplicar:
         *
         * - Efectos negativos.
         * - Reducción de vida máxima.
         */
        refresh(player);
    }

    public void refresh(Player player) {
        if (dayManager.isAtLeast(17)) persistence.ensureInitialized(player);

        double healthChange = 0.0;
        if (dayManager.isAtLeast(17)) {
            if (getState(player) == CurseState.CURED) healthChange = 4.0;
            else healthChange = dayManager.isAtLeast(25) ? -8.0 : -4.0;
        }
        applyHealthModifier(player, healthChange);

        if (isActive(player)) {
            boolean severe = dayManager.isAtLeast(25);
            applyEffect(player, PotionEffectType.SLOWNESS, severe ? 2 : 1);
            applyEffect(player, PotionEffectType.WEAKNESS, severe ? 1 : 0);
            if (severe) {
                player.removePotionEffect(PotionEffectType.HUNGER);
                applyEffect(player, PotionEffectType.POISON, 0);
            } else {
                player.removePotionEffect(PotionEffectType.POISON);
                applyEffect(player, PotionEffectType.HUNGER, 1);
            }
        }
    }

    public void clearCurseEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.POISON);
    }

    private void applyEffect(Player player, PotionEffectType type, int amplifier) {
        PotionEffect active = player.getPotionEffect(type);
        if (active != null && active.getAmplifier() == amplifier && active.getDuration() > 45) {
            return;
        }
        player.addPotionEffect(new PotionEffect(type, 100, amplifier, true, false, true));
    }

    private void applyHealthModifier(Player player, double amount) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        AttributeModifier existing = attribute.getModifiers().stream()
                .filter(m -> m.getKey().equals(healthModifierKey)).findFirst().orElse(null);
        if (existing != null && existing.getAmount() == amount) return;
        if (existing != null) attribute.removeModifier(existing);
        if (amount != 0.0) {
            attribute.addModifier(new AttributeModifier(
                    healthModifierKey, amount, AttributeModifier.Operation.ADD_NUMBER));
        }
        if (player.getHealth() > attribute.getValue()) {
            player.setHealth(Math.max(0.1, attribute.getValue()));
        }
    }
}
