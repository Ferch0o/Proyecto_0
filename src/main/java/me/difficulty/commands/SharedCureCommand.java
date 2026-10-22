
package me.difficulty.commands;

import me.difficulty.listeners.SharedVillagerCureListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;


public class SharedCureCommand implements CommandExecutor {

    private final SharedVillagerCureListener cureListener;


    public SharedCureCommand(
            SharedVillagerCureListener cureListener
    ) {

        this.cureListener = cureListener;
    }


    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        /*
         * Solo jugadores dentro del servidor.
         */

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Este comando debe ejecutarse dentro del juego."
            );

            return true;
        }


        /*
         * Solo administradores.
         */

        if (!player.hasPermission(
                "difficulty.sharedcure.admin"
        )) {

            player.sendMessage(
                    "§cNo tienes permiso para usar este comando."
            );

            return true;
        }


        /*
         * Único subcomando:
         *
         * /sharedcure add
         */

        if (args.length != 1
                || !args[0].equalsIgnoreCase("add")) {

            player.sendMessage(
                    "§eUso: /sharedcure add"
            );

            return true;
        }


        /*
         * Buscar únicamente la entidad
         * que el jugador está mirando.
         *
         * Distancia máxima: 6 bloques.
         * No atraviesa paredes.
         */

        Entity target = player.getTargetEntity(
                6,
                false
        );


        if (!(target instanceof Villager villager)) {

            player.sendMessage(
                    "§cDebes mirar directamente a un aldeano a menos de 6 bloques."
            );

            return true;
        }


        /*
         * Evitar registros duplicados.
         */

        if (cureListener.isShared(villager)) {

            player.sendMessage(
                    "§eEste aldeano ya tiene descuentos compartidos."
            );

            return true;
        }


        /*
         * Verificar evidencia de curación.
         *
         * Si no existe MAJOR_POSITIVE
         * de ningún jugador, rechazamos.
         */

        if (!cureListener.registerLegacyCure(villager)) {

            player.sendMessage(
                    "§cNo se encontró evidencia de que este aldeano haya sido curado."
            );

            player.sendMessage(
                    "§7No se han modificado sus descuentos."
            );

            return true;
        }


        player.sendMessage(
                "§a¡Descuentos compartidos activados!"
        );

        player.sendMessage(
                "§7Todos los jugadores podrán aprovechar la reputación de curación de este aldeano."
        );

        return true;
    }
}
