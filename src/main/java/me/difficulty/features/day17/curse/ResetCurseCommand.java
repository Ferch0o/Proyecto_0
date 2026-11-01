
package me.difficulty.features.day17.curse;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ResetCurseCommand
        implements CommandExecutor {

    private final CurseManager manager;
    private final CurseInventoryService inventory;

    public ResetCurseCommand(
            CurseManager manager,
            CurseInventoryService inventory
    ) {
        this.manager = manager;
        this.inventory = inventory;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        /*
         * ========================================
         * NOMBRE DEL JUGADOR OBLIGATORIO
         * ========================================
         */

        if (args.length != 1 || args[0].isBlank()) {

            sender.sendMessage(
                    "§cUso correcto: /resetcurse <jugador>"
            );

            return true;
        }

        /*
         * Solo desde el día 17.
         */
        if (manager.currentDay() < 17) {

            sender.sendMessage(
                    "§cLa Maldición está disponible "
                            + "desde el día 17."
            );

            return true;
        }

        /*
         * Buscar al jugador indicado.
         */
        Player target = Bukkit.getPlayerExact(
                args[0]
        );

        if (target == null) {

            sender.sendMessage(
                    "§cEl jugador indicado "
                            + "no está conectado."
            );

            return true;
        }

        /*
         * ========================================
         * RESTABLECER LA MALDICIÓN
         * ========================================
         */

        manager.reapplyCurseForTesting(target);

        inventory.synchronize(target);

        sender.sendMessage(
                "§aMaldición restaurada a "
                        + target.getName() + "."
        );

        target.sendMessage(
                "§5La Maldición del Inframundo "
                        + "vuelve a afectarte."
        );

        return true;
    }
}
