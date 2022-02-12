package haveric.stackableItems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SICraftItemListener extends SIListenerBase {
    
    public SICraftItemListener(StackableItems si) {
        super(si);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void craftItem(CraftItemEvent event) {
        ItemStack craftedItem = event.getCurrentItem();

        if (craftedItem != null) {
            Player player = (Player) event.getWhoClicked();
            Material type = craftedItem.getType();
            CraftingInventory inventory = event.getInventory();

            int maxItems = SIItems.getItemMax(player, type, ItemUtil.getDurability(craftedItem), inventory.getType());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite items for the crafted item
            if (maxItems == SIItems.ITEM_INFINITE) {
                // Handle infinite recipe items
                int inventSize = inventory.getSize();
                for (int i = 1; i < inventSize; i++) {
                    ItemStack temp = inventory.getItem(i);
                    if (temp != null) {
                        int maxSlot = SIItems.getItemMax(player, temp.getType(), ItemUtil.getDurability(temp), inventory.getType());

                        if (maxSlot == SIItems.ITEM_INFINITE) {
                            ItemStack clone = temp.clone();
                            InventoryUtil.replaceItem(inventory, i, clone);
                        }
                    }
                }
            } else if (maxItems == 0) {
                player.sendMessage(itemDisabledMessage);
                event.setCancelled(true);
            } else {
                ItemStack cursor = event.getCursor();
                int cursorAmount = cursor.getAmount();
                ItemStack result = event.getRecipe().getResult();
                int recipeAmount = result.getAmount();

                if (event.getClick() == ClickType.NUMBER_KEY) {
                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int hotbarButton = event.getHotbarButton();
                        ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                        int hotbarAmount = 0;
                        if (hotbarItem != null) {
                            hotbarAmount = hotbarItem.getAmount();
                        }
                        int total = hotbarAmount + recipeAmount;

                        event.setResult(Result.DENY);
                        InventoryUtil.removeFromCrafting(player, inventory, 1);
                        if (total <= maxItems) {
                            ItemStack toAdd = result.clone();
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                        } else {
                            ItemStack toAdd = result.clone();
                            toAdd.setAmount(maxItems - hotbarAmount);
                            InventoryUtil.addItems(player, toAdd, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                            ItemStack rest = result.clone();
                            rest.setAmount(total - maxItems);
                            InventoryUtil.addItemsToPlayer(player, rest, "");
                        }
                    }
                } else if (event.isShiftClick()) {
                    int amtCanCraft = InventoryUtil.getCraftingAmount(inventory, event.getRecipe());
                    int actualCraft = amtCanCraft * recipeAmount;

                    if (actualCraft > 0) {
                        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, craftedItem);
                        ItemStack clone = craftedItem.clone();
                        // Avoid crafting when there is nothing being crafted
                        if (clone.getType() != Material.AIR) {
                            // custom repairing
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), null, "craft");
                            if (amtCanCraft == 0 && ItemUtil.isRepairable(type)) {
                                // TODO: handle custom repairing to allow stacking
                                // TODO: don't let people repair two fully repaired items.. that's just stupid
                            } else if (freeSpaces > actualCraft) {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < actualCraft) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, amtCanCraft);
                                    clone.setAmount(actualCraft);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                }
                            } else {
                                // We only want to override if moving more than a vanilla stack will hold
                                if (defaultStack > -1 && defaultStack < freeSpaces) {
                                    event.setCancelled(true);

                                    InventoryUtil.removeFromCrafting(player, inventory, freeSpaces);
                                    clone.setAmount(freeSpaces);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                }
                            }
                        }
                    }
                } else if (event.isLeftClick() || event.isRightClick()) {
                    if (ItemUtil.isSameItem(result, cursor)) {
                        int total = cursorAmount + recipeAmount;

                        if (total > maxItems) {
                            event.setCancelled(true);
                        } else {
                            // Only handle stacks that are above normal stack amounts.
                            if (total > result.getMaxStackSize()) {
                                int numCanHold = maxItems - cursorAmount;

                                int craftTimes = numCanHold / recipeAmount;
                                int canCraft = InventoryUtil.getCraftingAmount(event.getInventory(), event.getRecipe());

                                int actualCraft = Math.min(craftTimes, canCraft);

                                if (actualCraft > 0) {
                                    ItemStack cursorClone = cursor.clone();

                                    // Remove one stack from the crafting grid
                                    InventoryUtil.removeFromCrafting(player, event.getInventory(), 1);

                                    // Add one set of items to the cursor
                                    cursorClone.setAmount(total);
                                    player.setItemOnCursor(cursorClone);
                                    event.setResult(Result.DENY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
