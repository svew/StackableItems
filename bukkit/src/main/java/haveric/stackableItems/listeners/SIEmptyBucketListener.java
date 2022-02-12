package haveric.stackableItems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;

public class SIEmptyBucketListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void emptyBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack holding = player.getInventory().getItemInMainHand();

        int amount = holding.getAmount();

        if (amount > 1) {
            ItemStack clone = holding.clone();
            clone.setAmount(amount - 1);

            int slot = player.getInventory().getHeldItemSlot();

            InventoryUtil.replaceItem(player.getInventory(), slot, clone);
            InventoryUtil.addItemsToPlayer(player, event.getItemStack(), "");
        }
    }
}
