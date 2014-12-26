package game.server.event;

import java.util.EventListener;

public interface MineDropListener extends EventListener {
	public void mineDropped(MineDropEvent event);
}
