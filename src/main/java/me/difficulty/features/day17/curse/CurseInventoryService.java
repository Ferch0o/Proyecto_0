package me.difficulty.features.day17.curse;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import me.difficulty.core.day.DayManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class CurseInventoryService {
    private final DayManager dayManager;
    private final CurseManager manager;
    private final NamespacedKey markerKey;

    public CurseInventoryService(JavaPlugin plugin, DayManager dayManager, CurseManager manager) {
        this.dayManager = dayManager;
        this.manager = manager;
        this.markerKey = new NamespacedKey(plugin, "blocked_inventory_slot");
    }

    public Set<Integer> blockedSlots(Player player) {
        return CurseSlotPolicy.getBlockedSlots(dayManager.getCurrentDay(), manager.getState(player));
    }

    public boolean isMarker(ItemStack stack) {
        return stack != null && stack.getType() == Material.STRUCTURE_VOID
                && stack.hasItemMeta() && stack.getItemMeta().getPersistentDataContainer()
                .has(markerKey, PersistentDataType.BYTE);
    }

    private ItemStack marker() {
        ItemStack item = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Espacio bloqueado por la Maldición", NamedTextColor.DARK_RED));
        meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public void synchronize(Player player) {
        PlayerInventory inventory = player.getInventory();
        Set<Integer> blocked = blockedSlots(player);
        boolean changed = false;

        // Los marcadores de días anteriores o del jugador curado desaparecen.
        for (int slot = 0; slot < 36; slot++) {
            if (!blocked.contains(slot) && isMarker(inventory.getItem(slot))) {
                inventory.setItem(slot, null);
                changed = true;
            }
        }

        // Primero evacuamos los objetos de todos los slots nuevos.
        for (int slot : blocked) {
            ItemStack previous = inventory.getItem(slot);
            if (isMarker(previous)) continue;
            if (previous != null && !previous.getType().isAir()) {
                inventory.setItem(slot, null);
                relocateOrDrop(player, previous.clone(), blocked);
            }
            inventory.setItem(slot, marker());
            changed = true;
        }

        if (changed) player.updateInventory();
    }

    private void relocateOrDrop(Player player, ItemStack original, Set<Integer> blocked) {
        PlayerInventory inventory = player.getInventory();
        ItemStack remaining = original.clone();

        // Completar stacks similares antes de ocupar ranuras nuevas.
        for (int slot = 0; slot < 36 && remaining.getAmount() > 0; slot++) {
            if (blocked.contains(slot)) continue;
            ItemStack current = inventory.getItem(slot);
            if (current == null || !current.isSimilar(remaining)) continue;
            int max = Math.min(current.getMaxStackSize(), inventory.getMaxStackSize());
            int room = max - current.getAmount();
            if (room <= 0) continue;
            int moved = Math.min(room, remaining.getAmount());
            current.setAmount(current.getAmount() + moved);
            inventory.setItem(slot, current);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        for (int slot = 0; slot < 36 && remaining.getAmount() > 0; slot++) {
            if (blocked.contains(slot)) continue;
            ItemStack current = inventory.getItem(slot);
            if (current != null && !current.getType().isAir()) continue;
            int moved = Math.min(remaining.getAmount(), remaining.getMaxStackSize());
            ItemStack placed = remaining.clone();
            placed.setAmount(moved);
            inventory.setItem(slot, placed);
            remaining.setAmount(remaining.getAmount() - moved);
        }

        if (remaining.getAmount() > 0) {
            Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), remaining);
            dropped.setPickupDelay(40);
        }
    }
}
