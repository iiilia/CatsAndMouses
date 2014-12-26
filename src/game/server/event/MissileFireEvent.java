package game.server.event;

import java.util.EventObject;

import game.server.Missile;

public class MissileFireEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private Missile missile;

	public MissileFireEvent(Object source, Missile missile) {
		super(source);
		this.missile = missile;
	}
	
	public Missile getMissile() {
		return missile;
	}
}
