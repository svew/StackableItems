package haveric.stackableItems.listeners;

import haveric.stackableItems.StackableItems;

public class SICreativeClickListener extends SIListenerBase {

    public SICreativeClickListener(StackableItems si) {
        super(si);
    }

    /* // TODO: Handle Creative inventory
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void creativeClick(InventoryCreativeEvent event) {
        Inventory inventory = event.getInventory();

        if (plugin.supportsInventoryStackSize) {
            try {
                inventory.setMaxStackSize(SIItems.ITEM_NEW_MAX);
            } catch (AbstractMethodError e) {
                plugin.supportsInventoryStackSize = false;
            }
        }

        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();


        if (cursor != null && clicked != null) {
            Player player = (Player) event.getWhoClicked();

            Material clickedType = clicked.getType();
            short clickedDur = clicked.getDurability();

            Material cursorType = cursor.getType();
            short cursorDur = cursor.getDurability();

            int maxItems = 0;
            if (clickedType == Material.AIR) {
                maxItems = InventoryUtil.getInventoryMax(player, inventory, cursorType, cursorDur, event.getSlot());
            } else {
                maxItems = InventoryUtil.getInventoryMax(player, inventory, clickedType, clickedDur, event.getSlot());
            }
            plugin.log.info("Max items: " + maxItems);
            plugin.log.info("ClickType: " + event.getClick());
            plugin.log.info("Shift?: " + event.isShiftClick());

            SlotType slotType = event.getSlotType();
            plugin.log.info("SlotType: " + slotType);
            plugin.log.info("Inv size: " + inventory.getSize());
            int rawSlot = event.getRawSlot();

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;
        }
    }
    */
}
