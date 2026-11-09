
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public final class AncientDebrisPopulator
        extends BlockPopulator {

    /*
     * ========================================
     * DIA 20 - GENERACION DE ANCIENT DEBRIS
     * ========================================
     *
     * Desde el dia 20:
     * - Se retira Ancient Debris de chunks nuevos.
     * - Solo se aplica al Nether.
     * - No modifica chunks antiguos.
     * - No afecta los drops de los bloques.
     */

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;

    public AncientDebrisPopulator(
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
         * Solo desde el dia 20.
         */
        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * No modificar Overworld ni End.
         */
        if (worldInfo.getEnvironment()
                != World.Environment.NETHER) {
            return;
        }

        /*
         * Coordenadas absolutas del chunk.
         */
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        /*
         * Revisar los bloques del chunk
         * mientras se esta generando.
         *
         * No accedemos a World.getChunkAt()
         * ni a bloques de mundos cargados.
         */
        for (int x = startX; x < startX + 16; x++) {

            for (int z = startZ; z < startZ + 16; z++) {

                for (int y = minY; y < maxY; y++) {

                    if (region.getType(x, y, z)
                            != Material.ANCIENT_DEBRIS) {
                        continue;
                    }

                    /*
                     * Sustituir Ancient Debris
                     * por Netherrack.
                     *
                     * No genera drops.
                     */
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