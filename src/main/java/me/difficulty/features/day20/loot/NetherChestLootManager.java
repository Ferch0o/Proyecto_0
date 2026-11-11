
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

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.LootGenerateEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import org.bukkit.loot.LootTable;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public final class NetherChestLootManager implements Listener {

    /*
     * ========================================
     * CONFIGURACION
     * ========================================
     */

    private static final String ROUND_CONFIG =
            "nether-loot.refill-round";

    private static final int CHUNKS_PER_TICK = 3;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final NetherLootGenerator generator;

    private final NamespacedKey chestTypeKey;
    private final NamespacedKey lastRoundKey;

    private final Queue<Chunk> pendingChunks =
            new ArrayDeque<>();

    private final Set<String> queuedChunks =
            new HashSet<>();

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

        this.generator = new NetherLootGenerator(items);

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
                plugin.getConfig().getInt(ROUND_CONFIG, 0)
        );

        /*
         * Soporte para instalar el sistema
         * en un servidor que ya esta en dia 20.
         */
        if (dayManager.isAtLeast(20) && currentRound == 0) {
            saveRound(1);
        }

        dayManager.addDayChangeListener(this::onDayChange);
    }

    /*
     * ========================================
     * INICIAR SISTEMA
     * ========================================
     */
    public void start() {

        Bukkit.getPluginManager().registerEvents(
                this,
                plugin
        );

        /*
         * Procesar chunks gradualmente.
         * No cargar chunks lejanos a la fuerza.
         */
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {

                    for (int i = 0;
                         i < CHUNKS_PER_TICK &&
                                 !pendingChunks.isEmpty();
                         i++) {

                        Chunk chunk = pendingChunks.poll();

                        queuedChunks.remove(
                                getChunkKey(chunk)
                        );

                        if (!chunk.isLoaded()) {
                            continue;
                        }

                        inspectChunk(chunk);
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
     * CAMBIO DE DIA
     * ========================================
     */
    private void onDayChange(int previous, int current) {

        if (previous < 20 && current >= 20) {

            saveRound(currentRound + 1);

            enqueueLoadedChunks();

            plugin.getLogger().info(
                    "Nueva ronda de loot Nether: " +
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
     * DETECTAR CHUNKS
     * ========================================
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        if (!isNether(event.getWorld())) {
            return;
        }

        Chunk chunk = event.getChunk();

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (chunk.isLoaded()) {
                        enqueueChunk(chunk);
                    }
                },
                2L
        );
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {

        if (!isNether(event.getWorld())) {
            return;
        }

        Chunk chunk = event.getChunk();

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (chunk.isLoaded()) {
                        enqueueChunk(chunk);
                    }
                },
                2L
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

        String key = getChunkKey(chunk);

        if (queuedChunks.add(key)) {
            pendingChunks.add(chunk);
        }
    }

    private String getChunkKey(Chunk chunk) {

        return chunk.getWorld().getUID() +
                ":" + chunk.getX() +
                ":" + chunk.getZ();
    }

    private boolean isNether(World world) {

        return world.getEnvironment() ==
                World.Environment.NETHER;
    }

    /*
     * ========================================
     * REVISAR COFRES DE UN CHUNK
     * ========================================
     */
    private void inspectChunk(Chunk chunk) {

        for (BlockState state :
                chunk.getTileEntities(true)) {

            if (!(state instanceof Chest chest)) {
                continue;
            }

            NetherLootType type = identifyChest(chest);

            if (type == null) {
                continue;
            }

            if (dayManager.isAtLeast(20) &&
                    currentRound > 0) {

                // Recuperar estado actualizado del bloque.
                BlockState fresh =
                        chest.getBlock().getState();

                if (fresh instanceof Chest freshChest) {
                    refill(freshChest, type, null);
                }
            }
        }
    }

    /*
     * ========================================
     * IDENTIFICAR COFRE NATURAL
     * ========================================
     */
    private NetherLootType identifyChest(Chest chest) {

        PersistentDataContainer data =
                chest.getPersistentDataContainer();

        /*
         * Primero revisar si el cofre ya
         * fue identificado anteriormente.
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
         * Si no tiene marcador, comprobar
         * su LootTable original.
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
         * Guardar identificacion sin abrir
         * el inventario ni generar su loot.
         */
        data.set(
                chestTypeKey,
                PersistentDataType.STRING,
                type.name()
        );

        chest.update(false, false);

        return type;
    }

    /*
     * ========================================
     * REFILL DE UN COFRE
     * ========================================
     */
    private void refill(
            Chest chest,
            NetherLootType type,
            UUID openingPlayer
    ) {

        if (!dayManager.isAtLeast(20) ||
                currentRound <= 0) {
            return;
        }

        Integer previousRound =
                chest.getPersistentDataContainer().get(
                        lastRoundKey,
                        PersistentDataType.INTEGER
                );

        if (previousRound != null &&
                previousRound >= currentRound) {
            return;
        }

        /*
         * No alterar un cofre mientras otro
         * jugador ya esta viendo su contenido.
         */
        if (isBeingViewed(chest, openingPlayer)) {

            Chunk chunk = chest.getChunk();

            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> {
                        if (chunk.isLoaded()) {
                            enqueueChunk(chunk);
                        }
                    },
                    20L
            );

            return;
        }

        try {

            /*
             * Generar primero todo el loot.
             * Si falla, no borrar el contenido.
             */
            List<ItemStack> loot = generator.generate(
                    type,
                    ThreadLocalRandom.current()
            );

            /*
             * IMPORTANTE:
             * Usar el inventario snapshot evita
             * activar el loot vanilla pendiente.
             */
            Inventory inventory =
                    chest.getSnapshotInventory();

            if (loot.size() > inventory.getSize()) {
                throw new IllegalStateException(
                        "Sin espacio para loot de " + type
                );
            }

            inventory.clear();

            List<Integer> slots = new ArrayList<>();

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
             * El nuevo loot reemplaza al
             * anterior por completo.
             */
            chest.setLootTable(null);

            chest.getPersistentDataContainer().set(
                    lastRoundKey,
                    PersistentDataType.INTEGER,
                    currentRound
            );

            if (!chest.update(false, false)) {
                throw new IllegalStateException(
                        "No se pudo actualizar el cofre"
                );
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "Error rellenando cofre " +
                            type + " en " +
                            chest.getLocation() + ": " +
                            exception.getMessage()
            );
        }
    }

    /*
     * ========================================
     * COFRES ABIERTOS POR JUGADORES
     * ========================================
     */
    private boolean isBeingViewed(
            Chest chest,
            UUID excludedPlayer
    ) {

        Location target = chest.getLocation();

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (excludedPlayer != null &&
                    player.getUniqueId().equals(excludedPlayer)) {
                continue;
            }

            Location opened =
                    player.getOpenInventory()
                            .getTopInventory()
                            .getLocation();

            if (opened == null) {
                continue;
            }

            if (opened.getWorld() != null &&
                    opened.getWorld().equals(target.getWorld()) &&
                    opened.getBlockX() == target.getBlockX() &&
                    opened.getBlockY() == target.getBlockY() &&
                    opened.getBlockZ() == target.getBlockZ()) {

                return true;
            }
        }

        return false;
    }

    /*
     * Si un jugador abre un cofre registrado
     * mientras su refill sigue pendiente,
     * intentar aplicarlo antes de mostrarlo.
     */
    @EventHandler(priority = EventPriority.HIGHEST,
            ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        Location location =
                event.getInventory().getLocation();

        if (location == null ||
                !isNether(location.getWorld())) {
            return;
        }

        BlockState state =
                location.getBlock().getState();

        if (!(state instanceof Chest chest)) {
            return;
        }

        NetherLootType type = identifyChest(chest);

        if (type == null) {
            return;
        }

        refill(
                chest,
                type,
                event.getPlayer().getUniqueId()
        );
    }

    /*
     * ========================================
     * GENERACION ORIGINAL DE LOOT
     * ========================================
     *
     * Respaldo cuando un cofre se abre antes
     * de que el escaneo llegue a su chunk.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onLootGenerate(LootGenerateEvent event) {

        if (event.isPlugin() ||
                !isNether(event.getWorld())) {
            return;
        }

        NetherLootType type =
                NetherLootType.fromLootTable(
                        event.getLootTable().getKey()
                );

        if (type == null) {
            return;
        }

        boolean day20 = dayManager.isAtLeast(20) &&
                currentRound > 0;

        /*
         * Desde el dia 20 sustituir
         * las recompensas vanilla.
         */
        if (day20) {

            try {
                event.setLoot(
                        generator.generate(
                                type,
                                ThreadLocalRandom.current()
                        )
                );
            } catch (Exception exception) {

                plugin.getLogger().log(
                        java.util.logging.Level.SEVERE,
                        "Error generando loot personalizado de " + type +
                                ". Se conserva el loot vanilla como respaldo.",
                        exception
                );

            }
        }

        InventoryHolder holder =
                event.getInventoryHolder();

        /*
         * Puede ser null si el cofre se
         * destruye al generar el loot.
         */
        if (!(holder instanceof Chest chest)) {
            return;
        }

        Location location = chest.getLocation();
        int generatedRound = day20 ? currentRound : 0;

        /*
         * Registrar en el siguiente tick,
         * cuando Minecraft haya terminado
         * de generar el contenido.
         */
        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!location.getWorld().isChunkLoaded(
                    location.getBlockX() >> 4,
                    location.getBlockZ() >> 4
            )) {
                return;
            }

            if (recentlyBroken.contains(getBlockKey(location))) {
                return;
            }

            BlockState state =
                    location.getBlock().getState();

            if (!(state instanceof Chest actual)) {
                return;
            }

            PersistentDataContainer data =
                    actual.getPersistentDataContainer();

            data.set(
                    chestTypeKey,
                    PersistentDataType.STRING,
                    type.name()
            );

            if (generatedRound > 0) {
                data.set(
                        lastRoundKey,
                        PersistentDataType.INTEGER,
                        generatedRound
                );
            }

            actual.update(false, false);
        });
    }

    /*
     * ========================================
     * EVITAR MARCAR UN COFRE REEMPLAZADO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onChestBreak(BlockBreakEvent event) {

        Block block = event.getBlock();

        if (!isNether(block.getWorld()) ||
                block.getType() != org.bukkit.Material.CHEST) {
            return;
        }

        String key = getBlockKey(block.getLocation());

        recentlyBroken.add(key);

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> recentlyBroken.remove(key),
                2L
        );
    }

    private String getBlockKey(Location location) {

        return location.getWorld().getUID() +
                ":" + location.getBlockX() +
                ":" + location.getBlockY() +
                ":" + location.getBlockZ();
    }
}