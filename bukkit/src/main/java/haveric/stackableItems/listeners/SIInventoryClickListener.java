package haveric.stackableItems.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.config.Config;
import haveric.stackableItems.config.FurnaceXPConfig;
import haveric.stackableItems.util.FurnaceUtil;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIInventoryClickListener extends SIListenerBase {
    
    public SIInventoryClickListener(StackableItems si) {
        super(si);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        if (plugin.supportsInventoryStackSize) {
            try {
                event.getInventory().setMaxStackSize(SIItems.ITEM_NEW_MAX);
            } catch (AbstractMethodError e) {
                plugin.supportsInventoryStackSize = false;
            }
        }

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();


        SlotType slotType = event.getSlotType();

        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        InventoryType topType = top.getType();

        String topName = event.getView().getTitle();
        // Let Vanilla handle the saddle and armor slots for horses
        boolean isHorseInventory = topName.equalsIgnoreCase("Horse") || topName.equalsIgnoreCase("Donkey") || topName.equalsIgnoreCase("Mule")
                || topName.equalsIgnoreCase("Undead horse") || topName.equalsIgnoreCase("Skeleton horse");
        if (event.getRawSlot() < 2 && topType == InventoryType.CHEST && isHorseInventory) {
            return;
        }

        InventoryAction action = event.getAction();
        // Ignore drop events
        if (action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ONE_CURSOR) {
            return;
        }

        ClickType clickType = event.getClick();

        if (clickType == ClickType.NUMBER_KEY && slotType != SlotType.RESULT) {
            Player player = (Player) event.getWhoClicked();
            int hotbarButton = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

            if (clicked != null) {
                Material clickedType = clicked.getType();
                int clickedDur = ItemUtil.getDurability(clicked);
                int clickedAmount = clicked.getAmount();

                boolean clickedEmpty = clickedType == Material.AIR;

                int hotbarAmount = 0;
                if (hotbarItem != null) {
                    hotbarAmount = hotbarItem.getAmount();
                }

                // Moving clicked to an empty hotbar slot
                if (!clickedEmpty && hotbarItem == null) {
                    int maxItems = InventoryUtil.getInventoryMax(player, null, view, player.getInventory(), clickedType, clickedDur, hotbarButton);

                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                        event.setResult(Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        event.setCurrentItem(null);

                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        InventoryUtil.addItemsToPlayer(player, clone2, "");

                        event.setResult(Result.ALLOW);
                    } // else let vanilla handle it
                // Moving hotbar to an empty clicked slot
                } else if (clickedEmpty && hotbarItem != null) {
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, null, view, top, clickedType, clickedDur, rawSlot);
                    int inventorySize = top.getSize();

                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        event.setCurrentItem(null);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), rawSlot, rawSlot + 1, null, "");
                        } else {
                            InventoryUtil.addItems(player, clicked.clone(), top, rawSlot, rawSlot + 1, null, "");
                        }

                        event.setResult(Result.ALLOW);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(maxItems);

                        if (rawSlot >= inventorySize) {
                            InventoryUtil.addItems(player, clone2, player.getInventory(), rawSlot, rawSlot + 1, null, "");
                        } else {
                            InventoryUtil.addItems(player, clone2, top, rawSlot, rawSlot + 1, null, "");
                        }
                    } // else let vanilla handle it
                // Move clicked to hotbar. Move hotbar elsewhere
                } else if (!clickedEmpty && hotbarItem != null) {
                    int rawSlot = event.getRawSlot();
                    int maxItems = InventoryUtil.getInventoryMax(player, null, view, player.getInventory(), clickedType, clickedDur, hotbarButton);
                    int inventorySize = top.getSize();
                    int totalItems = clickedAmount + hotbarAmount;

                    if (rawSlot < inventorySize) {
                        if (ItemUtil.isSameItem(hotbarItem, clicked)) {
                            if (totalItems <= maxItems && totalItems > clickedType.getMaxStackSize()) {
                                event.setCurrentItem(null);
                                InventoryUtil.addItems(player, clicked.clone(), player.getInventory(), hotbarButton, hotbarButton + 1, null, "");
                                event.setResult(Result.DENY);
                            } else if (totalItems > maxItems) {
                                event.setCurrentItem(null);
                                int extra = totalItems - maxItems;
                                int toAdd = maxItems - hotbarAmount;
                                ItemStack clone = clicked.clone();
                                clone.setAmount(toAdd);
                                InventoryUtil.addItems(player, clone, player.getInventory(), hotbarButton, hotbarButton + 1, null, "");

                                ItemStack clone2 = clicked.clone();
                                clone2.setAmount(extra);
                                InventoryUtil.addItemsToPlayer(player, clone2, "");
                                event.setResult(Result.DENY);
                            } // Else vanilla can handle it.
                        // Different Items
                        } else {
                            event.setCurrentItem(null);

                            ItemStack cloneHotbar = hotbarItem.clone();
                            if (clickedAmount > maxItems) {
                                ItemStack clone = clicked.clone();
                                clone.setAmount(maxItems);
                                InventoryUtil.replaceItem(player.getInventory(), hotbarButton, clone);

                                ItemStack clone2 = clicked.clone();
                                clone2.setAmount(clickedAmount - maxItems);
                                InventoryUtil.addItemsToPlayer(player, clone2, "");
                            } else {
                                ItemStack cloneClicked = clicked.clone();
                                InventoryUtil.replaceItem(player.getInventory(), hotbarButton, cloneClicked);
                            }
                            InventoryUtil.addItemsToPlayer(player, cloneHotbar, "");

                            event.setResult(Result.DENY);
                        }
                    } // Else let vanilla move items between player slots
                }
            }
        } else if (cursor != null && clicked != null && slotType == SlotType.RESULT && top instanceof FurnaceInventory) {
            Material clickedType = clicked.getType();
            boolean clickedEmpty = clickedType == Material.AIR;

            // Only deal with items in the result slot.
            if (!clickedEmpty) {
                Player player = (Player) event.getWhoClicked();
                InventoryHolder inventoryHolder = event.getInventory().getHolder();


                if (inventoryHolder instanceof Furnace) {
                    Furnace furnace = (Furnace) inventoryHolder;
                    int cursorAmount = cursor.getAmount();
                    Material cursorType = cursor.getType();

                    int clickedDur = ItemUtil.getDurability(clicked);
                    int clickedAmount = clicked.getAmount();

                    boolean cursorEmpty = cursorType == Material.AIR;

                    int maxItems = InventoryUtil.getInventoryMax(player, null, view, top, clickedType, clickedDur, event.getRawSlot());

                    if (maxItems == 0) {
                        player.sendMessage(itemDisabledMessage);
                        event.setCancelled(true);
                    } else {
                        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, clicked);

                        ItemStack clone = clicked.clone();
                        ItemStack clone2 = clicked.clone();
                        int xpItems = 0;

                        int maxFurnaceSize = Config.getMaxBlockAmount(furnace, clickedType);
                        if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {
                            int amt = Config.getFurnaceAmount(furnace);
                            if (amt > -1) {
                                int maxPlayerInventory = SIItems.getItemMax(player, clickedType, clickedDur, topType);
                                // Don't touch default items
                                if (maxPlayerInventory == SIItems.ITEM_DEFAULT) {
                                    return;
                                }
                                if (maxPlayerInventory == SIItems.ITEM_INFINITE) {
                                    maxPlayerInventory = clickedType.getMaxStackSize();
                                }
                                if (event.isShiftClick()) {
                                    clone.setAmount(amt);
                                    InventoryUtil.addItemsToPlayer(player, clone, "");
                                    event.setCurrentItem(null);
                                    event.setResult(Result.DENY);
                                    Config.clearFurnace(furnace);
                                    xpItems = amt;
                                } else if (cursorEmpty && event.isRightClick()) {
                                    // Give half of furnace amount to cursor
                                    int cursorHalf = (int) Math.round((amt + 0.5) / 2);
                                    if (cursorHalf > maxPlayerInventory) {
                                        cursorHalf = maxPlayerInventory;
                                    }
                                    int furnaceHalf = amt - cursorHalf;
                                    clone.setAmount(cursorHalf);
                                    player.setItemOnCursor(clone);

                                    clone2.setAmount(furnaceHalf);
                                    event.setCurrentItem(clone2);
                                    Config.clearFurnace(furnace);
                                    xpItems = cursorHalf;
                                } else if (event.isLeftClick() || event.isRightClick()) {
                                    // Any other click will stack on the cursor
                                    if (cursorEmpty || ItemUtil.isSameItem(clicked, cursor)) {
                                        int total = amt + cursorAmount;
                                        if (total <= maxPlayerInventory) {
                                            clone.setAmount(total);
                                            event.setCurrentItem(null);
                                            player.setItemOnCursor(clone);
                                            event.setResult(Result.DENY);
                                            Config.clearFurnace(furnace);
                                            xpItems = amt;
                                        } else {
                                            int left = total - maxPlayerInventory;

                                            clone.setAmount(maxPlayerInventory);
                                            player.setItemOnCursor(clone);

                                            if (left < 64) {
                                                Config.clearFurnace(furnace);
                                                clone2.setAmount(left);
                                            } else {
                                                Config.setFurnaceAmount(furnace, left);
                                                clone2.setAmount(63);
                                            }
                                            event.setCurrentItem(clone2);

                                            event.setResult(Result.DENY);
                                            xpItems = maxPlayerInventory - cursorAmount;
                                        }
                                    }
                                }
                                ItemStack xpClone = clicked.clone();
                                xpClone.setAmount(xpItems);
                                FurnaceXPConfig.giveFurnaceXP(player, xpClone);
                            }
                            InventoryUtil.updateInventory(player);
                            // normal amounts in the furnace
                        } else {
                            if (event.isShiftClick()) {
                                if (freeSpaces > clickedAmount) {
                                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), top, "");
                                    if (defaultStack > -1 && defaultStack < clone.getAmount()) {
                                        event.setCancelled(true);

                                        event.setCurrentItem(null);

                                        FurnaceXPConfig.giveFurnaceXP(player, clone);

                                        InventoryUtil.addItemsToPlayer(player, clone, "");
                                    }
                                } else {
                                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clone, player.getInventory(), top, "");
                                    if (defaultStack > -1 && defaultStack < clone2.getAmount()) {
                                        event.setCancelled(true);

                                        int newAmount = clickedAmount - freeSpaces;
                                        clone.setAmount(newAmount);
                                        event.setCurrentItem(clone);

                                        clone2.setAmount(freeSpaces);
                                        FurnaceXPConfig.giveFurnaceXP(player, clone2);

                                        InventoryUtil.addItemsToPlayer(player, clone2, "");
                                    }
                                }
                            } else if (event.isLeftClick() || event.isRightClick()) {
                                if (cursorAmount + clickedAmount > maxItems) {
                                    if (maxItems > 0 && cursorAmount == 0) {
                                        if (clickedAmount > maxItems) {
                                            event.setCancelled(true);

                                            clone.setAmount(clickedAmount - maxItems);
                                            event.setCurrentItem(clone);

                                            clone2.setAmount(maxItems);
                                            FurnaceXPConfig.giveFurnaceXP(player, clone2);

                                            player.setItemOnCursor(clone2);
                                        }
                                    } else {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        // prevent clicks outside the inventory area or within result slots
        } else if (cursor != null && clicked != null && slotType != SlotType.RESULT) {
            Player player = (Player) event.getWhoClicked();

            Material cursorType = cursor.getType();
            int cursorDur = ItemUtil.getDurability(cursor);
            int cursorAmount = cursor.getAmount();

            Material clickedType = clicked.getType();
            int clickedDur = ItemUtil.getDurability(clicked);
            int clickedAmount = clicked.getAmount();

            int maxItems;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, null, view, top, cursorType, cursorDur, event.getRawSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, null, view, top, clickedType, clickedDur, event.getRawSlot());
            }

            int rawSlot = event.getRawSlot();

            // TODO: might be able to remove this (except maxstacksize?)
             if (topType == InventoryType.ENCHANTING) {
                if (rawSlot == 0) {
                    if (plugin.supportsInventoryStackSize) {
                        try {
                            top.setMaxStackSize(1);
                        } catch (AbstractMethodError e) {
                            plugin.supportsInventoryStackSize = false;
                        }
                    }
                    if (!event.isShiftClick()) {
                        return;
                    }
                } else if (rawSlot == 1) {
                    if (plugin.supportsInventoryStackSize) {
                        try {
                            top.setMaxStackSize(64);
                        } catch (AbstractMethodError e) {
                            plugin.supportsInventoryStackSize = false;
                        }
                    }
                }
            } else if (topType == InventoryType.BREWING) {
                if (rawSlot <= 2) {
                    if (!event.isShiftClick()) {
                        return;
                    }
                }
            }

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;


            // Creative Player Inventory is handled elsewhere
            if (player.getGameMode() == GameMode.CREATIVE && topType == InventoryType.PLAYER) {
                return;
            }

            if (clickType == ClickType.DOUBLE_CLICK) {
                if (!cursorEmpty && slotEmpty && maxItems != cursor.getMaxStackSize()) {
                    if (!InventoryUtil.canVanillaGatherItemsToCursor(player, top, cursor, maxItems)) {
                        event.setCancelled(true);
                        InventoryUtil.gatherItemsToCursor(player, top, cursor, maxItems);
                    }
                }
            } else if (event.isShiftClick()) {
                if (rawSlot < top.getSize()) {
                    // We only want to override if moving more than a vanilla stack will hold
                    int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, player.getInventory(), top, "");
                    if (defaultStack > -1 && clickedAmount > defaultStack) {
                        InventoryUtil.moveItemsToPlayer(player, clicked.clone(), event, 0, 36, true, top);
                    }
                } else {
                    if (topType == InventoryType.CRAFTING) {
                        PlayerInventory inventory = player.getInventory();

                        if (ItemUtil.isArmor(clickedType)) {
                            ItemStack armorSlot = null;
                            boolean moved = false;

                            ItemStack cloneArmor = clicked.clone();
                            cloneArmor.setAmount(1);
                            if (ItemUtil.isHelmet(clickedType)) {
                                armorSlot = inventory.getHelmet();
                                if (armorSlot == null) {
                                    inventory.setHelmet(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isChestplate(clickedType)) {
                                armorSlot = inventory.getChestplate();
                                if (armorSlot == null) {
                                    inventory.setChestplate(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isLeggings(clickedType)) {
                                armorSlot = inventory.getLeggings();
                                if (armorSlot == null) {
                                    inventory.setLeggings(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isBoots(clickedType)) {
                                armorSlot = inventory.getBoots();
                                if (armorSlot == null) {
                                    inventory.setBoots(cloneArmor);
                                    moved = true;
                                }
                            } else if (ItemUtil.isOffhand(clickedType)) {
                                armorSlot = inventory.getItemInOffHand();
                                if (armorSlot.getType() == Material.AIR) {
                                    inventory.setItemInOffHand(cloneArmor);
                                    moved = true;
                                }
                            }

                            if ((armorSlot == null || armorSlot.getType() == Material.AIR) && moved) {
                                event.setCurrentItem(InventoryUtil.decrementStack(clicked));
                                event.setCancelled(true);
                            } else {
                                InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 9);
                            }
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 9);
                        }
                    } else if (topType == InventoryType.BREWING) {
                        // TODO Prevent stacks from going into potion slots when shift clicking
                        boolean isBrewingIngredient = ItemUtil.isBrewingIngredient(clickedType);
                        boolean isPotion = clickedType == Material.POTION;

                        boolean moved = false;
                        if (isBrewingIngredient) {
                            ItemStack brewingSlot = top.getItem(3);

                            if (brewingSlot == null || ItemUtil.isSameItem(brewingSlot, clicked)) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 3, 4, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }
                        } else if (isPotion) {
                            ItemStack potionSlot1 = top.getItem(0);
                            ItemStack potionSlot2 = top.getItem(1);
                            ItemStack potionSlot3 = top.getItem(2);

                            boolean movedAll = false;
                            if (potionSlot1 == null) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot2 == null && !movedAll) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                } else {
                                    movedAll = true;
                                }
                                moved = true;
                            }
                            if (potionSlot3 == null && !movedAll) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 2, 3, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                                moved = true;
                            }

                        }
                        if (!moved) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 4);
                        }
                    } else if (topType == InventoryType.CHEST && isHorseInventory) {
                        // No chest
                        if (top.getSize() < 2) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 2);
                        // Has chest
                        } else {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 2, top.getSize(), true);

                            if (left > 0) {
                                clicked.setAmount(left);
                            }
                        }
                    } else if (topType == InventoryType.CHEST || topType == InventoryType.DISPENSER || topType == InventoryType.ENDER_CHEST
                            || topType == InventoryType.HOPPER || topType == InventoryType.DROPPER || topType == InventoryType.BARREL) {
                        // We only want to override if moving more than a vanilla stack will hold
                        int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");

                        if (defaultStack > -1 && clickedAmount > defaultStack) {
                            InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "inventory");
                        }
                    } else if (topType == InventoryType.SHULKER_BOX) {
                        // Shulker boxes can't go inside other shulker boxes
                        if (!ItemUtil.isShulkerBox(clicked.getType())) {
                            // We only want to override if moving more than a vanilla stack will hold
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");

                            if (defaultStack > -1 && clickedAmount > defaultStack) {
                                InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "inventory");
                            }
                        }
                    // This adds shift clicking from the player inventory to the workbench.
                    } else if (topType == InventoryType.WORKBENCH) {
                        int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 10, false);
                        if (left > 0) {
                            clicked.setAmount(left);
                        }

                        if (left == clickedAmount) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 10);
                        }
                    // TODO Improve merchant shift click handling (Based on current recipe)
                    } else if (topType == InventoryType.MERCHANT) {
                        InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                    } else if (topType == InventoryType.BEACON) {
                        ItemStack beaconSlot = top.getItem(0);
                        if (ItemUtil.isBeaconFuel(clickedType) && beaconSlot == null) {
                            InventoryUtil.moveItemsToFullInventory(player, clicked.clone(), event, top, true, "");
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 1);
                        }
                    } else if (topType == InventoryType.ANVIL) {
                        ItemStack renameSlot = top.getItem(0);
                        ItemStack repairSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (renameSlot == null || ItemUtil.isSameItem(clicked, renameSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (repairSlot == null || ItemUtil.isSameItem(clicked, repairSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.ENCHANTING) {
                        if (clickedType == Material.LAPIS_LAZULI) {
                            // Let vanilla handle stacking lapis for now.
                        } else if (ItemUtil.isEnchantable(clickedType) && top.getItem(0) == null) {
                            // We only want to override if moving more than a vanilla stack will hold
                            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, clicked, top, null, "inventory");
                            if (defaultStack > -1 && clickedAmount > defaultStack) {
                                int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);

                                if (left > 0) {
                                    clicked.setAmount(left);
                                }
                            }
                        } else {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, top.getSize());
                        }
                    } else if (topType == InventoryType.FURNACE || topType == InventoryType.BLAST_FURNACE || topType == InventoryType.SMOKER) {
                        boolean isFuel = FurnaceUtil.isFuel(clickedType);
                        boolean isBurnable;

                        if (topType == InventoryType.SMOKER) {
                            isBurnable = FurnaceUtil.isSmokerBurnable(clickedType);
                        } else if (topType == InventoryType.BLAST_FURNACE) {
                            isBurnable = FurnaceUtil.isBlastFurnaceBurnable(clickedType);
                        } else {
                            isBurnable = FurnaceUtil.isFurnaceBurnable(clickedType);
                        }


                        // Furnace slots:
                        // 0 - Burnable
                        // 1 - Fuel
                        // 2 - Result
                        ItemStack burnable = top.getItem(0);
                        ItemStack fuel = top.getItem(1);

                        boolean fuelMoved = false;
                        if (isFuel) {
                            if (rawSlot >= 3 && rawSlot <= 38) {
                                if (fuel == null || ItemUtil.isSameItem(fuel,  clicked)) {
                                    fuelMoved = true;
                                    int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        fuelMoved = false;
                                    }
                                }
                            }
                        }

                        boolean burnableMoved = false;
                        if (!fuelMoved && isBurnable) {
                            if (rawSlot >= 3 && rawSlot <= 38) {
                                if (burnable == null || ItemUtil.isSameItem(burnable, clicked)) {
                                    burnableMoved = true;
                                    int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                                    if (left > 0) {
                                        clicked.setAmount(left);
                                        burnableMoved = false;
                                    }
                                }
                            }
                        }

                        // normal item;
                        if ((!fuelMoved && !burnableMoved) || (!isFuel && !isBurnable)) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.LOOM) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);
                        ItemStack thirdSlot = top.getItem(2);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (thirdSlot == null || ItemUtil.isSameItem(clicked, thirdSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 4);
                        }
                    } else if (topType == InventoryType.CARTOGRAPHY) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.GRINDSTONE) {
                        ItemStack firstSlot = top.getItem(0);
                        ItemStack secondSlot = top.getItem(1);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll && (secondSlot == null || ItemUtil.isSameItem(clicked, secondSlot))) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 1, 2, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }
                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 3);
                        }
                    } else if (topType == InventoryType.STONECUTTER) {
                        ItemStack firstSlot = top.getItem(0);

                        boolean movedAll = false;
                        if (firstSlot == null || ItemUtil.isSameItem(clicked, firstSlot)) {
                            int left = InventoryUtil.moveItemsToInventory(player, clicked.clone(), event, top, 0, 1, false);
                            if (left > 0) {
                                clicked.setAmount(left);
                            } else {
                                movedAll = true;
                            }
                        }

                        if (!movedAll) {
                            InventoryUtil.swapInventory(player, clicked.clone(), event, rawSlot, 2);
                        }
                    }
                }
            } else if (event.isLeftClick()) {
                // Pick up a stack with an empty hand
                if (cursorEmpty && !slotEmpty) {
                    if (clickedAmount <= maxItems && clickedAmount > clickedType.getMaxStackSize()) {
                        player.setItemOnCursor(clicked.clone());
                        event.setCurrentItem(null);
                        event.setResult(Result.DENY);
                    } else if (clickedAmount > maxItems) {
                        ItemStack clone = clicked.clone();
                        clone.setAmount(maxItems);
                        player.setItemOnCursor(clone);

                        ItemStack clone2 = clicked.clone();
                        clone2.setAmount(clickedAmount - maxItems);
                        event.setCurrentItem(clone2);
                        event.setResult(Result.DENY);
                        InventoryUtil.updateInventory(player);
                    }

                // Drop a stack into an empty slot
                } else if (!cursorEmpty && slotEmpty) {
                    boolean isShulkerInShulker = topType == InventoryType.SHULKER_BOX && ItemUtil.isShulkerBox(cursor.getType());

                    // Ignore armor slots and attempts to next shulker boxes when dropping items, let default Minecraft handle them.
                    if (event.getSlotType() != SlotType.ARMOR && !isShulkerInShulker) {
                        if (cursorAmount <= maxItems) {
                            event.setCurrentItem(cursor.clone());
                            player.setItemOnCursor(null);
                            event.setResult(Result.DENY);

                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            } else {
                                InventoryUtil.updateInventory(player);
                            }
                        // More items than can fit in this slot
                        } else {
                            ItemStack toDrop = cursor.clone();
                            toDrop.setAmount(maxItems);
                            event.setCurrentItem(toDrop);

                            ItemStack toHold = cursor.clone();
                            toHold.setAmount(cursorAmount - maxItems);
                            player.setItemOnCursor(toHold);

                            event.setResult(Result.DENY);
                            InventoryUtil.updateInventory(player);
                        }
                    }
                // Combine two items
                } else if (!cursorEmpty && !slotEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    if (sameType) {
                        if (ItemUtil.isSameItem(cursor, clicked)) {
                            int total = clickedAmount + cursorAmount;

                            if (total <= maxItems) {
                                if (total > clicked.getMaxStackSize()) {
                                    //player.sendMessage("Combine two stacks fully");
                                    ItemStack clone = cursor.clone();
                                    clone.setAmount(total);
                                    event.setCurrentItem(clone);

                                    player.setItemOnCursor(null);
                                    event.setResult(Result.DENY);

                                    // These inventories need a 2 tick update for RecipeManager
                                    if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                        InventoryUtil.updateInventoryLater(player, 2);
                                    }
                                }
                            } else {
                                //player.sendMessage("Combine two stacks partially");
                                ItemStack clone = cursor.clone();
                                clone.setAmount(maxItems);
                                event.setCurrentItem(clone);

                                ItemStack clone2 = cursor.clone();
                                clone2.setAmount(total - maxItems);
                                player.setItemOnCursor(clone2);

                                event.setResult(Result.DENY);
                                // These inventories need a 2 tick update for RecipeManager
                                if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                    InventoryUtil.updateInventoryLater(player, 2);
                                }
                            }
                        } else {
                            // Swap two unstackable items
                            //player.sendMessage("Swap two unstackable items");
                            event.setCurrentItem(cursor.clone());
                            player.setItemOnCursor(clicked.clone());

                            event.setResult(Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("Swap two items");
                        event.setCurrentItem(cursor.clone());
                        player.setItemOnCursor(clicked.clone());

                        event.setResult(Result.DENY);
                        // These inventories need a 2 tick update for RecipeManager
                        if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                            InventoryUtil.updateInventoryLater(player, 2);
                        }
                    }
                }
            } else if (event.isRightClick()) {
                if (!slotEmpty && !cursorEmpty) {
                    boolean sameType = clickedType.equals(cursorType);

                    // Add two normal items
                    if (sameType) {
                        if (ItemUtil.isSameItem(cursor, clicked)) {
                            int total = clickedAmount + 1;
                            if (total <= maxItems) {
                                if (total > clicked.getMaxStackSize()) {
                                    //player.sendMessage("RC:Drop single item");

                                    ItemStack clone = cursor.clone();
                                    clone.setAmount(total);

                                    event.setCurrentItem(clone);
                                    if (cursorAmount == 1) {
                                        player.setItemOnCursor(null);
                                    } else {
                                        cursor.setAmount(cursorAmount - 1);
                                    }
                                    event.setResult(Result.DENY);
                                    // These inventories need a 2 tick update for RecipeManager
                                    if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                        InventoryUtil.updateInventoryLater(player, 2);
                                    }
                                }
                            } else {
                                event.setCancelled(true);
                            }
                        } else {
                            // Swap two unstackable Items
                            //player.sendMessage("RC:Swap two unstackable items");
                            event.setCurrentItem(cursor.clone());
                            player.setItemOnCursor(clicked.clone());

                            event.setResult(Result.DENY);
                            // These inventories need a 2 tick update for RecipeManager
                            if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                                InventoryUtil.updateInventoryLater(player, 2);
                            }
                        }
                    } else if (cursorAmount > SIItems.ITEM_DEFAULT_MAX) {
                        //player.sendMessage("RC:Swap two items");
                        event.setCurrentItem(cursor.clone());
                        player.setItemOnCursor(clicked.clone());

                        event.setResult(Result.DENY);
                        // These inventories need a 2 tick update for RecipeManager
                        if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH) {
                            InventoryUtil.updateInventoryLater(player, 2);
                        }
                    }
                // pick up half a stack
                } else if (!slotEmpty && cursorEmpty && maxItems > -1) {
                    if (clickedAmount > maxItems) {
                        int maxPickup = (int) Math.round((clickedAmount + 0.5) / 2);

                        ItemStack clone = clicked.clone();
                        ItemStack clone2 = clicked.clone();

                        if (maxPickup < maxItems) {
                            clone.setAmount(maxPickup);
                            player.setItemOnCursor(clone);
                            clone2.setAmount(clickedAmount - maxPickup);

                        } else {
                            clone.setAmount(maxItems);
                            player.setItemOnCursor(clone);
                            clone2.setAmount(clickedAmount - maxItems);
                        }
                        event.setCurrentItem(clone2);
                        event.setResult(Result.DENY);
                    }
                }
            }
        }
    }
}
