
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public final class GoldOrePopulator
        extends BlockPopulator {

    /*
     * ========================================
     * DIA 20 - GENERACION DE ORO
     * ========================================
     *
     * Desde el dia 20:
     *
     * Overworld:
     * - GOLD_ORE deja de generarse.
     * - DEEPSLATE_GOLD_ORE deja de generarse.
     *
     * Nether:
     * - NETHER_GOLD_ORE deja de generarse.
     *
     * Los chunks existentes no se modifican.
     */

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;

    public GoldOrePopulator(
            DayManager dayManager
    ) {
        this.dayManager = dayManager;
    }

    @Override
    public void populate(
            WorldInfo worldInfo,
            Random random,
            int chunkX,
            int chunkZ,
            LimitedRegion region
    ) {

        /*
         * Solo funciona desde el dia 20.
         */
        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        World.Environment environment =
                worldInfo.getEnvironment();

        /*
         * Solamente Overworld y Nether.
         * El End no sera modificado.
         */
        if (environment != World.Environment.NORMAL
                && environment != World.Environment.NETHER) {
            return;
        }

        /*
         * Coordenadas del chunk.
         */
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        /*
         * Revisar todos los bloques
         * del chunk que se esta generando.
         */
        for (int x = startX; x < startX + 16; x++) {

            for (int z = startZ; z < startZ + 16; z++) {

                for (int y = minY; y < maxY; y++) {

                    Material block =
                            region.getType(x, y, z);

                    /*
                     * ========================================
                     * OVERWORLD
                     * ========================================
                     */
                    if (environment == World.Environment.NORMAL) {

                        if (block == Material.GOLD_ORE) {

                            region.setType(
                                    x,
                                    y,
                                    z,
                                    Material.STONE
                            );

                        } else if (
                                block == Material.DEEPSLATE_GOLD_ORE) {

                            region.setType(
                                    x,
                                    y,
                                    z,
                                    Material.DEEPSLATE
                            );
                        }

                    }

                    /*
                     * ========================================
                     * NETHER
                     * ========================================
                     */
                    else if (environment == World.Environment.NETHER) {

                        if (block == Material.NETHER_GOLD_ORE) {

                            region.setType(
                                    x,
                                    y,
                                    z,
                                    Material.NETHERRACK
                            );
                        }
                    }
                }
            }
        }
    }
}