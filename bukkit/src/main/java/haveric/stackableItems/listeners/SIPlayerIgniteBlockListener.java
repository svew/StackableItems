package haveric.stackableItems.listeners;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.*;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIPlayerIgniteBlockListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerIgniteBlock(BlockIgniteEvent event) {
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
            Player player = event.getPlayer();
            // Only deal with players.
            if (player != null) {
                ItemStack holding = player.getInventory().getItemInMainHand();

                // Since repeatedly using flint and steel causes durability loss, reset durability on a new hit.
                ItemStack newStack = holding.clone();
                ItemUtil.setDurability(newStack, 0);
                int maxItems = SIItems.getItemMax(player, newStack.getType(), ItemUtil.getDurability(newStack), player.getInventory().getType());

                // Don't touch default items.
                if (maxItems == SIItems.ITEM_DEFAULT) {
                    return;
                }
                // Handle unlimited flint and steel
                if (maxItems == SIItems.ITEM_INFINITE) {
                    player.getInventory().setItemInMainHand(newStack);
                    InventoryUtil.updateInventory(player);
                } else {
                    InventoryUtil.splitStack(player, false);
                }
            }
        }
    }
}
