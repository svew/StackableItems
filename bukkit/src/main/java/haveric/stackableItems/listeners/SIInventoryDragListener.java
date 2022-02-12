package haveric.stackableItems.listeners;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIInventoryDragListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryDrag(InventoryDragEvent event) {
        ItemStack cursor = event.getOldCursor();
        ItemStack newCursor = event.getCursor();

        Player player = (Player) event.getWhoClicked();

        int cursorAmount = 0;
        if (newCursor != null) {
            cursorAmount = newCursor.getAmount();
        }

        Material cursorType = cursor.getType();
        int defaultStackAmount = cursorType.getMaxStackSize();
        int cursorDur = ItemUtil.getDurability(cursor);

        InventoryView view = event.getView();
        Inventory inventory = event.getInventory();

        Map<Integer, ItemStack> items = event.getNewItems();

        int inventorySize = inventory.getSize();

        boolean deny = false;
        int numStacksToSplit = 0;
        int numToSplit = cursorAmount;
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            numStacksToSplit++;
            int slot = entry.getKey();
            ItemStack added = entry.getValue();
            int newAmount = added.getAmount();

            int maxSlot = InventoryUtil.getInventoryMax(player, null, view, inventory, cursorType, cursorDur, slot);

            if (newAmount > maxSlot && maxSlot > SIItems.ITEM_DEFAULT) {
                int extra = newAmount - maxSlot;
                numToSplit += extra;
                numStacksToSplit--;
                deny = true;
            } else if (newAmount >= defaultStackAmount && newAmount < maxSlot) {
                deny = true;

                int oldAmount = 0;
                ItemStack oldStack;
                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                        actualPlayerSlot -= 36;
                    } else if (actualPlayerSlot == 45) { // Handle shield
                        actualPlayerSlot = 40;
                    }
                    oldStack = player.getInventory().getItem(actualPlayerSlot);
                } else {
                    oldStack = inventory.getItem(slot);
                }
                if (oldStack != null) {
                    oldAmount = oldStack.getAmount();
                }
                numToSplit += newAmount - oldAmount;
            } else if (newAmount < defaultStackAmount && defaultStackAmount < maxSlot) {
                numToSplit += newAmount;
            }
        }

        if (deny) {
            event.setResult(Result.DENY);

            int toAdd = 0;
            if (numStacksToSplit > 0) {
                toAdd = numToSplit / numStacksToSplit;
            }
            int left = numToSplit - (toAdd * numStacksToSplit);

            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                int slot = entry.getKey();
                ItemStack added = entry.getValue();
                int newAmount = added.getAmount();

                int maxSlot = InventoryUtil.getInventoryMax(player, null, view, inventory, cursorType, cursorDur, slot);
                if (maxSlot <= SIItems.ITEM_DEFAULT) {
                    maxSlot = added.getMaxStackSize();
                }

                ItemStack clone = cursor.clone();


                int cloneAmount = 0;
                if (defaultStackAmount >= maxSlot) {
                    if (newAmount > maxSlot) {
                        cloneAmount = maxSlot;
                    } else if (newAmount <= maxSlot) {
                        newAmount += toAdd;
                        if (newAmount > maxSlot) {
                            left += newAmount - maxSlot;
                            newAmount = maxSlot;
                        }
                        cloneAmount = newAmount;
                    }
                } else {
                    int oldAmount = 0;
                    ItemStack oldStack;
                    if (slot >= inventorySize) {
                        int rawPlayerSlot = slot - inventorySize;
                        if (inventory.getType() == InventoryType.CRAFTING) {
                            rawPlayerSlot -= 4; // Handle armor slots
                        }
                        int actualPlayerSlot = rawPlayerSlot + 9;
                        // Offset for hotbar
                        if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                            actualPlayerSlot -= 36;
                        } else if (actualPlayerSlot == 45) { // Handle shield
                            actualPlayerSlot = 40;
                        }
                        oldStack = player.getInventory().getItem(actualPlayerSlot);
                    } else {
                        oldStack = inventory.getItem(slot);
                    }
                    if (oldStack != null) {
                        oldAmount = oldStack.getAmount();
                    }

                    cloneAmount = oldAmount + toAdd;
                    if (cloneAmount > maxSlot) {
                        left += cloneAmount - maxSlot;
                        cloneAmount = maxSlot;
                    }
                }

                clone.setAmount(cloneAmount);

                if (slot >= inventorySize) {
                    int rawPlayerSlot = slot - inventorySize;
                    if (inventory.getType() == InventoryType.CRAFTING) {
                        rawPlayerSlot -= 4; // Handle armor slots
                    }
                    int actualPlayerSlot = rawPlayerSlot + 9;
                    // Offset for hotbar
                    if (actualPlayerSlot >= 36 && actualPlayerSlot <= 44) {
                        actualPlayerSlot -= 36;
                    } else if (actualPlayerSlot == 45) { // Handle shield
                        actualPlayerSlot = 40;
                    }
                    InventoryUtil.replaceItem(player.getInventory(), actualPlayerSlot, clone);
                } else {
                    InventoryUtil.replaceItem(inventory, slot, clone);
                }
            }

            ItemStack cursorClone = cursor.clone();
            cursorClone.setAmount(left);
            InventoryUtil.updateCursor(player, cursorClone);
            InventoryUtil.updateInventory(player);
        }
    }
}
