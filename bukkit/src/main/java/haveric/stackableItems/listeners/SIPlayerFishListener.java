package haveric.stackableItems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIPlayerFishListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getInventory().getItemInMainHand();

        ItemStack clone = holding.clone();

        int maxItems = SIItems.getItemMax(player, clone.getType(), ItemUtil.getDurability(clone), player.getInventory().getType());

        // Don't touch default items.
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        // Handle infinite fishing rods
        if (maxItems == SIItems.ITEM_INFINITE) {
            player.getInventory().setItemInMainHand(clone);
        } else {
            InventoryUtil.splitStack(player, false);
        }
    }
}
