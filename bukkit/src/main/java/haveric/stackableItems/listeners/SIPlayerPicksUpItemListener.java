package haveric.stackableItems.listeners;

import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIPlayerPicksUpItemListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerPicksUpItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();

        int maxItems = SIItems.getItemMax(player, stack.getType(), ItemUtil.getDurability(stack), player.getInventory().getType());

        // Don't touch default items
        if (maxItems == SIItems.ITEM_DEFAULT) {
            return;
        }

        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(player, stack);

        if (freeSpaces == 0 || maxItems == 0) {
            event.setCancelled(true);
        } else {
            // We only want to override if moving more than a vanilla stack will hold
            int defaultStack = InventoryUtil.getAmountDefaultCanMove(player, stack, player.getInventory(), null, "pickup");

            if (defaultStack > -1 && (stack.getAmount() > defaultStack || stack.getAmount() > stack.getMaxStackSize())) {
                InventoryUtil.addItemsToPlayer(player, stack.clone(), "pickup");
                Random random = new Random();
                Sound pickupSound = Sound.ENTITY_ITEM_PICKUP;
                player.playSound(item.getLocation(), pickupSound, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                item.remove();

                event.setCancelled(true);
            }
        }
    }
}
