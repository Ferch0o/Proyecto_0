
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GoldOreExplosionListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - EXPLOSIONES Y MINERALES DE ORO
     * ========================================
     *
     * Desde el dia 20:
     *
     * - Las explosiones destruyen las menas.
     * - Las menas no generan ningun drop.
     * - Los demas bloques siguen normales.
     *
     * Funciona con explosiones de entidades
     * y bloques.
     */

    private static final int REQUIRED_DAY = 20;

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    /*
     * Guardar el bloque y su tipo original
     * para comprobarlo al eliminarlo.
     */
    private record GoldOreBlock(
            Block block,
            Material originalType
    ) {
    }

    public GoldOreExplosionListener(
            JavaPlugin plugin,
            DayManager dayManager
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
    }

    /*
     * ========================================
     * EXPLOSIONES DE ENTIDADES
     * ========================================
     *
     * Ejemplos:
     * - TNT
     * - Creepers
     * - Ghasts
     * - Otras entidades explosivas
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onEntityExplode(
            EntityExplodeEvent event
    ) {

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        processExplosion(event.blockList());
    }

    /*
     * ========================================
     * EXPLOSIONES DE BLOQUES
     * ========================================
     *
     * Ejemplos:
     * - Camas en el Nether
     * - Anclas de reaparicion
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlockExplode(
            BlockExplodeEvent event
    ) {

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        processExplosion(event.blockList());
    }

    /*
     * ========================================
     * PROCESAR BLOQUES AFECTADOS
     * ========================================
     */

    private void processExplosion(
            List<Block> affectedBlocks
    ) {

        List<GoldOreBlock> ores =
                new ArrayList<>();

        Iterator<Block> iterator =
                affectedBlocks.iterator();

        while (iterator.hasNext()) {

            Block block = iterator.next();

            Material material = block.getType();

            if (!isGoldOre(material)) {
                continue;
            }

            /*
             * Guardar la mena afectada.
             */
            ores.add(
                    new GoldOreBlock(
                            block,
                            material
                    )
            );

            /*
             * Excluirla de los bloques
             * procesados por la explosion.
             *
             * Asi evitamos sus drops vanilla.
             */
            iterator.remove();
        }

        if (ores.isEmpty()) {
            return;
        }

        /*
         * ========================================
         * DESTRUIR SIN GENERAR DROPS
         * ========================================
         *
         * En el siguiente tick eliminamos
         * las menas guardadas.
         *
         * No usamos breakNaturally(),
         * porque generaria objetos.
         */

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

                    for (GoldOreBlock ore : ores) {

                        Block block = ore.block();

                        /*
                         * Evitar eliminar un bloque
                         * si ha cambiado desde
                         * la explosion.
                         */
                        if (block.getType()
                                != ore.originalType()) {
                            continue;
                        }

                        /*
                         * Eliminar la mena.
                         *
                         * No genera items
                         * ni experiencia.
                         */
                        block.setType(
                                Material.AIR,
                                false
                        );
                    }
                }
        );
    }

    /*
     * ========================================
     * IDENTIFICAR MENAS DE ORO
     * ========================================
     */

    private boolean isGoldOre(
            Material material
    ) {

        return material == Material.GOLD_ORE
                || material == Material.DEEPSLATE_GOLD_ORE
                || material == Material.NETHER_GOLD_ORE;
    }
}