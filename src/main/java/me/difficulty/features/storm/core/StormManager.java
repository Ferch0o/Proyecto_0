package me.difficulty.features.storm.core;

import me.difficulty.core.day.DayManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StormManager {

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    /*
     * ========================================
     * ESTADO DE LA TORMENTA
     * ========================================
     */

    private boolean active;

    private long remainingSeconds;

    private long totalStormSeconds;

    private int startDay;


    /*
     * ========================================
     * BOSS BAR
     * ========================================
     */

    private BossBar bossBar;


    public StormManager(
            JavaPlugin plugin,
            DayManager dayManager
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;


        loadState();


        /*
         * Si el servidor se reinició mientras
         * había una Tormenta activa:
         *
         * - restauramos clima
         * - restauramos BossBar
         */
        if (active) {

            keepStormWeatherActive();

            createBossBar();

            updateBossBar();
        }
    }


    /*
     * ========================================
     * CARGAR ESTADO
     * ========================================
     */

    private void loadState() {

        FileConfiguration config =
                plugin.getConfig();


        active =
                config.getBoolean(
                        "storm.active",
                        false
                );


        remainingSeconds =
                config.getLong(
                        "storm.remaining-seconds",
                        0L
                );


        startDay =
                config.getInt(
                        "storm.start-day",
                        0
                );


// Recuperar la duración total de la tormenta.
//
// Si el servidor tenía una tormenta guardada
// con la versión anterior, recuperamos un
// total compatible con ese guardado.

        long legacyTotal = Math.max(
                remainingSeconds,
                Math.max(1, startDay) * 3600L
        );

        totalStormSeconds = config.getLong(
                "storm.total-seconds",
                legacyTotal
        );

        if (active) {
            totalStormSeconds = Math.max(
                    totalStormSeconds,
                    remainingSeconds
            );
        } else {
            totalStormSeconds = 0L;
        }

        /*
         * Seguridad.
         */
        if (active && remainingSeconds <= 0) {

            active = false;

            remainingSeconds = 0L;

            totalStormSeconds = 0L;

            startDay = 0;

            saveState();
        }
    }

    /*
     * ========================================
     * INICIAR TORMENTA CON COMANDO
     * ========================================
     *
     * Conserva el comportamiento de /storm start.
     *
     * Las muertes y tormentas naturales usarán
     * addStormTime() directamente.
     */

    public boolean startStorm() {

        if (active) {
            return false;
        }

        long seconds =
                Math.max(
                        1,
                        dayManager.getCurrentDay()
                ) * 3600L;

        addStormTime(
                seconds,
                "Comando administrativo"
        );

        return true;
    }


    /*
     * ========================================
     * AÑADIR TIEMPO DE TORMENTA
     * ========================================
     *
     * Funciona si:
     *
     * - No hay tormenta.
     * - Ya existe una tormenta.
     *
     * Nunca reinicia el tiempo acumulado.
     */

    public void addStormTime(
            long seconds,
            String reason
    ) {

        if (seconds <= 0) {
            return;
        }

        boolean wasActive = active;

        /*
         * Primera activación.
         */

        if (!active) {

            active = true;

            startDay =
                    dayManager.getCurrentDay();

            remainingSeconds = 0L;
            totalStormSeconds = 0L;
        }

        /*
         * Acumular la nueva duración.
         */

        remainingSeconds += seconds;
        totalStormSeconds += seconds;

        /*
         * Mantener clima y BossBar.
         */

        keepStormWeatherActive();

        createBossBar();
        updateBossBar();

        /*
         * Guardar inmediatamente el nuevo tiempo.
         */

        saveState();

        /*
         * Anunciar el evento.
         */

        if (wasActive) {

            Bukkit.broadcastMessage(
                    "§6§l¡LA TORMENTA SE HA EXTENDIDO!"
            );

        } else {

            Bukkit.broadcastMessage(
                    "§4§l¡LA TORMENTA HA COMENZADO!"
            );
        }

        Bukkit.broadcastMessage(
                "§eCausa: §f" + reason
        );

        Bukkit.broadcastMessage(
                "§eTiempo añadido: §f"
                        + formatAddedTime(seconds)
        );

        Bukkit.broadcastMessage(
                "§eTiempo restante: §f"
                        + getFormattedRemainingTime()
        );
    }


    /*
     * ========================================
     * FORMATEAR TIEMPO AÑADIDO
     * ========================================
     */

    private String formatAddedTime(long seconds) {

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return hours + "h " + minutes + "min";
    }

    /*
     * ========================================
     * DETENER TORMENTA
     * ========================================
     */

    public boolean stopStorm() {

        if (!active) {
            return false;
        }


        active = false;

        remainingSeconds = 0L;

        totalStormSeconds = 0L;

        startDay = 0;


        /*
         * ========================================
         * QUITAR CLIMA
         * ========================================
         */

        clearStormWeather();


        /*
         * ========================================
         * QUITAR BOSS BAR
         * ========================================
         */

        removeBossBar();


        /*
         * Guardar estado.
         */
        saveState();


        Bukkit.broadcastMessage(
                "§a§lLa Tormenta ha terminado."
        );


        return true;
    }


    /*
     * ========================================
     * PASAR 1 SEGUNDO
     * ========================================
     */

    public void tickSecond() {

        if (!active) {
            return;
        }


        /*
         * ========================================
         * MANTENER CLIMA
         * ========================================
         */

        keepStormWeatherActive();


        /*
         * ========================================
         * RESTAR 1 SEGUNDO
         * ========================================
         */

        if (remainingSeconds > 0) {

            remainingSeconds--;
        }


        /*
         * ========================================
         * ¿TERMINÓ?
         * ========================================
         */

        if (remainingSeconds <= 0) {

            finishStorm();

            return;
        }


        /*
         * ========================================
         * ACTUALIZAR BOSS BAR
         * ========================================
         */

        updateBossBar();


        /*
         * ========================================
         * GUARDAR CADA MINUTO
         * ========================================
         */

        if (remainingSeconds % 60 == 0) {

            saveState();
        }
    }


    /*
     * ========================================
     * FINALIZAR AUTOMÁTICAMENTE
     * ========================================
     */

    private void finishStorm() {

        active = false;

        remainingSeconds = 0L;

        totalStormSeconds = 0L;

        startDay = 0;


        clearStormWeather();

        removeBossBar();

        saveState();


        Bukkit.broadcastMessage(
                "§a§lLa Tormenta ha terminado."
        );
    }


    /*
     * ========================================
     * CREAR BOSS BAR
     * ========================================
     */

    private void createBossBar() {

        /*
         * Si ya existe, no hacemos otra.
         */
        if (bossBar != null) {
            return;
        }


        /*
         * SEGMENTED_20 hace que la barra
         * tenga divisiones similares a la
         * imagen que mostraste.
         */
        bossBar =
                Bukkit.createBossBar(
                        "§4§lTormenta",
                        BarColor.RED,
                        BarStyle.SOLID
                );


        bossBar.setVisible(
                true
        );


        /*
         * Añadir jugadores actualmente
         * conectados.
         */
        for (Player player :
                Bukkit.getOnlinePlayers()) {

            bossBar.addPlayer(
                    player
            );
        }
    }


    /*
     * ========================================
     * ACTUALIZAR BOSS BAR
     * ========================================
     */

    private void updateBossBar() {

        if (!active) {
            return;
        }


        /*
         * Si por alguna razón no existe,
         * la volvemos a crear.
         */
        if (bossBar == null) {

            createBossBar();
        }


        /*
         * ========================================
         * AÑADIR JUGADORES NUEVOS
         * ========================================
         *
         * Así alguien que entre al servidor
         * cuando la Tormenta ya empezó verá
         * el BossBar como máximo 1 segundo
         * después.
         */

        for (Player player :
                Bukkit.getOnlinePlayers()) {

            if (!bossBar.getPlayers()
                    .contains(player)) {

                bossBar.addPlayer(
                        player
                );
            }
        }


        /*
         * ========================================
         * TEXTO
         * ========================================
         */

        bossBar.setTitle(
                "§4§lTormenta §8» §f"
                        + getFormattedRemainingTime()
        );


        /*
         * ========================================
         * PROGRESO
         * ========================================
         */

        long totalSeconds =
                getTotalStormSeconds();


        double progress;


        if (totalSeconds <= 0) {

            progress = 0.0;

        } else {

            progress =
                    (double) remainingSeconds
                            / (double) totalSeconds;
        }


        /*
         * Seguridad:
         *
         * BossBar solamente acepta
         * valores entre 0.0 y 1.0.
         */

        progress =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                progress
                        )
                );


        bossBar.setProgress(
                progress
        );


        bossBar.setVisible(
                true
        );
    }


    /*
     * ========================================
     * RESTAURAR BOSSBAR AL ENTRAR
     * ========================================
     *
     * Reconecta un jugador a la barra
     * que ya existe, sin reiniciar
     * la tormenta ni modificar su tiempo.
     */

    public void showBossBarFor(Player player) {

        if (!active || !player.isOnline()) {
            return;
        }

        if (bossBar == null) {
            createBossBar();
        }

        if (bossBar == null) {
            return;
        }

        /*
         * Forzar una nueva asociación.
         *
         * Esto evita que una referencia
         * antigua impida mostrar la barra
         * en una conexión nueva.
         */

        bossBar.removePlayer(player);
        bossBar.addPlayer(player);

        bossBar.setVisible(true);

        // Sincronizar tiempo y progreso.
        updateBossBar();
    }


    /*
     * ========================================
     * RETIRAR BOSSBAR AL DESCONECTARSE
     * ========================================
     *
     * Evitar conservar al jugador
     * en la lista de espectadores.
     */

    public void removeBossBarFor(Player player) {

        if (bossBar == null) {
            return;
        }

        bossBar.removePlayer(player);
    }

    /*
     * ========================================
     * QUITAR BOSS BAR
     * ========================================
     */

    private void removeBossBar() {

        if (bossBar == null) {
            return;
        }


        bossBar.setVisible(
                false
        );


        bossBar.removeAll();


        bossBar = null;
    }


    /*
     * ========================================
     * DURACIÓN TOTAL
     * ========================================
     */

    private long getTotalStormSeconds() {
        return totalStormSeconds;
    }

    /*
     * ========================================
     * MANTENER CLIMA
     * ========================================
     */

    private void keepStormWeatherActive() {

        for (World world :
                Bukkit.getWorlds()) {


            /*
             * Solo Overworld.
             */
            if (world.getEnvironment()
                    != World.Environment.NORMAL) {

                continue;
            }


            world.setClearWeatherDuration(
                    0
            );


            world.setStorm(
                    true
            );


            world.setThundering(
                    true
            );


            world.setWeatherDuration(
                    Integer.MAX_VALUE
            );


            world.setThunderDuration(
                    Integer.MAX_VALUE
            );
        }
    }


    /*
     * ========================================
     * DESPEJAR CLIMA
     * ========================================
     */

    private void clearStormWeather() {

        for (World world :
                Bukkit.getWorlds()) {


            if (world.getEnvironment()
                    != World.Environment.NORMAL) {

                continue;
            }


            world.setStorm(
                    false
            );


            world.setThundering(
                    false
            );


            world.setWeatherDuration(
                    0
            );


            world.setThunderDuration(
                    0
            );


            world.setClearWeatherDuration(
                    0
            );
        }
    }


    /*
     * ========================================
     * GUARDAR ESTADO
     * ========================================
     */

    public void saveState() {

        FileConfiguration config =
                plugin.getConfig();

        config.set(
                "storm.active",
                active
        );


        config.set(
                "storm.remaining-seconds",
                remainingSeconds
        );

        config.set(
                "storm.total-seconds",
                totalStormSeconds
        );

        config.set(
                "storm.start-day",
                startDay
        );


        plugin.saveConfig();
    }


    /*
     * ========================================
     * GETTERS
     * ========================================
     */

    public boolean isActive() {

        return active;
    }


    public long getRemainingSeconds() {

        return remainingSeconds;
    }


    public int getStartDay() {

        return startDay;
    }


    /*
     * ========================================
     * FORMATEAR TIEMPO
     * ========================================
     */

    public String getFormattedRemainingTime() {

        long total =
                remainingSeconds;


        long hours =
                total / 3600;


        long minutes =
                (total % 3600) / 60;


        long seconds =
                total % 60;


        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }

    /*
     * ========================================
     * CAMBIOS ESPECIALES DE TORMENTA
     * ========================================
     *
     * Antes del Día 10 la tormenta
     * es solamente climatológica.
     */

    public boolean hasDay10Effects() {
        return active && dayManager.isAtLeast(10);
    }
}