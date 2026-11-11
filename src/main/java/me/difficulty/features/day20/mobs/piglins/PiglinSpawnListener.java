
package me.difficulty.features.day20.mobs.piglins;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public final class PiglinSpawnListener implements Listener {

    /*
     * ========================================
     * CONFIGURACION
     * ========================================
     */

    private static final int CORRUPTED_CHANCE = 50;
    private static final int EXPLOSIVE_CHANCE = 40;

    // El 10% restante corresponde al Rey.

    private static final int CHUNKS_PER_TICK = 3;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final CorruptedPiglin corrupted;
    private final ExplosivePiglin explosive;
    private final KingPiglin king;

    /*
     * Permite mantener vanilla los Piglins
     * creados manualmente mediante comandos,
     * huevos o por otros plugins.
     */
    private final NamespacedKey vanillaExemptKey;

    /*
     * Evitar programar varias conversiones
     * del mismo Piglin en Rey.
     */
    private final Set<UUID> pendingKings =
            new HashSet<>();

    /*
     * Cola para revisar chunks cargados
     * sin procesarlos todos en un tick.
     */
    private final Queue<Chunk> pendingChunks =
            new ArrayDeque<>();

    private final Set<String> queuedChunks =
            new HashSet<>();

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     *
     * Conserva los mismos parametros
     * utilizados por Day20Bootstrap.
     */
    public PiglinSpawnListener(
            DayManager dayManager,
            CustomMobRegistry registry,
            CorruptedPiglin corrupted,
            ExplosivePiglin explosive,
            KingPiglin king
    ) {

        this.plugin = JavaPlugin.getProvidingPlugin(
                PiglinSpawnListener.class
        );

        this.dayManager = dayManager;
        this.registry = registry;

        this.corrupted = corrupted;
        this.explosive = explosive;
        this.king = king;

        this.vanillaExemptKey = new NamespacedKey(
                plugin,
                "piglin_vanilla_exempt"
        );

        /*
         * Cuando se alcanza el dia 20,
         * revisar los Piglins que ya
         * estaban cargados.
         */
        dayManager.addDayChangeListener(
                this::onDayChange
        );

        /*
         * Procesar chunks gradualmente.
         */
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::processPendingChunks,
                1L,
                1L
        );

        /*
         * Si el servidor inicia estando
         * en dia 20, revisar los chunks
         * que ya se encuentren cargados.
         */
        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

                    if (dayManager.isAtLeast(20)) {
                        enqueueLoadedChunks();
                    }
                }
        );
    }

    /*
     * ========================================
     * CONTROL DE APARICIONES MANUALES
     * ========================================
     *
     * No alterar entidades creadas mediante
     * comandos, huevos o plugins.
     *
     * Las apariciones NATURAL y SPAWNER
     * seran procesadas normalmente.
     *
     * Los Piglins generados en Bastiones
     * se detectan al incorporarse al mundo.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpawn(CreatureSpawnEvent event) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        Entity entity = event.getEntity();

        if (!isNether(entity.getWorld())) {
            return;
        }

        if (!(entity instanceof Piglin)
                && !(entity instanceof PiglinBrute)) {
            return;
        }

        LivingEntity living = (LivingEntity) entity;

        /*
         * Los mobs personalizados que ya
         * tienen ID nunca se modifican.
         */
        if (registry.getMobId(living).isPresent()) {
            return;
        }

        CreatureSpawnEvent.SpawnReason reason =
                event.getSpawnReason();

        boolean manual = switch (reason) {

            case COMMAND,
                 CUSTOM,
                 EGG,
                 SPAWNER_EGG,
                 DISPENSE_EGG -> true;

            default -> false;
        };

        if (manual) {

            living.getPersistentDataContainer().set(
                    vanillaExemptKey,
                    PersistentDataType.BYTE,
                    (byte) 1
            );
        }
    }

    /*
     * ========================================
     * ENTIDAD INCORPORADA AL MUNDO
     * ========================================
     *
     * Este evento de Paper permite detectar:
     *
     * - Apariciones naturales
     * - Entidades de Bastiones nuevos
     * - Entidades cargadas desde chunks
     * - Entidades provenientes de spawners
     *
     * No depende de CHUNK_GEN.
     */
    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onEntityAdded(
            EntityAddToWorldEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        convertIfNeeded(event.getEntity());
    }

    /*
     * ========================================
     * CARGA DE ENTIDADES EXISTENTES
     * ========================================
     *
     * Respaldo para las entidades cargadas
     * desde archivos del mundo.
     */
    @EventHandler
    public void onEntitiesLoad(
            EntitiesLoadEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        Chunk chunk = event.getChunk();

        if (!isNether(chunk.getWorld())) {
            return;
        }

        scheduleChunk(chunk);
    }

    /*
     * ========================================
     * CARGA DE CHUNKS
     * ========================================
     *
     * También revisamos chunks nuevos.
     *
     * Es importante para los Piglins
     * incluidos en estructuras de Bastión.
     */
    @EventHandler
    public void onChunkLoad(
            ChunkLoadEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!isNether(event.getWorld())) {
            return;
        }

        scheduleChunk(event.getChunk());
    }

    /*
     * ========================================
     * CAMBIO AL DIA 20
     * ========================================
     */
    private void onDayChange(
            int previous,
            int current
    ) {

        if (previous < 20 && current >= 20) {

            enqueueLoadedChunks();

            plugin.getLogger().info(
                    "Dia 20: revisando Piglins " +
                            "de los chunks cargados."
            );
        }
    }

    /*
     * ========================================
     * PROGRAMAR REVISION DE CHUNK
     * ========================================
     */
    private void scheduleChunk(Chunk chunk) {

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

    /*
     * ========================================
     * PROCESAR COLA
     * ========================================
     */
    private void processPendingChunks() {

        if (!dayManager.isAtLeast(20)) {

            pendingChunks.clear();
            queuedChunks.clear();

            return;
        }

        for (int i = 0;
             i < CHUNKS_PER_TICK
                     && !pendingChunks.isEmpty();
             i++) {

            Chunk chunk = pendingChunks.poll();

            queuedChunks.remove(
                    getChunkKey(chunk)
            );

            if (!chunk.isLoaded()) {
                continue;
            }

            /*
             * Solo entidades que ya
             * pertenecen a este chunk.
             *
             * No se buscan ubicaciones
             * lejanas ni se fuerzan cargas.
             */
            for (Entity entity : chunk.getEntities()) {

                convertIfNeeded(entity);
            }
        }
    }

    /*
     * ========================================
     * CONVERTIR ENTIDAD SI ES NECESARIO
     * ========================================
     */
    private void convertIfNeeded(Entity entity) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!isNether(entity.getWorld())) {
            return;
        }

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        /*
         * Solo Piglins y Piglins Brutos.
         */
        if (!(living instanceof Piglin)
                && !(living instanceof PiglinBrute)) {
            return;
        }

        /*
         * Nunca volver a aplicar una
         * configuracion personalizada.
         */
        if (registry.getMobId(living).isPresent()) {
            return;
        }

        /*
         * Respetar entidades manuales
         * que fueron marcadas anteriormente.
         */
        if (living.getPersistentDataContainer().has(
                vanillaExemptKey,
                PersistentDataType.BYTE
        )) {

            return;
        }

        /*
         * ========================================
         * PIGLIN BRUTO: 100% REY
         * ========================================
         */
        if (living instanceof PiglinBrute brute) {

            king.apply(brute);
            return;
        }

        /*
         * ========================================
         * PIGLIN NORMAL
         * ========================================
         */
        Piglin piglin = (Piglin) living;

        /*
         * Todos los Piglins personalizados
         * seran adultos.
         */
        piglin.setAdult();

        if (pendingKings.contains(
                piglin.getUniqueId()
        )) {

            return;
        }

        /*
         * Distribucion:
         *
         * 0-49  = Corrupto  (50%)
         * 50-89 = Explosivo (40%)
         * 90-99 = Rey      (10%)
         */
        int roll = ThreadLocalRandom
                .current()
                .nextInt(100);

        if (roll < CORRUPTED_CHANCE) {

            corrupted.apply(piglin);
            return;
        }

        if (roll <
                CORRUPTED_CHANCE + EXPLOSIVE_CHANCE) {

            explosive.apply(piglin);
            return;
        }

        /*
         * Para el Rey necesitamos una
         * entidad PiglinBrute real.
         *
         * La sustitucion se realiza en
         * el siguiente tick para no
         * eliminar la entidad mientras
         * esta siendo incorporada al mundo.
         */
        scheduleKingConversion(piglin);
    }

    /*
     * ========================================
     * CONVERTIR PIGLIN NORMAL EN REY
     * ========================================
     */
    private void scheduleKingConversion(
            Piglin piglin
    ) {

        UUID uuid = piglin.getUniqueId();

        if (!pendingKings.add(uuid)) {
            return;
        }

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

                    try {

                        /*
                         * Si el dia cambio o
                         * el Piglin desaparecio,
                         * cancelar la conversion.
                         */
                        if (!dayManager.isAtLeast(20)) {
                            return;
                        }

                        if (!piglin.isValid()
                                || piglin.isDead()) {
                            return;
                        }

                        if (!isNether(piglin.getWorld())) {
                            return;
                        }

                        if (registry.getMobId(
                                piglin
                        ).isPresent()) {

                            return;
                        }

                        if (piglin.getPersistentDataContainer()
                                .has(
                                        vanillaExemptKey,
                                        PersistentDataType.BYTE
                                )) {

                            return;
                        }

                        Location location =
                                piglin.getLocation().clone();

                        /*
                         * Primero generar el Rey.
                         *
                         * Su ID personalizado
                         * se asigna antes de
                         * incorporarse al mundo.
                         */
                        location.getWorld().spawn(
                                location,
                                PiglinBrute.class,
                                CreatureSpawnEvent
                                        .SpawnReason.CUSTOM,
                                king::apply
                        );

                        /*
                         * Eliminar el Piglin
                         * original solamente
                         * despues de crear
                         * correctamente al Rey.
                         */
                        piglin.remove();

                    } catch (Exception exception) {

                        plugin.getLogger().log(
                                Level.SEVERE,
                                "Error convirtiendo " +
                                        "Piglin en Rey.",
                                exception
                        );

                    } finally {

                        pendingKings.remove(uuid);
                    }
                }
        );
    }

    /*
     * ========================================
     * COMPROBAR DIMENSION
     * ========================================
     */
    private boolean isNether(World world) {

        return world != null
                && world.getEnvironment()
                == World.Environment.NETHER;
    }
}