package haveric.stackableItems.listeners;

import org.bukkit.event.Listener;

import haveric.stackableItems.StackableItems;

public abstract class SIListenerBase implements Listener {

    protected StackableItems plugin;
    protected String itemDisabledMessage;

    public SIListenerBase(StackableItems si) {
        plugin = si;
        itemDisabledMessage = String.format("[%s] This item has been disabled.", plugin.getDescription().getName());
    }
}
