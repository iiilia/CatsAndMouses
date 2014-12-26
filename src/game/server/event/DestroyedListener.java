package game.server.event;

import java.util.EventListener;

public interface DestroyedListener extends EventListener{
	public void destroyed(DestroyedEvent e);
}
