
package me.difficulty.features.day20.mobs;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;

public final class FortressLocationUtil {

    private FortressLocationUtil() {
    }

    /*
     * ========================================
     * COMPROBAR FORTALEZA DEL NETHER
     * ========================================
     */
    public static boolean isInFortress(
            Location location
    ) {

        World world = location.getWorld();

        if (world == null
                || world.getEnvironment()
                != World.Environment.NETHER) {
            return false;
        }

        /*
         * Consultar solamente las fortalezas
         * que intersectan el chunk actual.
         */
        for (GeneratedStructure structure :
                location.getChunk()
                        .getStructures(Structure.FORTRESS)) {

            /*
             * Comprobar que el punto de
             * aparición esté dentro de los
             * límites de esa fortaleza.
             */
            if (structure.getBoundingBox().contains(
                    location.getX(),
                    location.getY(),
                    location.getZ()
            )) {
                return true;
            }
        }

        return false;
    }
}
