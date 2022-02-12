package haveric.stackableItems.listeners;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import haveric.stackableItems.config.Config;
import haveric.stackableItems.util.InventoryUtil;
import haveric.stackableItems.util.ItemUtil;
import haveric.stackableItems.util.SIItems;

public class SIPlayerClickListener implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerClick(PlayerInteractEvent event) {
        Action action = event.getAction();

        // Right click air is cancelled for some reason, even when it succeeds
        if (action != Action.RIGHT_CLICK_AIR && (event.useInteractedBlock() == Result.DENY || event.useItemInHand() == Result.DENY)) {
            return;
        }

        ItemStack holding = event.getItem();
        Player player = event.getPlayer();

        if (holding != null) {
            if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) && holding.getType() == Material.GLASS_BOTTLE) {
                Block targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);

                if (targetBlock != null && targetBlock.getType() == Material.WATER) {
                    ItemStack toAdd = new ItemStack(Material.POTION);
                    PotionMeta meta = (PotionMeta) toAdd.getItemMeta();
                    if (meta != null) {
                        meta.setBasePotionData(new PotionData(PotionType.WATER));
                        toAdd.setItemMeta(meta);
                    }

                    int maxItems = SIItems.getItemMax(player, toAdd.getType(), ItemUtil.getDurability(toAdd), player.getInventory().getType());

                    // Let Vanilla handle filling bottles for default value
                    if (maxItems != SIItems.ITEM_DEFAULT) {
                        int amount = holding.getAmount();
                        int slot = player.getInventory().getHeldItemSlot();

                        ItemStack clone = holding.clone();
                        clone.setAmount(amount - 1);

                        InventoryUtil.replaceItem(player.getInventory(), slot, clone);
                        InventoryUtil.addItemsToPlayer(player, toAdd, "");

                        event.setCancelled(true);

                        InventoryUtil.updateInventory(player);
                    }
                }
            } else if (action == Action.RIGHT_CLICK_BLOCK && holding.getType() == Material.FLINT_AND_STEEL && Config.isPreventWastedFASEnabled()) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {
                    Material placedType = clickedBlock.getRelative(event.getBlockFace()).getType();

                    switch (placedType) {
                        case WATER:
                        case LAVA:
                        case FIRE:
                            event.setUseItemInHand(Result.DENY);
                            event.setUseInteractedBlock(Result.DENY);
                            break;
                        default:
                            break;
                    }

                    InventoryUtil.updateInventory(player);
                }
            }

            InventoryUtil.splitStack(player, true);
        }
    }
}
