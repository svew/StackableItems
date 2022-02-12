package haveric.stackableItems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;

public class SIConsumeItemListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void consumeItem(PlayerItemConsumeEvent event) {
        ItemStack consumedItem = event.getItem();
        int amt = consumedItem.getAmount();

        if (amt > 1) {
            Player player = event.getPlayer();
            Material type = consumedItem.getType();

            if (type == Material.MILK_BUCKET) {
                InventoryUtil.addItemsToPlayer(player, new ItemStack(Material.BUCKET), "");
            } else if (type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW || type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW) {
                int heldSlot = player.getInventory().getHeldItemSlot();

                InventoryUtil.replaceItem(player.getInventory(), heldSlot, new ItemStack(type, amt - 1));
                InventoryUtil.addItemsToPlayer(player, new ItemStack(Material.BOWL), "");
            }
        }
    }
}
