package me.difficulty.features.day10.items.minotaurhorn;

import me.difficulty.items.CustomItem;
import me.difficulty.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class MinotaurHorn extends CustomItem {


    public MinotaurHorn(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                ItemIds.MINOTAUR_HORN,
                Material.RESIN_CLUMP,
                ItemIds.MINOTAUR_HORN_MODEL
        );
    }


    @Override
    protected Component getDisplayName() {

        return Component.text(
                        "Cuerno de Minotauro",
                        NamedTextColor.GOLD
                )
                .decoration(
                        TextDecoration.ITALIC,
                        false
                );
    }


    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                                "Un fragmento endurecido obtenido de un Ravager.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Disponible desde el Día 10.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }


    public boolean isMinotaurHorn(
            ItemStack item
    ) {

        return isItem(item);
    }
}