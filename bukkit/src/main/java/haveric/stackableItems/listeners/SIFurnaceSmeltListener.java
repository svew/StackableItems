package haveric.stackableItems.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import haveric.stackableItems.config.Config;
import haveric.stackableItems.util.SIItems;

import org.bukkit.block.Furnace;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class SIFurnaceSmeltListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void furnaceSmelt(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        Furnace furnace = (Furnace) block.getState();
        ItemStack result = furnace.getInventory().getResult();
        if (result != null) {
            int amt = result.getAmount() + 1;

            int maxFurnaceSize = Config.getMaxBlockAmount(furnace, result.getType());
            if (maxFurnaceSize > SIItems.ITEM_DEFAULT_MAX && maxFurnaceSize <= SIItems.ITEM_NEW_MAX) {

                // going to be a full furnace
                if (amt == SIItems.ITEM_DEFAULT_MAX) {
                    int furnaceAmt = Config.getFurnaceAmount(furnace);
                    if (furnaceAmt == maxFurnaceSize - 1) {
                        result.setAmount(furnaceAmt);
                        Config.clearFurnace(furnace);
                    // increment virtual count
                    } else {
                        if (furnaceAmt == -1) {
                            furnaceAmt = SIItems.ITEM_DEFAULT_MAX;
                        } else {
                            furnaceAmt++;
                        }

                        Config.setFurnaceAmount(furnace, furnaceAmt);

                        result.setAmount(62);
                    }
                }
            }
        }
        // TODO: Handle a max furnace amount of less than 64 items
        /*
        else if (maxFurnaceSize < SIItems.ITEM_DEFAULT_MAX) {
            if (amt == maxFurnaceSize) {
                //event.setCancelled(true);
                // TODO: Can we somehow stop the furnace burning so we can keep the fuel?
            }
        }
        */
    }
}
