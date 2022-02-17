package haveric.stackableItems.config;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public interface IMaxStackSizeProvider {
    int getMaxStackSize(Player player, InventoryType inventoryType, Material material, int durability);
    int getMaxStackSize(String worldName, InventoryType inventoryType, Material material, int durability);
}
