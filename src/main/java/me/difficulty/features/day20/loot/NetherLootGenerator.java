
package me.difficulty.features.day20.loot;

import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.items.ItemIds;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Supplier;

public final class NetherLootGenerator {

    /*
     * ========================================
     * CONFIGURACION
     * ========================================
     */

    private static final int MAX_CHEST_SLOTS = 27;

    private static final double ENCHANTED_TOOL_CHANCE = 0.95;

    /*
     * Cada objeto tiene una probabilidad
     * independiente.
     *
     * No existen pools ni tiradas adicionales.
     */
    private record Reward(
            String id,
            int min,
            int max,
            double chance,
            Supplier<ItemStack> factory
    ) {}

    private final CustomItemRegistry items;

    private final Map<NetherLootType, List<Reward>> tables =
            new EnumMap<>(NetherLootType.class);

    public NetherLootGenerator(CustomItemRegistry items) {

        this.items = Objects.requireNonNull(items);

        createTables();
    }

    /*
     * ========================================
     * GENERACION PRINCIPAL
     * ========================================
     */
    public List<ItemStack> generate(
            NetherLootType type,
            Random random
    ) {

        Objects.requireNonNull(random);

        List<Reward> table = tables.get(type);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Tabla de loot desconocida: " + type
            );
        }

        List<StackPlan> plans = new ArrayList<>();

        /*
         * Cada objeto tiene una sola tirada.
         *
         * 100% = siempre aparece.
         * 50% = una oportunidad del 50%.
         * 0.5% = una oportunidad del 0.5%.
         */
        for (Reward reward : table) {

            if (reward.chance() < 100.0 &&
                    random.nextDouble() * 100.0
                            >= reward.chance()) {

                continue;
            }

            ItemStack item = reward.factory().get();

            if (item == null || item.getType().isAir()) {

                throw new IllegalStateException(
                        "Item invalido: " + reward.id()
                );
            }

            /*
             * Encantamientos y durabilidad
             * de herramientas de netherite.
             */
            if (reward.id().startsWith("tool:")) {

                prepareTool(item, random);
            }

            /*
             * Generamos una cantidad total
             * dentro del rango establecido.
             */
            int amount = reward.min()
                    + random.nextInt(
                    reward.max() - reward.min() + 1
            );

            plans.add(
                    new StackPlan(
                            reward.id(),
                            item,
                            amount
                    )
            );
        }

        /*
         * Distribuir cantidades en stacks,
         * respetando los 27 espacios.
         */
        return distributeStacks(plans, random);
    }

    /*
     * ========================================
     * PLAN DE STACKS
     * ========================================
     */
    private static final class StackPlan {

        private final String id;
        private final ItemStack item;
        private final int amount;
        private final int maximumStackSize;

        private int stacks;

        private StackPlan(
                String id,
                ItemStack item,
                int amount
        ) {

            this.id = id;
            this.item = item;
            this.amount = amount;

            this.maximumStackSize = Math.max(
                    1,
                    item.getMaxStackSize()
            );

            /*
             * Espacios minimos necesarios.
             *
             * Por ejemplo:
             * 150 diamantes necesitan
             * al menos 3 stacks.
             */
            this.stacks =
                    (amount + maximumStackSize - 1)
                            / maximumStackSize;
        }
    }

    /*
     * ========================================
     * DISTRIBUCION ALEATORIA DE STACKS
     * ========================================
     *
     * Ejemplo:
     *
     * 25 diamantes
     *
     * Puede producir:
     * 10 + 5 + 10
     * 7 + 9 + 9
     * 12 + 6 + 7
     *
     * La suma siempre es 25.
     */
    private List<ItemStack> distributeStacks(
            List<StackPlan> plans,
            Random random
    ) {

        int occupiedSlots = 0;

        for (StackPlan plan : plans) {

            occupiedSlots += plan.stacks;
        }

        /*
         * Nunca eliminar silenciosamente
         * un objeto si no cabe.
         */
        if (occupiedSlots > MAX_CHEST_SLOTS) {

            throw new IllegalStateException(
                    "El loot necesita " + occupiedSlots +
                            " espacios y el maximo es " +
                            MAX_CHEST_SLOTS
            );
        }

        int freeSlots =
                MAX_CHEST_SLOTS - occupiedSlots;

        /*
         * Algunos materiales tendran
         * prioridad para dividirse.
         */
        List<StackPlan> ordered =
                new ArrayList<>(plans);

        Collections.shuffle(ordered, random);

        ordered.sort(
                Comparator.comparingInt(
                        this::getSplitPriority
                ).reversed()
        );

        /*
         * ========================================
         * ASIGNAR STACKS ADICIONALES
         * ========================================
         */
        for (StackPlan plan : ordered) {

            if (freeSlots <= 0) {
                break;
            }

            if (!canSplitVisually(plan)) {
                continue;
            }

            int extraWanted;

            if (plan.amount >= 24) {

                extraWanted = 2;

            } else if (plan.amount >= 12) {

                extraWanted = 1;

            } else {

                extraWanted = 0;
            }

            /*
             * No podemos tener mas stacks
             * que unidades del objeto.
             */
            int maximumExtra = Math.max(
                    0,
                    plan.amount - plan.stacks
            );

            int extra = Math.min(
                    freeSlots,
                    Math.min(extraWanted, maximumExtra)
            );

            plan.stacks += extra;

            freeSlots -= extra;
        }

        /*
         * ========================================
         * CONSTRUIR STACKS
         * ========================================
         */
        List<ItemStack> result = new ArrayList<>();

        for (StackPlan plan : plans) {

            int remaining = plan.amount;

            for (int part = 0;
                 part < plan.stacks;
                 part++) {

                int partsLeft = plan.stacks - part;

                /*
                 * Cada stack debe tener al
                 * menos una unidad.
                 *
                 * Ninguno puede superar
                 * su maximo permitido.
                 */
                int lower = Math.max(
                        1,
                        remaining -
                                (partsLeft - 1)
                                        * plan.maximumStackSize
                );

                int upper = Math.min(
                        plan.maximumStackSize,
                        remaining - (partsLeft - 1)
                );

                /*
                 * Reparticion aproximadamente
                 * equilibrada pero aleatoria.
                 */
                double average =
                        (double) remaining / partsLeft;

                double variation =
                        0.60 + random.nextDouble() * 0.80;

                int proposed = (int) Math.round(
                        average * variation
                );

                int count = Math.max(
                        lower,
                        Math.min(upper, proposed)
                );

                ItemStack stack = plan.item.clone();

                stack.setAmount(count);

                result.add(stack);

                remaining -= count;
            }

            /*
             * Confirmar que no se perdieron
             * ni duplicaron unidades.
             */
            if (remaining != 0) {

                throw new IllegalStateException(
                        "Error distribuyendo " + plan.id
                );
            }
        }

        if (result.size() > MAX_CHEST_SLOTS) {

            throw new IllegalStateException(
                    "Demasiados stacks generados: " +
                            result.size()
            );
        }

        /*
         * La clase NetherChestLootManager
         * colocara estos stacks en
         * posiciones aleatorias.
         */
        Collections.shuffle(result, random);

        return result;
    }

    /*
     * ========================================
     * RECURSOS QUE PUEDEN DIVIDIRSE
     * ========================================
     */
    private boolean canSplitVisually(StackPlan plan) {

        /*
         * No dividir adicionalmente
         * herramientas, libros, plantillas
         * ni objetos personalizados.
         */
        if (!plan.id.startsWith("vanilla:")) {
            return false;
        }

        if (plan.amount < 12) {
            return false;
        }

        return switch (plan.item.getType()) {

            case DIAMOND,
                 DIAMOND_BLOCK,
                 IRON_BLOCK,
                 GOLD_BLOCK,
                 GOLDEN_CARROT,
                 GOLDEN_APPLE,
                 ENCHANTED_GOLDEN_APPLE,
                 NETHER_WART -> true;

            default -> false;
        };
    }

    /*
     * ========================================
     * PRIORIDAD DE REPARTICION
     * ========================================
     */
    private int getSplitPriority(StackPlan plan) {

        return switch (plan.item.getType()) {

            case DIAMOND -> 100;

            case DIAMOND_BLOCK -> 95;

            case GOLD_BLOCK -> 90;

            case IRON_BLOCK -> 80;

            case ENCHANTED_GOLDEN_APPLE -> 75;

            case GOLDEN_APPLE -> 70;

            case GOLDEN_CARROT -> 60;

            case NETHER_WART -> 40;

            default -> 0;
        };
    }

    /*
     * ========================================
     * REGISTRAR ITEM VANILLA
     * ========================================
     */
    private void vanilla(
            List<Reward> table,
            Material material,
            int min,
            int max,
            double chance
    ) {

        validateReward(
                material.name(),
                min,
                max,
                chance
        );

        table.add(
                new Reward(
                        "vanilla:" + material.name(),
                        min,
                        max,
                        chance,
                        () -> new ItemStack(material)
                )
        );
    }

    /*
     * ========================================
     * REGISTRAR ITEM PERSONALIZADO
     * ========================================
     */
    private void custom(
            List<Reward> table,
            String id,
            int min,
            int max,
            double chance
    ) {

        validateReward(id, min, max, chance);

        /*
         * Detectar objetos faltantes al
         * iniciar el plugin, no durante
         * una tirada muy poco frecuente.
         */
        if (items.get(id).isEmpty()) {

            throw new IllegalStateException(
                    "Item personalizado no registrado: " + id
            );
        }

        table.add(
                new Reward(
                        "custom:" + id,
                        min,
                        max,
                        chance,
                        () -> items.get(id)
                                .orElseThrow()
                                .createItem()
                )
        );
    }

    /*
     * ========================================
     * REGISTRAR HERRAMIENTA
     * ========================================
     */
    private void tool(
            List<Reward> table,
            Material material,
            double chance
    ) {

        validateReward(
                material.name(),
                1,
                1,
                chance
        );

        table.add(
                new Reward(
                        "tool:" + material.name(),
                        1,
                        1,
                        chance,
                        () -> new ItemStack(material)
                )
        );
    }

    /*
     * ========================================
     * REGISTRAR SOUL SPEED III
     * ========================================
     */
    private void soulSpeed(
            List<Reward> table,
            double chance
    ) {

        validateReward(
                "SOUL_SPEED_III",
                1,
                1,
                chance
        );

        table.add(
                new Reward(
                        "book:soul_speed_iii",
                        1,
                        1,
                        chance,
                        () -> {

                            ItemStack book =
                                    new ItemStack(
                                            Material.ENCHANTED_BOOK
                                    );

                            EnchantmentStorageMeta meta =
                                    (EnchantmentStorageMeta)
                                            book.getItemMeta();

                            meta.addStoredEnchant(
                                    Enchantment.SOUL_SPEED,
                                    3,
                                    true
                            );

                            book.setItemMeta(meta);

                            return book;
                        }
                )
        );
    }

    /*
     * ========================================
     * VALIDAR CANTIDADES Y PROBABILIDADES
     * ========================================
     */
    private void validateReward(
            String id,
            int min,
            int max,
            double chance
    ) {

        if (min < 1 || max < min) {

            throw new IllegalArgumentException(
                    "Cantidad invalida para " + id
            );
        }

        if (!Double.isFinite(chance)
                || chance < 0
                || chance > 100) {

            throw new IllegalArgumentException(
                    "Probabilidad invalida para " + id
            );
        }
    }

    /*
     * ========================================
     * HERRAMIENTAS DE NETHERITE
     * ========================================
     *
     * 95% encantadas.
     * 5% sin encantamientos.
     *
     * Los encantamientos seleccionados
     * siempre usan niveles maximos vanilla.
     *
     * La durabilidad es aleatoria
     * en ambos casos.
     */
    private void prepareTool(
            ItemStack item,
            Random random
    ) {

        boolean enchanted =
                random.nextDouble()
                        < ENCHANTED_TOOL_CHANCE;

        if (enchanted) {

            switch (item.getType()) {

                case NETHERITE_PICKAXE,
                     NETHERITE_SHOVEL ->
                        enchantMiningTool(item, random);

                case NETHERITE_SWORD ->
                        enchantSword(item, random);

                case NETHERITE_AXE ->
                        enchantAxe(item, random);

                default -> throw new IllegalArgumentException(
                        "Herramienta no soportada: " +
                                item.getType()
                );
            }
        }

        /*
         * Incluso una herramienta
         * sin encantamientos puede
         * aparecer bastante dañada.
         */
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof Damageable damageable) {

            int maximum =
                    item.getType().getMaxDurability();

            if (maximum > 1) {

                damageable.setDamage(
                        1 + random.nextInt(maximum - 1)
                );
            }

            item.setItemMeta(meta);
        }
    }

    /*
     * ========================================
     * PICO Y PALA
     * ========================================
     */
    private void enchantMiningTool(
            ItemStack item,
            Random random
    ) {

        /*
         * Encantamiento principal.
         */
        item.addUnsafeEnchantment(
                Enchantment.EFFICIENCY,
                5
        );

        /*
         * Irrompibilidad III.
         */
        if (random.nextDouble() < 0.70) {

            item.addUnsafeEnchantment(
                    Enchantment.UNBREAKING,
                    3
            );
        }

        /*
         * Fortuna III O Toque de Seda I.
         * Nunca los dos.
         */
        if (random.nextDouble() < 0.50) {

            if (random.nextBoolean()) {

                item.addUnsafeEnchantment(
                        Enchantment.FORTUNE,
                        3
                );

            } else {

                item.addUnsafeEnchantment(
                        Enchantment.SILK_TOUCH,
                        1
                );
            }
        }
    }

    /*
     * ========================================
     * ESPADA
     * ========================================
     */
    private void enchantSword(
            ItemStack item,
            Random random
    ) {

        /*
         * Filo V garantizado cuando
         * la espada sale encantada.
         */
        item.addUnsafeEnchantment(
                Enchantment.SHARPNESS,
                5
        );

        if (random.nextDouble() < 0.70) {

            item.addUnsafeEnchantment(
                    Enchantment.UNBREAKING,
                    3
            );
        }

        if (random.nextDouble() < 0.60) {

            item.addUnsafeEnchantment(
                    Enchantment.LOOTING,
                    3
            );
        }

        if (random.nextDouble() < 0.35) {

            item.addUnsafeEnchantment(
                    Enchantment.FIRE_ASPECT,
                    2
            );
        }

        if (random.nextDouble() < 0.40) {

            item.addUnsafeEnchantment(
                    Enchantment.SWEEPING_EDGE,
                    3
            );
        }
    }

    /*
     * ========================================
     * HACHA
     * ========================================
     */
    private void enchantAxe(
            ItemStack item,
            Random random
    ) {

        /*
         * Eficiencia V garantizada
         * cuando el hacha sale encantada.
         */
        item.addUnsafeEnchantment(
                Enchantment.EFFICIENCY,
                5
        );

        /*
         * Filo V es compatible con el
         * hacha mediante yunque.
         */
        if (random.nextDouble() < 0.55) {

            item.addUnsafeEnchantment(
                    Enchantment.SHARPNESS,
                    5
            );
        }

        if (random.nextDouble() < 0.70) {

            item.addUnsafeEnchantment(
                    Enchantment.UNBREAKING,
                    3
            );
        }

        /*
         * Fortuna III o Toque de Seda I.
         */
        if (random.nextDouble() < 0.50) {

            if (random.nextBoolean()) {

                item.addUnsafeEnchantment(
                        Enchantment.FORTUNE,
                        3
                );

            } else {

                item.addUnsafeEnchantment(
                        Enchantment.SILK_TOUCH,
                        1
                );
            }
        }
    }

    /*
     * ========================================
     * CREACION DE LAS CINCO TABLAS
     * ========================================
     */
    private void createTables() {

        createFortress();
        createBastionGeneral();
        createBastionBridge();
        createBastionStable();
        createBastionTreasure();
    }

    /*
     * ========================================
     * 1. FORTALEZA DEL NETHER
     * ========================================
     *
     * Seis recursos garantizados.
     * Sin obsidiana ni obsidiana llorosa.
     */
    private void createFortress() {

        List<Reward> t = new ArrayList<>();

        // Minerales y recursos
        vanilla(t, Material.DIAMOND, 10, 32, 100);
        vanilla(t, Material.DIAMOND_BLOCK, 3, 10, 50);
        vanilla(t, Material.NETHERITE_INGOT, 1, 2, 25);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 8, 50);

        // Supervivencia
        vanilla(t, Material.GOLDEN_CARROT, 10, 64, 100);
        vanilla(t, Material.GOLDEN_APPLE, 5, 30, 100);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 2, 5, 10);

        // Bloques
        vanilla(t, Material.IRON_BLOCK, 10, 50, 100);
        vanilla(t, Material.GOLD_BLOCK, 10, 50, 100);

        // Otros objetos vanilla
        vanilla(t, Material.NETHER_WART, 6, 20, 75);
        vanilla(t, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                1, 1, 7);

        // Objetos personalizados
        custom(t, ItemIds.ELEMENTAL_ROD, 3, 6, 12);
        custom(t, ItemIds.ELEMENTAL_HEAD, 1, 1, 3);
        custom(t, ItemIds.ELEMENTAL_TEAR, 1, 3, 16);
        custom(t, ItemIds.ELEMENTAL_PEARL, 2, 5, 18);
        custom(t, ItemIds.MULTI_PEARLS, 1, 1, 3);
        custom(t, ItemIds.CORRUPTED_STAR, 1, 1, 3);

        tables.put(
                NetherLootType.FORTRESS,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 2. BASTION - COFRES GENERALES
     * ========================================
     *
     * Destacan por hierro y oro.
     * Se conservan ambas obsidianas.
     */
    private void createBastionGeneral() {

        List<Reward> t = new ArrayList<>();

        // Minerales
        vanilla(t, Material.DIAMOND, 10, 32, 100);
        vanilla(t, Material.DIAMOND_BLOCK, 3, 10, 100);
        vanilla(t, Material.NETHERITE_INGOT, 1, 2, 10);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 8, 30);

        // Supervivencia
        vanilla(t, Material.GOLDEN_CARROT, 10, 64, 40);
        vanilla(t, Material.GOLDEN_APPLE, 5, 30, 10);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 2, 5);

        // Bloques
        vanilla(t, Material.IRON_BLOCK, 30, 80, 100);
        vanilla(t, Material.GOLD_BLOCK, 30, 80, 100);

        // Plantillas y recompensas vanilla
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                1, 1, 12);

        vanilla(t, Material.OBSIDIAN, 3, 7, 24);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 7, 20);

        vanilla(t, Material.MUSIC_DISC_PIGSTEP, 1, 1, 6);

        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                1, 1, 8);

        // Soul Speed III: 5% + 25 puntos = 30%
        soulSpeed(t, 30);

        // Objetos personalizados
        custom(t, ItemIds.ELEMENTAL_TEAR, 1, 4, 22);
        custom(t, ItemIds.ELEMENTAL_PEARL, 2, 5, 24);
        custom(t, ItemIds.MULTI_PEARLS, 1, 1, 5);

        tables.put(
                NetherLootType.BASTION_OTHER,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 3. BASTION - PUENTE
     * ========================================
     *
     * Mejores minerales que General.
     * Magnetita garantizada.
     * Sin obsidianas.
     */
    private void createBastionBridge() {

        List<Reward> t = new ArrayList<>();

        // Minerales
        vanilla(t, Material.DIAMOND, 15, 50, 100);
        vanilla(t, Material.DIAMOND_BLOCK, 5, 18, 100);
        vanilla(t, Material.NETHERITE_INGOT, 1, 3, 15);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 10, 35);

        // Supervivencia
        vanilla(t, Material.GOLDEN_CARROT, 20, 75, 50);
        vanilla(t, Material.GOLDEN_APPLE, 10, 60, 20);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 3, 7);

        // Bloques
        vanilla(t, Material.IRON_BLOCK, 30, 80, 100);
        vanilla(t, Material.GOLD_BLOCK, 30, 80, 100);

        // Objetos especiales vanilla
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                1, 1, 14);

        vanilla(t, Material.LODESTONE, 1, 1, 100);

        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                1, 1, 8);

        // Soul Speed III: 8% + 20 puntos = 28%
        soulSpeed(t, 28);

        // Objetos personalizados
        custom(t, ItemIds.ELEMENTAL_MEAT, 10, 20, 28);
        custom(t, ItemIds.ELEMENTAL_GOLD, 1, 3, 12);
        custom(t, ItemIds.EXPLOSIVE_CROSSBOW, 1, 1, 2);

        tables.put(
                NetherLootType.BASTION_BRIDGE,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 4. BASTION - ESTABLOS
     * ========================================
     *
     * Especialidad:
     * alimentos y herramientas.
     *
     * Sin bloque de hierro.
     * Sin plantilla de netherita.
     * Sin obsidianas.
     * Sin Soul Speed.
     */
    private void createBastionStable() {

        List<Reward> t = new ArrayList<>();

        // Minerales secundarios
        vanilla(t, Material.DIAMOND, 1, 20, 40);
        vanilla(t, Material.DIAMOND_BLOCK, 1, 5, 30);
        vanilla(t, Material.NETHERITE_INGOT, 1, 1, 15);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 3, 20);

        // Alimentos abundantes garantizados
        vanilla(t, Material.GOLDEN_CARROT, 60, 140, 100);
        vanilla(t, Material.GOLDEN_APPLE, 35, 110, 100);

        // Recompensas de supervivencia
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE,
                1, 5, 50);

        // Bloque de oro garantizado
        vanilla(t, Material.GOLD_BLOCK, 10, 30, 100);

        // Plantilla de armadura
        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                1, 1, 8);

        /*
         * Herramientas de netherite.
         *
         * Probabilidades independientes:
         * Pico:   15% + 7 puntos = 22%
         * Pala:   12% + 7 puntos = 19%
         * Espada: 22%
         * Hacha:  19%
         */
        tool(t, Material.NETHERITE_PICKAXE, 22);
        tool(t, Material.NETHERITE_SHOVEL, 19);
        tool(t, Material.NETHERITE_SWORD, 22);
        tool(t, Material.NETHERITE_AXE, 19);

        custom(t, ItemIds.ELEMENTAL_MEAT, 24, 110, 100);

        tables.put(
                NetherLootType.BASTION_STABLE,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 5. BASTION - SALA DEL TESORO
     * ========================================
     *
     * Mayor riqueza mineral.
     *
     * Sin hierro.
     * Sin zanahorias.
     * Sin obsidianas.
     * Sin Soul Speed.
     */
    private void createBastionTreasure() {

        List<Reward> t = new ArrayList<>();

        // Diamantes garantizados
        vanilla(t, Material.DIAMOND, 50, 170, 100);
        vanilla(t, Material.DIAMOND_BLOCK, 20, 102, 100);

        // Netherita
        vanilla(t, Material.NETHERITE_INGOT, 1, 6, 50);
        vanilla(t, Material.NETHERITE_SCRAP, 3, 24, 100);

        // Bloque de netherite: vuelve al 2%
        vanilla(t, Material.NETHERITE_BLOCK, 1, 1, 2);

        // Supervivencia
        vanilla(t, Material.GOLDEN_APPLE, 15, 170, 70);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 12, 55);

        // Bloques de oro garantizados
        vanilla(t, Material.GOLD_BLOCK, 20, 250, 100);

        // Plantilla de mejora de netherita
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                1, 3, 50);

        /*
         * Objetos personalizados.
         */
        custom(t, ItemIds.ELEMENTAL_HEAD, 1, 3, 100);
        custom(t, ItemIds.ELEMENTAL_INGOT, 1, 1, 5);
        custom(t, ItemIds.ELEMENTAL_TEMPLATE, 1, 1, 0.5);

        tables.put(
                NetherLootType.BASTION_TREASURE,
                List.copyOf(t)
        );
    }
}