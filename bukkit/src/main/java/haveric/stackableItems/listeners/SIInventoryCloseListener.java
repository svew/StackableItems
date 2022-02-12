package haveric.stackableItems.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIInventoryCloseListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryClose(InventoryCloseEvent event) {
        event.
    }
}
