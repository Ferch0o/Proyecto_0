
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

    private record Reward(
            String id,
            int min,
            int max,
            double chance,
            boolean basic,
            Supplier<ItemStack> factory
    ) {}

    private final CustomItemRegistry items;

    private final Map<NetherLootType, List<Reward>> tables =
            new EnumMap<>(NetherLootType.class);

    public NetherLootGenerator(CustomItemRegistry items) {
        this.items = items;
        createTables();
    }

    /*
     * ========================================
     * GENERAR RECOMPENSAS
     * ========================================
     */
    public List<ItemStack> generate(
            NetherLootType type,
            Random random
    ) {

        List<Reward> table = tables.get(type);
        if (table == null) {
            throw new IllegalArgumentException(
                    "Tabla desconocida: " + type
            );
        }

        List<Reward> selected = new ArrayList<>();
        Set<String> selectedIds = new HashSet<>();

        // Una comprobacion por cada recompensa.
        for (Reward reward : table) {

            if (random.nextDouble() * 100 < reward.chance()) {
                selected.add(reward);
                selectedIds.add(reward.id());
            }
        }

        /*
         * ========================================
         * GARANTIZAR LOOT MINIMO
         * ========================================
         *
         * Solo se agregan recursos basicos.
         * Nunca se fuerzan objetos raros
         * o personalizados.
         */
        if (selected.size() < type.getMinimumRewards()) {

            List<Reward> candidates = new ArrayList<>();

            for (Reward reward : table) {

                if (reward.basic() &&
                        !selectedIds.contains(reward.id())) {
                    candidates.add(reward);
                }
            }

            Collections.shuffle(candidates, random);

            for (Reward reward : candidates) {

                if (selected.size() >=
                        type.getMinimumRewards()) {
                    break;
                }

                selected.add(reward);
                selectedIds.add(reward.id());
            }
        }

        if (selected.size() < type.getMinimumRewards()) {
            throw new IllegalStateException(
                    "No se pudo cumplir el loot minimo de " + type
            );
        }

        /*
         * ========================================
         * CONSTRUIR ITEMSTACKS
         * ========================================
         */
        List<ItemStack> result = new ArrayList<>();

        for (Reward reward : selected) {

            ItemStack item = reward.factory().get();

            if (item == null || item.getType().isAir()) {
                throw new IllegalStateException(
                        "Item invalido: " + reward.id()
                );
            }

            if (reward.id().startsWith("tool:")) {
                enchantTool(item, random);
            }

            int amount = reward.min() +
                    random.nextInt(
                            reward.max() - reward.min() + 1
                    );

            // Dividir cantidades mayores al stack maximo.
            while (amount > 0) {

                ItemStack stack = item.clone();

                int count = Math.min(
                        amount,
                        Math.max(1, item.getMaxStackSize())
                );

                stack.setAmount(count);
                result.add(stack);

                amount -= count;
            }
        }

        if (result.size() > 27) {
            throw new IllegalStateException(
                    "El loot supera 27 espacios: " + type
            );
        }

        return result;
    }

    /*
     * ========================================
     * TIPOS DE RECOMPENSAS
     * ========================================
     */
    private void vanilla(
            List<Reward> table,
            Material material,
            int min,
            int max,
            double chance,
            boolean basic
    ) {

        table.add(new Reward(
                "vanilla:" + material.name(),
                min,
                max,
                chance,
                basic,
                () -> new ItemStack(material)
        ));
    }

    private void custom(
            List<Reward> table,
            String id,
            int min,
            int max,
            double chance
    ) {

        table.add(new Reward(
                "custom:" + id,
                min,
                max,
                chance,
                false,
                () -> items.get(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No registrado: " + id
                                ))
                        .createItem()
        ));
    }

    private void tool(
            List<Reward> table,
            Material material,
            double chance
    ) {

        table.add(new Reward(
                "tool:" + material.name(),
                1,
                1,
                chance,
                false,
                () -> new ItemStack(material)
        ));
    }

    private void soulSpeed(
            List<Reward> table,
            double chance
    ) {

        table.add(new Reward(
                "book:soul_speed_iii",
                1,
                1,
                chance,
                false,
                () -> {
                    ItemStack book =
                            new ItemStack(Material.ENCHANTED_BOOK);

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
        ));
    }

    /*
     * ========================================
     * HERRAMIENTAS DE NETHERITE
     * ========================================
     *
     * Encantamientos vanilla compatibles.
     * Sus niveles maximos son posibles,
     * pero no estan garantizados.
     *
     * Durabilidad aleatoria.
     */
    private void enchantTool(ItemStack item, Random random) {

        if (random.nextDouble() < 0.80) {
            item.addUnsafeEnchantment(
                    Enchantment.EFFICIENCY,
                    1 + random.nextInt(5)
            );
        }

        if (random.nextDouble() < 0.65) {
            item.addUnsafeEnchantment(
                    Enchantment.UNBREAKING,
                    1 + random.nextInt(3)
            );
        }

        if (random.nextDouble() < 0.40) {

            if (random.nextBoolean()) {
                item.addUnsafeEnchantment(
                        Enchantment.FORTUNE,
                        1 + random.nextInt(3)
                );
            } else {
                item.addUnsafeEnchantment(
                        Enchantment.SILK_TOUCH,
                        1
                );
            }
        }

        // Nunca entregar una herramienta sin encantamientos.
        if (item.getEnchantments().isEmpty()) {
            item.addUnsafeEnchantment(
                    Enchantment.EFFICIENCY,
                    1 + random.nextInt(5)
            );
        }

        ItemMeta meta = item.getItemMeta();

        if (meta instanceof Damageable damageable) {

            int maximum = item.getType().getMaxDurability();

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
     * TABLAS DE LOOT
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
     * 1. FORTALEZA
     * MINIMO: 2 RECOMPENSAS
     * ========================================
     */
    private void createFortress() {

        List<Reward> t = new ArrayList<>();

        vanilla(t, Material.DIAMOND, 3, 12, 28, false);
        vanilla(t, Material.DIAMOND_BLOCK, 1, 2, 5, false);
        vanilla(t, Material.NETHERITE_INGOT, 1, 2, 1, false);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 3, 5, false);
        vanilla(t, Material.GOLDEN_CARROT, 6, 16, 12, true);
        vanilla(t, Material.GOLDEN_APPLE, 2, 6, 8, false);
        vanilla(t, Material.IRON_BLOCK, 2, 8, 16, true);
        vanilla(t, Material.GOLD_BLOCK, 2, 8, 14, false);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1, false);
        vanilla(t, Material.OBSIDIAN, 3, 6, 20, true);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 5, 6, false);
        vanilla(t, Material.NETHER_WART, 6, 20, 35, true);
        vanilla(t, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 7, false);

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
     * 2. BASTION GENERAL
     * MINIMO: 3 RECOMPENSAS
     * ========================================
     */
    private void createBastionGeneral() {

        List<Reward> t = new ArrayList<>();

        vanilla(t, Material.DIAMOND, 6, 24, 35, false);
        vanilla(t, Material.DIAMOND_BLOCK, 1, 4, 10, false);
        vanilla(t, Material.NETHERITE_INGOT, 1, 2, 4, false);
        vanilla(t, Material.NETHERITE_SCRAP, 1, 5, 15, false);
        vanilla(t, Material.GOLDEN_CARROT, 12, 32, 25, true);
        vanilla(t, Material.GOLDEN_APPLE, 4, 18, 22, false);
        vanilla(t, Material.IRON_BLOCK, 5, 20, 30, true);
        vanilla(t, Material.GOLD_BLOCK, 6, 20, 30, false);
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 1, 12, false);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 2, false);
        vanilla(t, Material.OBSIDIAN, 3, 7, 24, true);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 7, 20, true);
        vanilla(t, Material.MUSIC_DISC_PIGSTEP, 1, 1, 6, false);
        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 8, false);

        soulSpeed(t, 5);

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
     * 3. BASTION PUENTE
     * MINIMO: 3 RECOMPENSAS
     * ========================================
     */
    private void createBastionBridge() {

        List<Reward> t = new ArrayList<>();

        vanilla(t, Material.DIAMOND, 8, 32, 38, false);
        vanilla(t, Material.DIAMOND_BLOCK, 2, 5, 12, false);
        vanilla(t, Material.NETHERITE_INGOT, 1, 2, 5, false);
        vanilla(t, Material.NETHERITE_SCRAP, 2, 6, 18, false);
        vanilla(t, Material.GOLDEN_CARROT, 12, 40, 28, true);
        vanilla(t, Material.GOLDEN_APPLE, 5, 25, 25, false);
        vanilla(t, Material.IRON_BLOCK, 7, 25, 34, true);
        vanilla(t, Material.GOLD_BLOCK, 10, 30, 40, true);
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 1, 14, false);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 2, false);
        vanilla(t, Material.OBSIDIAN, 3, 8, 20, false);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 8, 28, true);
        vanilla(t, Material.LODESTONE, 1, 1, 100, false);
        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 8, false);

        soulSpeed(t, 8);

        custom(t, ItemIds.ELEMENTAL_MEAT, 4, 10, 28);
        custom(t, ItemIds.ELEMENTAL_GOLD, 1, 3, 12);
        custom(t, ItemIds.EXPLOSIVE_CROSSBOW, 1, 1, 2);

        tables.put(
                NetherLootType.BASTION_BRIDGE,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 4. BASTION ESTABLOS
     * MINIMO: 4 RECOMPENSAS
     * ========================================
     */
    private void createBastionStable() {

        List<Reward> t = new ArrayList<>();

        vanilla(t, Material.DIAMOND, 10, 50, 45, false);
        vanilla(t, Material.DIAMOND_BLOCK, 2, 10, 16, false);
        vanilla(t, Material.NETHERITE_INGOT, 1, 3, 10, false);
        vanilla(t, Material.NETHERITE_SCRAP, 3, 10, 28, false);
        vanilla(t, Material.GOLDEN_CARROT, 16, 64, 40, true);
        vanilla(t, Material.GOLDEN_APPLE, 8, 40, 35, false);
        vanilla(t, Material.IRON_BLOCK, 10, 40, 42, true);
        vanilla(t, Material.GOLD_BLOCK, 12, 45, 48, true);
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 2, 18, false);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 2, 5, false);
        vanilla(t, Material.OBSIDIAN, 3, 9, 24, false);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 9, 32, true);
        vanilla(t, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 8, false);

        tool(t, Material.NETHERITE_PICKAXE, 15);
        tool(t, Material.NETHERITE_SHOVEL, 12);

        soulSpeed(t, 12);

        custom(t, ItemIds.ELEMENTAL_MEAT, 4, 10, 35);

        tables.put(
                NetherLootType.BASTION_STABLE,
                List.copyOf(t)
        );
    }

    /*
     * ========================================
     * 5. SALA DEL TESORO
     * MINIMO: 5 RECOMPENSAS
     * ========================================
     */
    private void createBastionTreasure() {

        List<Reward> t = new ArrayList<>();

        vanilla(t, Material.DIAMOND, 10, 150, 75, true);
        vanilla(t, Material.DIAMOND_BLOCK, 3, 32, 50, false);
        vanilla(t, Material.NETHERITE_INGOT, 1, 6, 45, false);
        vanilla(t, Material.NETHERITE_SCRAP, 3, 24, 62, false);
        vanilla(t, Material.NETHERITE_BLOCK, 1, 1, 2, false);
        vanilla(t, Material.GOLDEN_CARROT, 20, 192, 70, true);
        vanilla(t, Material.GOLDEN_APPLE, 15, 170, 65, true);
        vanilla(t, Material.IRON_BLOCK, 20, 125, 70, true);
        vanilla(t, Material.GOLD_BLOCK, 20, 125, 75, true);
        vanilla(t, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 3, 50, false);
        vanilla(t, Material.ENCHANTED_GOLDEN_APPLE, 1, 5, 12, false);
        vanilla(t, Material.OBSIDIAN, 3, 10, 30, false);
        vanilla(t, Material.CRYING_OBSIDIAN, 3, 10, 45, false);

        soulSpeed(t, 18);

        custom(t, ItemIds.ELEMENTAL_INGOT, 1, 1, 5);
        custom(t, ItemIds.ELEMENTAL_TEMPLATE, 1, 1, 0.5);

        tables.put(
                NetherLootType.BASTION_TREASURE,
                List.copyOf(t)
        );
    }
}