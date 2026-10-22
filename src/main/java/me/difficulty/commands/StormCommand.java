package me.difficulty.commands;

import me.difficulty.storm.StormManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StormCommand
        implements CommandExecutor, TabCompleter {

    private final StormManager stormManager;


    public StormCommand(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    /*
     * ========================================
     * COMANDO
     * ========================================
     */

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        /*
         * ========================================
         * SIN ARGUMENTOS
         * ========================================
         */

        if (args.length == 0) {

            sender.sendMessage(
                    "§6/storm start"
            );

            sender.sendMessage(
                    "§6/storm stop"
            );


            return true;
        }


        /*
         * ========================================
         * START
         * ========================================
         */

        if (args[0].equalsIgnoreCase(
                "start"
        )) {

            if (stormManager.isActive()) {

                sender.sendMessage(
                        "§cLa Tormenta ya está activa."
                );

                return true;
            }


            stormManager.startStorm();


            sender.sendMessage(
                    "§aTormenta iniciada."
            );


            return true;
        }


        /*
         * ========================================
         * STOP
         * ========================================
         */

        if (args[0].equalsIgnoreCase(
                "stop"
        )) {

            if (!stormManager.isActive()) {

                sender.sendMessage(
                        "§cNo hay ninguna Tormenta activa."
                );

                return true;
            }


            stormManager.stopStorm();


            sender.sendMessage(
                    "§aTormenta detenida."
            );


            return true;
        }


        /*
         * ========================================
         * ARGUMENTO INCORRECTO
         * ========================================
         */

        sender.sendMessage(
                "§cUso: /storm <start|stop>"
        );


        return true;
    }


    /*
     * ========================================
     * AUTOCOMPLETADO
     * ========================================
     */

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {

        /*
         * Únicos subcomandos:
         *
         * /storm start
         * /storm stop
         */

        if (args.length == 1) {

            List<String> options =
                    List.of(
                            "start",
                            "stop"
                    );


            List<String> results =
                    new ArrayList<>();


            String current =
                    args[0].toLowerCase();


            for (String option :
                    options) {


                if (option.startsWith(
                        current
                )) {

                    results.add(
                            option
                    );
                }
            }


            return results;
        }


        return List.of();
    }
}