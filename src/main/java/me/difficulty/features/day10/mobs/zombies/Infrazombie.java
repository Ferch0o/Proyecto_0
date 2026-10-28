package me.difficulty.features.day10.mobs.zombies;

import me.difficulty.core.mobs.CustomMob;
import me.difficulty.core.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Infrazombie extends CustomMob {

    public Infrazombie(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                MobIds.INFRAZOMBIE
        );
    }

    @Override
    protected void configure(
            LivingEntity entity
    ) {

        /*
         * Seguridad:
         * un Infrazombie debe ser realmente
         * un Zombie.
         */
        if (!(entity instanceof Zombie zombie)) {
            return;
        }

        /*
         * Nombre visible.
         */
        zombie.customName(
                Component.text(
                        "Infrazombie",
                        NamedTextColor.RED
                )
        );

        zombie.setCustomNameVisible(
                true
        );


        /*
         * Resistencia al fuego.
         *
         * Duración prácticamente permanente.
         */
        zombie.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        PotionEffect.INFINITE_DURATION,
                        0,
                        false,
                        false
                )
        );

        zombie.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.SPEED,
                        PotionEffect.INFINITE_DURATION,
                        4,
                        false,
                        false
                )
        );

        /*
         * Espada de hierro.
         */
        ItemStack sword =
                new ItemStack(
                        Material.WOODEN_SWORD
                );

        ItemMeta meta =
                sword.getItemMeta();

        /*
         * Aspecto Ígneo V.
         *
         * Vanilla normalmente no permite
         * este nivel mediante mesa de encantamiento,
         * pero el plugin sí puede aplicarlo.
         */
        meta.addEnchant(
                Enchantment.FIRE_ASPECT,
                5,
                true
        );

        sword.setItemMeta(meta);


        /*
         * Se equipa en la mano principal.
         */
        zombie.getEquipment()
                .setItemInMainHand(
                        sword
                );


        /*
         * Evitamos que la espada caiga
         * automáticamente.
         *
         * Si después queremos un drop especial,
         * lo controlaremos nosotros.
         */
        zombie.getEquipment()
                .setItemInMainHandDropChance(
                        0.0f
                );
    }
}