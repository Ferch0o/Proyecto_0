
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.features.qualityoflife.aldeanos.SharedCureCommand;
import me.difficulty.features.qualityoflife.aldeanos.SharedVillagerCureListener;
import me.difficulty.features.qualityoflife.friendlyfire.FriendlyFireProtectionListener;

import java.util.Objects;

public final class QualityOfLifeBootstrap {

    private final DifficultyPlugin plugin;

    private SharedVillagerCureListener sharedVillagerCureListener;

    public QualityOfLifeBootstrap(DifficultyPlugin plugin) {
        this.plugin = plugin;
    }

    // Protección contra fuego amigo
    public void registerFriendlyFire() {
        plugin.getServer().getPluginManager().registerEvents(
                new FriendlyFireProtectionListener(plugin),
                plugin
        );
    }

    // Curación compartida de aldeanos
    public void registerVillagerListener() {
        sharedVillagerCureListener =
                new SharedVillagerCureListener(plugin);

        plugin.getServer().getPluginManager().registerEvents(
                sharedVillagerCureListener,
                plugin
        );
    }

    // Comando /sharedcure
    public void registerVillagerCommand() {
        Objects.requireNonNull(
                plugin.getCommand("sharedcure")
        ).setExecutor(
                new SharedCureCommand(sharedVillagerCureListener)
        );
    }
}
