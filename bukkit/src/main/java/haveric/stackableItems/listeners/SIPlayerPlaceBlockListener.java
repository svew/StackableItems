package haveric.stackableItems.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import haveric.stackableItems.StackableItems;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIPlayerPlaceBlockListener extends SIListenerBase {
    
    public SIPlayerPlaceBlockListener(StackableItems si) {
        super(si);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlock();

        EquipmentSlot hand = event.getHand();
        ItemStack holding = event.getItemInHand();
        ItemStack clone = holding.clone();
        Player player = event.getPlayer();

        int maxItems = SIItems.getItemMax(player, clone.getType(), ItemUtil.getDurability(clone), player.getInventory().getType());
        if (ItemUtil.isShulkerBox(holding.getType())) {
            BlockStateMeta meta = (BlockStateMeta) holding.getItemMeta();
            if (meta != null) {
                NamespacedKey keyStackCounts = new NamespacedKey(StackableItems.getPlugin(), "shulkerstackcounts");

                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(keyStackCounts, PersistentDataType.INTEGER_ARRAY)) {
                    int[] itemCounts = container.get(keyStackCounts, PersistentDataType.INTEGER_ARRAY);
                    if (itemCounts != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                            Inventory shulkerInventory = shulkerBox.getInventory();
                            for (int i = 0; i < itemCounts.length; i++) {
                                int itemCount = itemCounts[i];
                                if (itemCount > 64) {
                                    ItemStack item = shulkerInventory.getItem(i);
                                    if (item != null) {
                                        item.setAmount(itemCount);
                                    }
                                }
                            }

                        }, 0);
                    }
                }
            }
        } else if (holding.getType() == Material.POWDER_SNOW_BUCKET) {
            ItemStack bucket = new ItemStack(Material.BUCKET);
            int maxBuckets = SIItems.getItemMax(player, bucket.getType(), ItemUtil.getDurability(bucket), player.getInventory().getType());
            if (clone.getAmount() > clone.getMaxStackSize() && (maxItems > SIItems.ITEM_DEFAULT || maxBuckets > SIItems.ITEM_DEFAULT)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    clone.setAmount(clone.getAmount() - 1);
                    if (hand == EquipmentSlot.HAND) {
                        player.getInventory().setItemInMainHand(clone);
                    } else {
                        player.getInventory().setItemInOffHand(clone);
                    }
                }, 0);

                InventoryUtil.addItemsToPlayer(player, bucket, "");
            }
        }

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }
        // Restore unlimited items
        if (maxItems == SIItems.ITEM_INFINITE) {
            if (hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(clone);
            } else {
                player.getInventory().setItemInOffHand(clone);
            }
        }
    }
}
