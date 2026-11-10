package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day17.curse.*;
import me.difficulty.features.day17.items.*;
import me.difficulty.features.day17.items.elementalpotato.*;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class Day17Bootstrap {
    private final DifficultyPlugin plugin;
    private final DayManager dayManager;

    public Day17Bootstrap(DifficultyPlugin plugin, DayManager dayManager) {
        this.plugin = Objects.requireNonNull(plugin);
        this.dayManager = Objects.requireNonNull(dayManager);
    }

    public void register(CustomItemRegistry registry, CustomMobRegistry mobRegistry) {
        // 1) Maldición y ranuras (reutilizable al llegar al día 25).
        CurseManager manager = new CurseManager(plugin, dayManager);
        CurseInventoryService inventory = new CurseInventoryService(plugin, dayManager, manager);
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(
                new CurseInventoryListener(
                        plugin,
                        inventory
                ),
                plugin
        );
        pm.registerEvents(new CursePlayerListener(plugin, manager, inventory), plugin);

        // 2) Seis materiales nuevos, drops y protección básica de uso.
        Day17Materials materials = Day17Materials.register(plugin, registry);
        pm.registerEvents(new Day17MaterialDropListener(dayManager, mobRegistry, materials), plugin);

        // 3) Patata Elemental: ítem, receta y curación por consumo normal.
        ElementalPotato potato = new ElementalPotato(plugin);
        registry.register(potato);
        ElementalPotatoRecipe recipe = new ElementalPotatoRecipe(plugin, potato, materials);
        recipe.register();

        pm.registerEvents(
                new Day17MaterialProtectionListener(
                        materials,
                        recipe.key(),
                        new NamespacedKey(
                                plugin,
                                "elemental_ingot_recipe"
                        )
                ),
                plugin
        );

        pm.registerEvents(
                new ElementalPotatoRecipeListener(
                        plugin,
                        dayManager,
                        materials,
                        potato,
                        recipe.key()
                ),
                plugin
        );

        pm.registerEvents(
                new ElementalPotatoRecipeBookListener(
                        plugin,
                        dayManager,
                        materials,
                        potato,
                        recipe.key()
                ),
                plugin
        );

        /*
         * ========================================
         * PATATA ELEMENTAL
         * PROTECCIÓN DE USOS VANILLA
         * ========================================
         */

        pm.registerEvents(
                new ElementalPotatoProtectionListener(
                        potato
                ),
                plugin
        );

        pm.registerEvents(new ElementalPotatoConsumeListener(plugin, manager, inventory, potato), plugin);
        new CurseMaintenanceTask(manager, inventory, recipe.key()).runTaskTimer(plugin, 20L, 20L);

        // 4) Comandos exclusivos de test.
        Objects.requireNonNull(plugin.getCommand("giveelementalpotato"),
                "Falta giveelementalpotato en plugin.yml")
                .setExecutor(new GiveElementalPotatoCommand(potato));
        Objects.requireNonNull(plugin.getCommand("giveday17materials"),
                "Falta giveday17materials en plugin.yml")
                .setExecutor(new GiveDay17MaterialsCommand(materials));

        /*
         * ========================================
         * COMANDO DE PRUEBAS
         * RESTABLECER MALDICIÓN
         * ========================================
         */

        Objects.requireNonNull(
                plugin.getCommand("resetcurse"),
                "Falta resetcurse en plugin.yml"
        ).setExecutor(
                new ResetCurseCommand(
                        manager,
                        inventory
                )
        );


        // 5) Restaurar online si la clase se registra durante recargas.
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            manager.refresh(player);
            inventory.synchronize(player);
        });
    }
}
