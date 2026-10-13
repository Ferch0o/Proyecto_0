package me.difficulty;

import me.difficulty.commands.DayCommand;
import me.difficulty.commands.GiveCorruptedStarCommand;
import me.difficulty.commands.SetDayCommand;
import me.difficulty.day.DayManager;
import me.difficulty.items.CustomItemRegistry;
import me.difficulty.items.day7.CorruptedStar;
import me.difficulty.listeners.CorruptedStarListener;
import me.difficulty.listeners.CorruptedStarRecipeBookListener;
import me.difficulty.listeners.CorruptedStarRecipeListener;
import me.difficulty.listeners.RecipeUnlockListener;
import me.difficulty.recipes.CorruptedStarRecipe;
import me.difficulty.recipes.RecipeUnlockManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DifficultyPlugin extends JavaPlugin {

    private DayManager dayManager;
    private CustomItemRegistry itemRegistry;
    private CorruptedStar corruptedStar;
    private RecipeUnlockManager recipeUnlockManager;

    @Override
    public void onEnable() {

        /*
         * ========================================
         * 1. CONFIGURACIÓN
         * ========================================
         */

        saveDefaultConfig();


        /*
         * ========================================
         * 2. SISTEMAS PRINCIPALES
         * ========================================
         */

        dayManager =
                new DayManager(this);

        itemRegistry =
                new CustomItemRegistry(this);


        /*
         * ========================================
         * 3. ITEMS PERSONALIZADOS
         * ========================================
         */

        corruptedStar =
                new CorruptedStar(this);

        itemRegistry.register(
                corruptedStar
        );


        /*
         * ========================================
         * 4. RECETAS
         * ========================================
         */

        CorruptedStarRecipe corruptedStarRecipe =
                new CorruptedStarRecipe(
                        this,
                        corruptedStar
                );

        /*
         * IMPORTANTE:
         *
         * Primero registramos la receta
         * en Minecraft.
         */
        corruptedStarRecipe.register();


        /*
         * ========================================
         * 5. DESBLOQUEO DE RECETAS
         * ========================================
         */

        recipeUnlockManager =
                new RecipeUnlockManager(
                        this,
                        dayManager,
                        corruptedStarRecipe.getRecipeKey()
                );


        /*
         * ========================================
         * 6. LISTENERS
         * ========================================
         */

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarListener(
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarRecipeListener(
                                corruptedStarRecipe.getRecipeKey(),
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarRecipeBookListener(
                                corruptedStarRecipe.getRecipeKey(),
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new RecipeUnlockListener(
                                recipeUnlockManager
                        ),
                        this
                );


        /*
         * ========================================
         * 7. COMANDOS
         * ========================================
         */

        Objects.requireNonNull(
                getCommand("day")
        ).setExecutor(
                new DayCommand(
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("setday")
        ).setExecutor(
                new SetDayCommand(
                        dayManager,
                        recipeUnlockManager
                )
        );

        Objects.requireNonNull(
                getCommand("givecorruptedstar")
        ).setExecutor(
                new GiveCorruptedStarCommand(
                        corruptedStar
                )
        );


        /*
         * ========================================
         * 8. ACTUALIZAR JUGADORES CONECTADOS
         * ========================================
         *
         * Esto ocurre DESPUÉS de registrar
         * la receta.
         */

        recipeUnlockManager.updateAllPlayers();


        /*
         * ========================================
         * 9. MENSAJE DE INICIO
         * ========================================
         */

        getLogger().info(
                "DifficultyPlugin iniciado correctamente. Día actual: "
                        + dayManager.getCurrentDay()
        );
    }

    @Override
    public void onDisable() {

        getLogger().info(
                "DifficultyPlugin apagado"
        );
    }
}