package me.difficulty.features.day17.curse;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class CursePersistence {
    private final NamespacedKey stateKey;

    public CursePersistence(JavaPlugin plugin) {
        stateKey = new NamespacedKey(plugin, "curse_state");
    }

    public CurseState getState(Player player) {
        String stored = player.getPersistentDataContainer()
                .get(stateKey, PersistentDataType.STRING);
        if (stored == null) return CurseState.CURSED;
        try {
            return CurseState.valueOf(stored);
        } catch (IllegalArgumentException ignored) {
            return CurseState.CURSED;
        }
    }

    public void saveState(Player player, CurseState state) {
        player.getPersistentDataContainer().set(
                stateKey, PersistentDataType.STRING, state.name());
    }

    public void ensureInitialized(Player player) {
        if (!player.getPersistentDataContainer().has(stateKey, PersistentDataType.STRING)) {
            saveState(player, CurseState.CURSED);
        }
    }
}
