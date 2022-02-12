package haveric.stackableItems.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIFillBucketListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getInventory().getItemInMainHand();

        int amount = holding.getAmount();

        if (amount > 1) {
            ItemStack toAdd = event.getItemStack();
            int maxItems = SIItems.getItemMax(player, toAdd.getType(), ItemUtil.getDurability(toAdd), player.getInventory().getType());

            // Let Vanilla handle filling buckets for default value
            if (maxItems != SIItems.ITEM_DEFAULT) {
                int slot = player.getInventory().getHeldItemSlot();

                ItemStack clone = holding.clone();
                clone.setAmount(amount - 1);

                InventoryUtil.replaceItem(player.getInventory(), slot, clone);
                InventoryUtil.addItemsToPlayer(player, toAdd, "");

                event.setCancelled(true);

                Block clickedBlock = event.getBlockClicked();

                Material bucketType = toAdd.getType();
                if (bucketType == Material.WATER_BUCKET) {
                    BlockData data = clickedBlock.getBlockData();
                    if (data instanceof Waterlogged) {
                        Waterlogged waterloggedData = (Waterlogged) data;
                        waterloggedData.setWaterlogged(false);
                        clickedBlock.setBlockData(waterloggedData);
                    } else {
                        clickedBlock.setType(Material.AIR);
                    }
                } else {
                    clickedBlock.setType(Material.AIR);
                }

                InventoryUtil.updateInventory(player);
            }
        }
    }
}
