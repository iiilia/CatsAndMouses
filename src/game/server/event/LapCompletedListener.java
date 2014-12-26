package game.server.event;

import java.util.EventListener;

public interface LapCompletedListener extends EventListener {
	public void lapCompleted(LapCompletedEvent e);
}
