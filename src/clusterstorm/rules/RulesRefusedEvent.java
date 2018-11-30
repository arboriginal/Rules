package clusterstorm.rules;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class RulesRefusedEvent extends Event implements Cancellable {
	
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player player;

    public RulesRefusedEvent(Player player) {
    	this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
      return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
      cancelled = cancel;
    }
}
