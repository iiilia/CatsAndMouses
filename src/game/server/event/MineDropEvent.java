package game.server.event;

import java.util.EventObject;

import game.server.Mine;

public class MineDropEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private Mine mine;

	public MineDropEvent(Object source, Mine mine) {
		super(source);
		this.mine = mine;
	}
	
	public Mine getMine() {
		return mine;
	}
}
