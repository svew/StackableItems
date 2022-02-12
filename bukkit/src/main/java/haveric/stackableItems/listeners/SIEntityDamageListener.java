package haveric.stackableItems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIEntityDamageListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack holding = player.getInventory().getItemInMainHand();

            int maxItems = SIItems.getItemMax(player, holding.getType(), ItemUtil.getDurability(holding), player.getInventory().getType());

            // Don't touch default items.
            if (maxItems == SIItems.ITEM_DEFAULT) {
                return;
            }

            // Handle infinite weapons
            if (maxItems == SIItems.ITEM_INFINITE) {
                PlayerInventory inventory = player.getInventory();
                InventoryUtil.replaceItem(inventory, inventory.getHeldItemSlot(), holding.clone());
                InventoryUtil.updateInventory(player);
            } else {
                InventoryUtil.splitStack(player, true);
            }
        }
    }
}
