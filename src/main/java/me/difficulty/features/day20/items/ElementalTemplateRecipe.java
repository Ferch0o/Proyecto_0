
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItem;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ElementalTemplateRecipe
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final ElementalTemplate template;

    private final Definition original;
    private final Definition copy;

    private final Set<UUID> pending = new HashSet<>();

    /*
     * Cada casilla tiene su propia cantidad.
     */
    static final class Requirement {

        final ItemStack item;
        final CustomItem customItem;
        final int amount;

        Requirement(
                ItemStack item,
                CustomItem customItem,
                int amount
        ) {
            this.item = item;
            this.customItem = customItem;
            this.amount = amount;
        }

        boolean matches(ItemStack candidate) {
            return candidate != null
                    && candidate.isSimilar(item)
                    && (customItem == null
                    || customItem.isItem(candidate));
        }

        ItemStack stack(int crafts) {
            ItemStack result = item.clone();
            result.setAmount(amount * crafts);
            return result;
        }
    }

    /*
     * Datos de cada una de las dos recetas.
     */
    static final class Definition {

        final NamespacedKey key;
        final String[] shape;
        final Requirement[] slots;
        final int outputAmount;

        Definition(
                NamespacedKey key,
                String[] shape,
                Requirement[] slots,
                int outputAmount
        ) {
            this.key = key;
            this.shape = shape;
            this.slots = slots;
            this.outputAmount = outputAmount;
        }
    }

    public ElementalTemplateRecipe(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalRod rod,
            ElementalHead head,
            ElementalTear tear,
            ElementalMeat meat,
            MultiPearls multiPearls,
            ElementalTemplate template
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.template = template;

        /*
         * ========================================
         * PRIMER CRAFTEO
         *
         * Bd Ve Bd
         * Ce Pt Le
         * Bd Cl Bd
         * ========================================
         */
        original = new Definition(
                new NamespacedKey(
                        plugin,
                        "elemental_template_recipe"
                ),
                new String[]{
                        "DRD",
                        "HTL",
                        "DMD"
                },
                new Requirement[]{
                        vanilla(Material.DIAMOND_BLOCK, 64),
                        custom(rod, 64),
                        vanilla(Material.DIAMOND_BLOCK, 64),

                        custom(head, 5),
                        vanilla(
                                Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                                1
                        ),
                        custom(tear, 32),

                        vanilla(Material.DIAMOND_BLOCK, 64),
                        custom(meat, 64),
                        vanilla(Material.DIAMOND_BLOCK, 64)
                },
                1
        );

        /*
         * ========================================
         * SEGUNDO CRAFTEO - DUPLICACION
         *
         * Bd Pe Bd
         * Ln Mp Ln
         * Bd Bd Bd
         *
         * Consume 1 plantilla y produce 2.
         * ========================================
         */
        copy = new Definition(
                new NamespacedKey(
                        plugin,
                        "elemental_template_copy_recipe"
                ),
                new String[]{
                        "DED",
                        "NPN",
                        "DDD"
                },
                new Requirement[]{
                        vanilla(Material.DIAMOND_BLOCK, 1),
                        custom(template, 1),
                        vanilla(Material.DIAMOND_BLOCK, 1),

                        vanilla(Material.NETHERITE_INGOT, 1),
                        custom(multiPearls, 1),
                        vanilla(Material.NETHERITE_INGOT, 1),

                        vanilla(Material.DIAMOND_BLOCK, 1),
                        vanilla(Material.DIAMOND_BLOCK, 1),
                        vanilla(Material.DIAMOND_BLOCK, 1)
                },
                2
        );
    }

    private Requirement vanilla(
            Material material,
            int amount
    ) {
        return new Requirement(
                new ItemStack(material),
                null,
                amount
        );
    }

    private Requirement custom(
            CustomItem item,
            int amount
    ) {
        return new Requirement(
                item.createItem(),
                item,
                amount
        );
    }

    public NamespacedKey getOriginalKey() {
        return original.key;
    }

    public NamespacedKey getCopyKey() {
        return copy.key;
    }

    /*
     * ========================================
     * REGISTRAR AMBAS RECETAS
     * ========================================
     */
    public void register() {
        registerDefinition(original);
        registerDefinition(copy);
    }

    private void registerDefinition(
            Definition definition
    ) {

        ShapedRecipe recipe = new ShapedRecipe(
                definition.key,
                createResult(definition)
        );

        recipe.shape(definition.shape);

        Set<Character> registered = new HashSet<>();

        for (int i = 0; i < 9; i++) {

            char symbol =
                    definition.shape[i / 3].charAt(i % 3);

            if (!registered.add(symbol)) {
                continue;
            }

            recipe.setIngredient(
                    symbol,
                    new RecipeChoice.ExactChoice(
                            definition.slots[i].item.clone()
                    )
            );
        }

        plugin.getServer().addRecipe(recipe);
    }

    /*
     * ========================================
     * IDENTIFICAR RECETA
     * ========================================
     */
    Definition findDefinition(
            NamespacedKey key
    ) {

        if (original.key.equals(key)) {
            return original;
        }

        if (copy.key.equals(key)) {
            return copy;
        }

        return null;
    }

    private Definition findDefinition(
            Recipe recipe
    ) {

        if (!(recipe instanceof Keyed keyed)) {
            return null;
        }

        return findDefinition(keyed.getKey());
    }

    ItemStack createResult(
            Definition definition
    ) {

        ItemStack result = template.createItem();

        result.setAmount(
                definition.outputAmount
        );

        return result;
    }

    /*
     * ========================================
     * LIMITE DE LA MATRIZ
     * ========================================
     *
     * Primera receta: maximo 1.
     * Segunda receta: depende del tamaño
     * de los stacks de sus ingredientes.
     */
    int maxMatrixCrafts(
            Definition definition
    ) {

        int maximum = Integer.MAX_VALUE;

        for (Requirement requirement : definition.slots) {

            maximum = Math.min(
                    maximum,
                    requirement.item.getMaxStackSize()
                            / requirement.amount
            );
        }

        return maximum;
    }

    /*
     * ========================================
     * CONTAR INGREDIENTES DEL INVENTARIO
     * ========================================
     */
    private int count(
            ItemStack[] storage,
            Requirement requirement
    ) {

        int total = 0;

        for (ItemStack item : storage) {

            if (requirement.matches(item)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    /*
     * ========================================
     * CALCULAR FABRICACIONES POSIBLES
     * ========================================
     *
     * Agrupamos casillas equivalentes.
     * Por ejemplo, 4 x 64 bloques de
     * diamante requieren 256 en total.
     */
    int possibleFromInventory(
            ItemStack[] storage,
            Definition definition
    ) {

        int maximum = maxMatrixCrafts(definition);

        for (Requirement requirement : definition.slots) {

            int totalRequired = 0;

            for (Requirement other : definition.slots) {

                if (requirement.item.isSimilar(
                        other.item
                )) {

                    totalRequired += other.amount;
                }
            }

            if (totalRequired <= 0) {
                return 0;
            }

            maximum = Math.min(
                    maximum,
                    count(storage, requirement)
                            / totalRequired
            );
        }

        return Math.max(0, maximum);
    }

    /*
     * ========================================
     * RETIRAR INGREDIENTES
     * ========================================
     *
     * Usar exclusivamente sobre copias
     * del inventario del jugador.
     */
    boolean takeMaterials(
            ItemStack[] storage,
            Definition definition,
            int crafts
    ) {

        for (Requirement requirement : definition.slots) {

            int remaining =
                    requirement.amount * crafts;

            for (int i = 0;
                 i < storage.length && remaining > 0;
                 i++) {

                ItemStack item = storage[i];

                if (!requirement.matches(item)) {
                    continue;
                }

                int removed = Math.min(
                        remaining,
                        item.getAmount()
                );

                int left = item.getAmount() - removed;

                if (left <= 0) {
                    storage[i] = null;
                } else {
                    item.setAmount(left);
                }

                remaining -= removed;
            }

            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    ItemStack[] createMatrix(
            Definition definition,
            int crafts
    ) {

        ItemStack[] matrix = new ItemStack[9];

        for (int i = 0; i < 9; i++) {

            matrix[i] = definition.slots[i]
                    .stack(crafts);
        }

        return matrix;
    }

    /*
     * ========================================
     * VALIDAR MATRIZ Y CANTIDADES
     * ========================================
     */
    private int craftsInMatrix(
            ItemStack[] matrix,
            Definition definition
    ) {

        if (matrix == null || matrix.length != 9) {
            return 0;
        }

        int possible = maxMatrixCrafts(definition);

        for (int i = 0; i < 9; i++) {

            Requirement requirement =
                    definition.slots[i];

            if (!requirement.matches(matrix[i])) {
                return 0;
            }

            possible = Math.min(
                    possible,
                    matrix[i].getAmount()
                            / requirement.amount
            );
        }

        return Math.max(0, possible);
    }

    /*
     * ========================================
     * MOSTRAR RESULTADO
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(
            PrepareItemCraftEvent event
    ) {

        Definition definition =
                findDefinition(event.getRecipe());

        if (definition == null) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || craftsInMatrix(
                event.getInventory().getMatrix(),
                definition
        ) < 1) {

            event.getInventory().setResult(null);
            return;
        }

        event.getInventory().setResult(
                createResult(definition)
        );
    }

    /*
     * ========================================
     * CRAFTEO MANUAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        Definition definition =
                findDefinition(event.getRecipe());

        if (definition == null) {
            return;
        }

        /*
         * Cancelar consumo vanilla.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        ClickType click = event.getClick();

        boolean shift =
                click == ClickType.SHIFT_LEFT
                        || click == ClickType.SHIFT_RIGHT;

        boolean normal =
                click == ClickType.LEFT
                        || click == ClickType.RIGHT;

        if (!normal && !shift) {
            return;
        }

        CraftingInventory crafting =
                event.getInventory();

        if (craftsInMatrix(
                crafting.getMatrix(),
                definition
        ) < 1) {
            return;
        }

        UUID playerId = player.getUniqueId();

        if (!pending.add(playerId)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    try {

                        if (!player.isOnline()
                                || !dayManager.isAtLeast(
                                REQUIRED_DAY
                        )
                                || player.getOpenInventory()
                                .getTopInventory()
                                != crafting) {
                            return;
                        }

                        ItemStack[] matrix =
                                crafting.getMatrix();

                        int possible = craftsInMatrix(
                                matrix,
                                definition
                        );

                        if (possible < 1) {
                            return;
                        }

                        int craftsToMake;

                        ItemStack[] newStorage = null;
                        ItemStack newCursor = null;

                        if (shift) {

                            /*
                             * Fabricar tantas veces como
                             * quepan en el inventario.
                             */
                            ItemStack[] bestStorage =
                                    cloneStorage(
                                            player.getInventory()
                                                    .getStorageContents()
                                    );

                            craftsToMake = 0;

                            for (int i = 0; i < possible; i++) {

                                ItemStack[] attempt =
                                        cloneStorage(bestStorage);

                                if (!insertProduct(
                                        attempt,
                                        createResult(definition)
                                )) {
                                    break;
                                }

                                bestStorage = attempt;
                                craftsToMake++;
                            }

                            if (craftsToMake == 0) {

                                player.sendMessage(
                                        "§cNo tienes espacio "
                                                + "para la Plantilla Elemental."
                                );

                                return;
                            }

                            newStorage = bestStorage;

                        } else {

                            /*
                             * Un clic normal fabrica una
                             * preparación: 1 o 2 plantillas
                             * según la receta elegida.
                             */
                            craftsToMake = 1;

                            ItemStack product =
                                    createResult(definition);

                            ItemStack cursor =
                                    player.getItemOnCursor();

                            if (cursor != null
                                    && !cursor.getType().isAir()) {

                                if (!cursor.isSimilar(product)
                                        || cursor.getAmount()
                                        + product.getAmount()
                                        > cursor.getMaxStackSize()) {
                                    return;
                                }

                                product.setAmount(
                                        cursor.getAmount()
                                                + product.getAmount()
                                );
                            }

                            newCursor = product;
                        }

                        /*
                         * ========================================
                         * DESCONTAR CANTIDADES EXACTAS
                         * ========================================
                         */
                        for (int i = 0; i < 9; i++) {

                            int required =
                                    definition.slots[i].amount
                                            * craftsToMake;

                            ItemStack item = matrix[i].clone();

                            int left =
                                    item.getAmount() - required;

                            if (left <= 0) {
                                matrix[i] = null;
                            } else {
                                item.setAmount(left);
                                matrix[i] = item;
                            }
                        }

                        crafting.setMatrix(matrix);

                        /*
                         * ========================================
                         * ENTREGAR RESULTADO
                         * ========================================
                         */
                        if (shift) {

                            player.getInventory()
                                    .setStorageContents(newStorage);

                        } else {

                            player.setItemOnCursor(newCursor);
                        }

                        crafting.setResult(
                                craftsInMatrix(
                                        matrix,
                                        definition
                                ) >= 1
                                        ? createResult(definition)
                                        : null
                        );

                        player.updateInventory();

                    } finally {

                        pending.remove(playerId);
                    }
                }
        );
    }

    /*
     * ========================================
     * COPIAR INVENTARIO
     * ========================================
     */
    private ItemStack[] cloneStorage(
            ItemStack[] storage
    ) {

        ItemStack[] copy =
                new ItemStack[storage.length];

        for (int i = 0; i < storage.length; i++) {

            copy[i] = storage[i] == null
                    ? null
                    : storage[i].clone();
        }

        return copy;
    }

    /*
     * ========================================
     * INSERTAR RESULTADO EN UNA COPIA
     * ========================================
     */
    private boolean insertProduct(
            ItemStack[] storage,
            ItemStack product
    ) {

        int remaining = product.getAmount();

        for (ItemStack item : storage) {

            if (item == null
                    || !item.isSimilar(product)) {
                continue;
            }

            int available =
                    item.getMaxStackSize()
                            - item.getAmount();

            int inserted = Math.min(
                    available,
                    remaining
            );

            if (inserted > 0) {

                item.setAmount(
                        item.getAmount() + inserted
                );

                remaining -= inserted;
            }

            if (remaining == 0) {
                return true;
            }
        }

        for (int i = 0;
             i < storage.length && remaining > 0;
             i++) {

            if (storage[i] != null
                    && !storage[i].getType().isAir()) {
                continue;
            }

            int inserted = Math.min(
                    remaining,
                    product.getMaxStackSize()
            );

            ItemStack stack = product.clone();
            stack.setAmount(inserted);

            storage[i] = stack;

            remaining -= inserted;
        }

        return remaining == 0;
    }

    /*
     * ========================================
     * CRAFTER AUTOMATICO
     * ========================================
     *
     * No permitir fabricación automática
     * de ninguna de las dos recetas.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafter(
            CrafterCraftEvent event
    ) {

        if (findDefinition(event.getRecipe()) != null) {
            event.setCancelled(true);
        }
    }
}