
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.mobs.spiders.WeaverRiderCombatListener;
import me.difficulty.features.day10.mobs.spiders.GoliathListener;
import me.difficulty.features.day10.mobs.spiders.AbyssSpiderDeathListener;
import me.difficulty.features.day10.mobs.spiders.SpiderSpawnListener;

import org.bukkit.plugin.PluginManager;
import java.util.Objects;

public final class Day10SpiderListenerRegistrar {

    private final DifficultyPlugin plugin;

    public Day10SpiderListenerRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void register(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Day10Bootstrap.Mobs mobs
    ) {

        PluginManager pm = plugin.getServer().getPluginManager();

        // TEJEDORA - COMBATE DEL JINETE
        pm.registerEvents(
                new WeaverRiderCombatListener(
                        mobs.weaver(),
                        dayManager
                ),
                plugin
        );

        // GOLIAT - HABILIDADES
        pm.registerEvents(
                new GoliathListener(
                        mobRegistry,
                        dayManager
                ),
                plugin
        );

        // ARANA ABISAL - MUERTE
        pm.registerEvents(
                new AbyssSpiderDeathListener(
                        mobRegistry,
                        dayManager,
                        mobs.goliath()
                ),
                plugin
        );

        // ARANAS - GENERACION
        pm.registerEvents(
                new SpiderSpawnListener(
                        dayManager,
                        mobs.speedSpider(),
                        mobs.abyssSpider(),
                        mobs.weaver(),
                        mobs.goliath()
                ),
                plugin
        );
    }
}