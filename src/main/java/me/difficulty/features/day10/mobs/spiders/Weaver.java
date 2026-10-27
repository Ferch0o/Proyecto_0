package me.difficulty.features.day10.mobs.spiders;

import me.difficulty.features.day10.mobs.skeletons.ElementalSkeleton;
import me.difficulty.features.day10.mobs.skeletons.Gladiator;
import me.difficulty.mobs.CustomMob;
import me.difficulty.mobs.MobIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class Weaver extends CustomMob {

    private final NamespacedKey riderKey;

    private final ElementalSkeleton elementalSkeleton;
    private final Gladiator gladiator;


    public Weaver(
            JavaPlugin plugin,
            ElementalSkeleton elementalSkeleton,
            Gladiator gladiator
    ) {

        super(
                plugin,
                MobIds.WEAVER
        );

        this.elementalSkeleton =
                elementalSkeleton;

        this.gladiator =
                gladiator;

        this.riderKey =
                new NamespacedKey(
                        plugin,
                        "weaver_rider"
                );
    }


    @Override
    protected void configure(
            LivingEntity entity
    ) {

        if (!(entity instanceof Spider spider)) {
            return;
        }


        /*
         * ========================================
         * NOMBRE
         * ========================================
         */

        spider.customName(
                Component.text(
                        "Tejedora",
                        NamedTextColor.WHITE
                )
        );

        spider.setCustomNameVisible(
                true
        );


        /*
         * ========================================
         * 80% DE PROBABILIDAD DE JINETE
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 80) {

            createSkeletonRider(
                    spider
            );
        }
    }


    /*
     * ========================================
     * CREAR ESQUELETO JINETE
     * ========================================
     */

    private void createSkeletonRider(
            Spider spider
    ) {

        Skeleton skeleton =
                spider.getWorld().spawn(
                        spider.getLocation(),
                        Skeleton.class
                );


        /*
         * ========================================
         * TIPO DE ESQUELETO
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll < 85) {
            elementalSkeleton.apply(skeleton);
        } else {
            gladiator.apply(skeleton);
        }


        /*
         * ========================================
         * MARCAR COMO JINETE DE TEJEDORA
         * ========================================
         *
         * Además de ser Elemental o Gladiador,
         * tendrá esta segunda marca.
         */

        skeleton.getPersistentDataContainer()
                .set(
                        riderKey,
                        PersistentDataType.BYTE,
                        (byte) 1
                );


        /*
         * ========================================
         * MONTAR ESQUELETO
         * ========================================
         */

        spider.addPassenger(
                skeleton
        );
    }


    /*
     * ========================================
     * IDENTIFICAR JINETE DE TEJEDORA
     * ========================================
     */

    public boolean isWeaverRider(
            Skeleton skeleton
    ) {

        Byte value =
                skeleton.getPersistentDataContainer()
                        .get(
                                riderKey,
                                PersistentDataType.BYTE
                        );

        return value != null
                && value == (byte) 1;
    }
}