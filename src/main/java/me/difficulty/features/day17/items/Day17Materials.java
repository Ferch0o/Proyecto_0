package me.difficulty.features.day17.items;

import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.items.ItemIds;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public record Day17Materials(
        Day17Material driedMeat,
        Day17Material guardianBone,
        Day17Material magicGunpowder,
        Day17Material goliathEye,
        Day17Material titaniumBlock,
        Day17Material wardenHeart
) {
    public static Day17Materials register(JavaPlugin plugin, CustomItemRegistry registry) {
        Day17Materials materials = new Day17Materials(
                new Day17Material(
                        plugin,
                        ItemIds.DRIED_MEAT,
                        Material.ROTTEN_FLESH,
                        ItemIds.DRIED_MEAT_MODEL,
                        "Carne Seca",
                        "Obtenida del Carroñero."
                ),
                new Day17Material(plugin, ItemIds.GUARDIAN_BONE, Material.BONE,
                        ItemIds.GUARDIAN_BONE_MODEL, "Hueso de Guardián", "Obtenido del Gladiador."),
                new Day17Material(plugin, ItemIds.MAGIC_GUNPOWDER, Material.GUNPOWDER,
                        ItemIds.MAGIC_GUNPOWDER_MODEL, "Pólvora Mágica", "Obtenida del Creeper Futurista."),
                new Day17Material(plugin, ItemIds.GOLIATH_EYE, Material.FERMENTED_SPIDER_EYE,
                        ItemIds.GOLIATH_EYE_MODEL, "Ojo de Goliat", "Obtenido de la Araña Goliat."),
                new Day17Material(plugin, ItemIds.TITANIUM_BLOCK, Material.IRON_BLOCK,
                        ItemIds.TITANIUM_BLOCK_MODEL, "Bloque de Titanio", "Obtenido de los Golems de Hierro."),
                new Day17Material(plugin, ItemIds.WARDEN_HEART, Material.HEART_OF_THE_SEA,
                        ItemIds.WARDEN_HEART_MODEL, "Corazón de Warden", "Obtenido al derrotar al Warden.")
        );
        for (Day17Material item : materials.all()) registry.register(item);
        return materials;
    }

    public List<Day17Material> all() {
        return List.of(driedMeat, guardianBone, magicGunpowder, goliathEye, titaniumBlock, wardenHeart);
    }
}
