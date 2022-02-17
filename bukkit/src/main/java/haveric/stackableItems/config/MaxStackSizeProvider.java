package haveric.stackableItems.config;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class MaxStackSizeProvider {
    private static IMaxStackSizeProvider instance;

    public static IMaxStackSizeProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MaxStackProvider hasn't been instantiated yet");
        }
        return instance;
    }

    public static void setInstance(IMaxStackSizeProvider provider) {
        instance = provider;
    }

    public static int get(String worldName, InventoryType inventoryType, Material material, int durability) {
        return getInstance().getMaxStackSize(worldName, inventoryType, material, durability);
    }

    public static int get(Player player, InventoryType inventoryType, Material material, int durability) {
        return getInstance().getMaxStackSize(player, inventoryType, material, durability);
    }
}
