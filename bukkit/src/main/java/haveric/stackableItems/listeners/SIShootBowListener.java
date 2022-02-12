package haveric.stackableItems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIShootBowListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void shootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack clone = event.getBow().clone();

            int maxItems = SIItems.getItemMax(player, clone.getType(), ItemUtil.getDurability(clone), player.getInventory().getType());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite bows
            if (maxItems == SIItems.ITEM_INFINITE) {
                player.getInventory().setItemInMainHand(clone);
                InventoryUtil.updateInventory(player);
            } else {
                InventoryUtil.splitStack(player, false);
            }

            // TODO: Handle Infinite arrows
            //  Arrows shouldn't be able to be picked up... similar to how the Infinite enchantment works
            //  Perhaps setting the Infinite enchantment temporarily, although I don't like that option
            /*
            int maxArrows = SIItems.getItemMax(player, Material.ARROW, (short) 0, false);
            if (maxArrows == SIItems.ITEM_INFINITE) {
                InventoryUtil.addItems(player, new ItemStack(Material.ARROW));
                InventoryUtil.updateInventory(player);
            }
            */
        }
    }

}
