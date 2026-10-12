package me.difficulty;

import org.bukkit.plugin.java.JavaPlugin;

public final class DifficultyPlugin extends JavaPlugin{

    @Override
    public void onEnable(){
        getLogger().info("DifficultyPlugin iniciado correctamente");
    }

    @Override
    public void onDisable(){
        getLogger().info("DifficultyPlugin apagado");
    }
}