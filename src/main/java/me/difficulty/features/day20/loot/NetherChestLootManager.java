
package me.difficulty.features.day20.loot;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;

import org.bukkit.entity.HumanEntity;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.LootGenerateEvent;

import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public final class NetherChestLootManager implements Listener {

    /*
     * ========================================
     * CONFIGURACION GENERAL
     * ========================================
     */

    private static final String ROUND_CONFIG =
            "nether-loot.refill-round";

    private static final int CHUNKS_PER_TICK = 3;

    private static final long RETRY_TICKS = 20L;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final NetherLootGenerator generator;

    private final NamespacedKey chestTypeKey;
    private final NamespacedKey lastRoundKey;

    private final Queue<Chunk> pendingChunks =
            new ArrayDeque<>();

    private final Set<String> queuedChunks =
            new HashSet<>();

    /*
     * Cofres cuya LootTable se esta generando.
     *
     * Evita que InventoryOpenEvent o el escaneo
     * intenten rellenarlos por segunda vez.
     */
    private final Set<String> generatingLoot =
            new HashSet<>();

    /*
     * Evita registrar un cofre nuevo colocado
     * inmediatamente tras destruir el original.
     */
    private final Set<String> recentlyBroken =
            new HashSet<>();

    private int currentRound;

    public NetherChestLootManager(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomItemRegistry items
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;

        this.generator =
                new NetherLootGenerator(items);

        this.chestTypeKey = new NamespacedKey(
                plugin,
                "nether_natural_chest_type"
        );

        this.lastRoundKey = new NamespacedKey(
                plugin,
                "nether_chest_last_round"
        );

        this.currentRound = Math.max(
                0,
                plugin.getConfig().getInt(
                        ROUND_CONFIG,
                        0
                )
        );

        /*
         * Si el plugin se instala cuando el
         * servidor ya esta en el dia 20.
         */
        if (dayManager.isAtLeast(20)
                && currentRound == 0) {

            saveRound(1);
        }

        dayManager.addDayChangeListener(
                this::onDayChange
        );
    }

    /*
     * ========================================
     * INICIAR EL SISTEMA
     * ========================================
     */
    public void start() {

        Bukkit.getPluginManager().registerEvents(
                this,
                plugin
        );

        /*
         * Revisar hasta tres chunks por tick.
         * No cargamos regiones lejanas.
         */
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {

                    for (int i = 0;
                         i < CHUNKS_PER_TICK
                                 && !pendingChunks.isEmpty();
                         i++) {

                        Chunk chunk = pendingChunks.poll();

                        queuedChunks.remove(
                                chunkKey(chunk)
                        );

                        if (chunk.isLoaded()) {
                            inspectChunk(chunk);
                        }
                    }

                },
                1L,
                1L
        );

        Bukkit.getScheduler().runTask(
                plugin,
                this::enqueueLoadedChunks
        );
    }

    /*
     * ========================================
     * CONTROL DE DIAS Y RONDAS
     * ========================================
     */
    private void onDayChange(
            int previous,
            int current
    ) {

        /*
         * Una nueva ronda ocurre solamente
         * al cruzar desde antes del dia 20
         * al dia 20 o superior.
         *
         * Ejemplo de testeo:
         * 19 -> 20 : ronda 1
         * 20 -> 19 : sin refill
         * 19 -> 20 : ronda 2
         */
        if (previous < 20 && current >= 20) {

            saveRound(currentRound + 1);

            enqueueLoadedChunks();

            plugin.getLogger().info(
                    "Loot del Nether: ronda de refill " +
                            currentRound
            );
        }
    }

    private void saveRound(int round) {

        currentRound = round;

        plugin.getConfig().set(
                ROUND_CONFIG,
                currentRound
        );

        plugin.saveConfig();
    }

    /*
     * ========================================
     * COLA DE CHUNKS
     * ========================================
     */

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if (!isNether(event.getWorld())) {
            return;
        }

        scheduleChunk(event.getChunk(), 2L);
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {

        if (!isNether(event.getWorld())) {
            return;
        }

        scheduleChunk(event.getChunk(), 2L);
    }

    private void scheduleChunk(
            Chunk chunk,
            long delay
    ) {

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    if (chunk.isLoaded()) {
                        enqueueChunk(chunk);
                    }

                },
                delay
        );
    }

    private void enqueueLoadedChunks() {

        for (World world : Bukkit.getWorlds()) {

            if (!isNether(world)) {
                continue;
            }

            for (Chunk chunk : world.getLoadedChunks()) {

                enqueueChunk(chunk);
            }
        }
    }

    private void enqueueChunk(Chunk chunk) {

        if (!isNether(chunk.getWorld())) {
            return;
        }

        String key = chunkKey(chunk);

        if (queuedChunks.add(key)) {

            pendingChunks.add(chunk);
        }
    }

    private String chunkKey(Chunk chunk) {

        return chunk.getWorld().getUID() +
                ":" + chunk.getX() +
                ":" + chunk.getZ();
    }

    private String blockKey(Location location) {

        return location.getWorld().getUID() +
                ":" + location.getBlockX() +
                ":" + location.getBlockY() +
                ":" + location.getBlockZ();
    }

    private boolean isNether(World world) {

        return world != null &&
                world.getEnvironment() ==
                        World.Environment.NETHER;
    }

    /*
     * ========================================
     * INSPECCIONAR CHUNK
     * ========================================
     */
    private void inspectChunk(Chunk chunk) {

        for (BlockState state :
                chunk.getTileEntities(true)) {

            if (!(state instanceof Chest snapshot)) {
                continue;
            }

            String key = blockKey(
                    snapshot.getLocation()
            );

            /*
             * No intervenir mientras Minecraft
             * esta generando el loot inicial.
             */
            if (generatingLoot.contains(key)) {
                continue;
            }

            /*
             * Recuperar el estado actual.
             */
            BlockState current =
                    snapshot.getBlock().getState();

            if (!(current instanceof Chest chest)) {
                continue;
            }

            NetherLootType type =
                    identifyChest(chest);

            if (type == null) {
                continue;
            }

            if (dayManager.isAtLeast(20)
                    && currentRound > 0) {

                /*
                 * El cofre puede seguir teniendo
                 * una tabla vanilla pendiente.
                 * El refill la sustituira.
                 */
                BlockState refreshed =
                        chest.getBlock().getState();

                if (refreshed instanceof Chest actual) {

                    refill(actual, type, null);
                }
            }
        }
    }

    /*
     * ========================================
     * IDENTIFICAR COFRE NATURAL
     * ========================================
     */
    private NetherLootType identifyChest(
            Chest chest
    ) {

        PersistentDataContainer data =
                chest.getPersistentDataContainer();

        /*
         * Prioridad 1:
         * marcador guardado previamente.
         */
        String stored = data.get(
                chestTypeKey,
                PersistentDataType.STRING
        );

        NetherLootType type =
                NetherLootType.fromStoredId(stored);

        if (type != null) {
            return type;
        }

        /*
         * Prioridad 2:
         * LootTable vanilla sin consumir.
         *
         * Cofres antiguos que ya perdieron
         * su tabla y no tienen marcador
         * NO se identifican a la fuerza.
         */
        LootTable table = chest.getLootTable();

        if (table == null) {
            return null;
        }

        type = NetherLootType.fromLootTable(
                table.getKey()
        );

        if (type == null) {
            return null;
        }

        /*
         * Guardar el tipo sin abrir
         * ni modificar el inventario.
         */
        data.set(
                chestTypeKey,
                PersistentDataType.STRING,
                type.name()
        );

        if (!chest.update(false, false)) {

            plugin.getLogger().warning(
                    "No se pudo marcar el cofre en " +
                            chest.getLocation()
            );

            return null;
        }

        return type;
    }

    /*
     * ========================================
     * COMPROBAR RONDA DEL COFRE
     * ========================================
     */
    private boolean isCurrentRound(
            Chest chest
    ) {

        Integer round =
                chest.getPersistentDataContainer().get(
                        lastRoundKey,
                        PersistentDataType.INTEGER
                );

        return round != null &&
                round >= currentRound;
    }

    /*
     * ========================================
     * REFILL DEL COFRE
     * ========================================
     *
     * Devuelve true cuando el cofre ya tiene
     * la ronda actual o se relleno bien.
     *
     * Devuelve false cuando hay que esperar
     * o se produjo un error.
     */
    private boolean refill(
            Chest chest,
            NetherLootType type,
            UUID openingPlayer
    ) {

        if (!dayManager.isAtLeast(20)
                || currentRound <= 0) {

            return true;
        }

        if (isCurrentRound(chest)) {
            return true;
        }

        String key = blockKey(
                chest.getLocation()
        );

        /*
         * Proteccion frente a doble
         * generacion en el mismo tick.
         */
        if (generatingLoot.contains(key)) {
            return false;
        }

        /*
         * Si otro jugador esta utilizando
         * este cofre o cualquiera de sus
         * mitades, aplazar el refill.
         */
        if (isBeingViewed(chest, openingPlayer)) {

            scheduleChunk(
                    chest.getChunk(),
                    RETRY_TICKS
            );

            return false;
        }

        try {

            /*
             * Generar primero.
             * Si ocurre un error, no tocamos
             * el contenido del cofre.
             */
            List<ItemStack> loot =
                    generator.generate(
                            type,
                            ThreadLocalRandom.current()
                    );

            /*
             * Inventario capturado.
             * No fuerza la generacion de
             * la LootTable pendiente.
             *
             * Un cofre doble se procesa
             * por cada mitad natural.
             */
            Inventory inventory =
                    chest.getSnapshotInventory();

            if (loot.size() > inventory.getSize()) {

                throw new IllegalStateException(
                        "Loot mayor al inventario: " +
                                type
                );
            }

            /*
             * Reemplazar todo el contenido
             * anterior de esta mitad.
             */
            inventory.clear();

            List<Integer> slots =
                    new ArrayList<>();

            for (int i = 0;
                 i < inventory.getSize();
                 i++) {

                slots.add(i);
            }

            Collections.shuffle(
                    slots,
                    ThreadLocalRandom.current()
            );

            for (int i = 0; i < loot.size(); i++) {

                inventory.setItem(
                        slots.get(i),
                        loot.get(i)
                );
            }

            /*
             * Eliminar la generacion vanilla
             * pendiente para este cofre.
             */
            chest.setLootTable(null);

            /*
             * Marcar la ronda solamente
             * tras generar exitosamente.
             */
            chest.getPersistentDataContainer().set(
                    lastRoundKey,
                    PersistentDataType.INTEGER,
                    currentRound
            );

            if (!chest.update(false, false)) {

                throw new IllegalStateException(
                        "No se pudo guardar el refill"
                );
            }

            return true;

        } catch (Exception exception) {

            plugin.getLogger().log(
                    Level.SEVERE,
                    "Error rellenando cofre " +
                            type + " en " +
                            chest.getLocation(),
                    exception
            );

            return false;
        }
    }

    /*
     * ========================================
     * DETECTAR INVENTARIOS ABIERTOS
     * ========================================
     *
     * getInventory() incluye ambas mitades
     * si el bloque forma un cofre doble.
     *
     * Este metodo solo se utiliza cuando
     * el cofre no esta generando loot.
     */
    private boolean isBeingViewed(
            Chest chest,
            UUID excludedPlayer
    ) {

        for (HumanEntity viewer :
                chest.getInventory().getViewers()) {

            if (excludedPlayer != null
                    && viewer.getUniqueId().equals(
                    excludedPlayer
            )) {

                continue;
            }

            return true;
        }

        return false;
    }

    /*
     * ========================================
     * UBICACIONES DE INVENTARIO
     * ========================================
     *
     * Permite reconocer un cofre simple
     * o las dos mitades de un cofre doble.
     */
    private List<Location> getChestLocations(
            Inventory inventory
    ) {

        List<Location> locations =
                new ArrayList<>();

        if (inventory instanceof DoubleChestInventory both) {

            Location left =
                    both.getLeftSide().getLocation();

            Location right =
                    both.getRightSide().getLocation();

            if (left != null) {
                locations.add(left);
            }

            if (right != null) {
                locations.add(right);
            }

        } else {

            Location location =
                    inventory.getLocation();

            if (location != null) {
                locations.add(location);
            }
        }

        return locations;
    }

    /*
     * ========================================
     * APERTURA DE COFRES
     * ========================================
     *
     * Respaldo por si un jugador abre
     * un cofre ya registrado antes de
     * que termine el escaneo del chunk.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryOpen(
            InventoryOpenEvent event
    ) {

        if (!dayManager.isAtLeast(20)
                || currentRound <= 0) {

            return;
        }

        for (Location location :
                getChestLocations(event.getInventory())) {

            if (!isNether(location.getWorld())) {
                continue;
            }

            String key = blockKey(location);

            /*
             * Si LootGenerateEvent ya intervino,
             * nunca generar otra vez.
             */
            if (generatingLoot.contains(key)) {
                continue;
            }

            BlockState state =
                    location.getBlock().getState();

            if (!(state instanceof Chest chest)) {
                continue;
            }

            NetherLootType type =
                    identifyChest(chest);

            if (type == null) {
                continue;
            }

            /*
             * Si la LootTable original sigue
             * pendiente, dejar que el evento
             * de generacion se encargue.
             */
            if (chest.getLootTable() != null) {
                continue;
            }

            /*
             * Si ya recibio esta ronda,
             * no hay nada que hacer.
             */
            if (isCurrentRound(chest)) {
                continue;
            }

            /*
             * Cofre abierto anteriormente,
             * pero pendiente del refill.
             */
            boolean success = refill(
                    chest,
                    type,
                    event.getPlayer().getUniqueId()
            );

            if (!success) {

                /*
                 * Evitar mostrar loot antiguo
                 * cuando el refill esta pendiente.
                 */
                event.setCancelled(true);

                scheduleChunk(
                        chest.getChunk(),
                        RETRY_TICKS
                );

                return;
            }
        }
    }

    /*
     * ========================================
     * GENERACION INICIAL DE LOOT
     * ========================================
     *
     * Se ejecuta cuando Minecraft consume
     * una LootTable natural por primera vez.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onLootGenerate(
            LootGenerateEvent event
    ) {

        if (event.isPlugin()
                || !isNether(event.getWorld())) {

            return;
        }

        NetherLootType type =
                NetherLootType.fromLootTable(
                        event.getLootTable().getKey()
                );

        if (type == null) {
            return;
        }

        boolean day20 =
                dayManager.isAtLeast(20)
                        && currentRound > 0;

        /*
         * Esta variable es diferente del dia.
         * Solo sera true si el loot personalizado
         * se genero correctamente.
         */
        boolean customSuccess = false;

        if (day20) {

            try {

                event.setLoot(
                        generator.generate(
                                type,
                                ThreadLocalRandom.current()
                        )
                );

                customSuccess = true;

            } catch (Exception exception) {

                plugin.getLogger().log(
                        Level.SEVERE,
                        "Error generando loot de " + type +
                                ". Se mantiene el loot vanilla.",
                        exception
                );
            }
        }

        /*
         * El evento puede no tener
         * InventoryHolder, por ejemplo
         * cuando se destruye un cofre.
         */
        InventoryHolder holder =
                event.getInventoryHolder();

        if (!(holder instanceof Chest source)) {
            return;
        }

        Location location =
                source.getLocation();

        String key = blockKey(location);

        final boolean succeeded =
                customSuccess;

        final int generatedRound =
                customSuccess ? currentRound : 0;

        /*
         * La marca temporal evita que
         * InventoryOpenEvent genere otra
         * recompensa en el mismo acceso.
         */
        generatingLoot.add(key);

        /*
         * Registrar la identificacion
         * en el siguiente tick, despues de
         * que Minecraft termine de llenar
         * el inventario.
         */
        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

                    try {

                        if (event.isCancelled()) {
                            return;
                        }

                        if (recentlyBroken.contains(key)) {
                            return;
                        }

                        World world =
                                location.getWorld();

                        if (world == null
                                || !world.isChunkLoaded(
                                location.getBlockX() >> 4,
                                location.getBlockZ() >> 4
                        )) {

                            return;
                        }

                        BlockState state =
                                location.getBlock().getState();

                        if (!(state instanceof Chest chest)) {
                            return;
                        }

                        PersistentDataContainer data =
                                chest.getPersistentDataContainer();

                        data.set(
                                chestTypeKey,
                                PersistentDataType.STRING,
                                type.name()
                        );

                        /*
                         * Nunca marcar como completada
                         * una generacion personalizada
                         * que haya fallado.
                         */
                        if (succeeded && generatedRound > 0) {

                            data.set(
                                    lastRoundKey,
                                    PersistentDataType.INTEGER,
                                    generatedRound
                            );
                        }

                        if (!chest.update(false, false)) {

                            plugin.getLogger().warning(
                                    "No se pudo registrar el loot de " +
                                            type + " en " + location
                            );
                        }

                    } finally {

                        generatingLoot.remove(key);
                    }
                }
        );
    }

    /*
     * ========================================
     * DESTRUCCION DE COFRES
     * ========================================
     *
     * Si un cofre natural se destruye,
     * el bloque nuevo no hereda el marcador.
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onChestBreak(BlockBreakEvent event) {

        Block block = event.getBlock();

        if (!isNether(block.getWorld())
                || block.getType() !=
                org.bukkit.Material.CHEST) {

            return;
        }

        String key = blockKey(
                block.getLocation()
        );

        recentlyBroken.add(key);

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> recentlyBroken.remove(key),
                2L
        );
    }
}