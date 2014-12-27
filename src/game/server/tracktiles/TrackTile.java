package game.server.tracktiles;

import java.awt.Point;
import java.awt.geom.Area;

import org.json.JSONObject;

import game.server.Direction;
import game.server.JSONable;
import game.server.TrackState;

public abstract class TrackTile implements JSONable {
	
	public static final int SEGMENT_LENGTH = 45;
	public static final int TRACK_WIDTH = SEGMENT_LENGTH * 5;
	
	private Point location;
	private Direction orientation;

	
	protected TrackTile(Direction orientation) {
		this.orientation = orientation;
	}

	public abstract Area getArea();
	public abstract TrackState getConnect(Direction direction);
	public abstract Point getRelativeConnect(Direction direction);
	public abstract boolean checkDirection(Direction direction);
	public abstract void calcLocation(TrackState state);
	
	/**
	 *  получить локацию
	 */
	public Point getLocation() {
		return location;
	}
	
	/**
	 * задать локацию точки
	 */
	protected void setLocation(Point p) {
		this.location = new Point(p);
	}
	
	/**
	 * задать локацию координаты точки
	 */
	protected void setLocation(int x, int y) {
		this.location = new Point(x, y);
	}
	/**
	 * получить ориентацию
	 */
	public Direction getOrientation() {
		return orientation;
	}
	
	/**
	 *  передать данные объектом Json
	 */
	@Override
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("message", "tile");
		obj.put("type", getClass().getSimpleName());
		obj.put("orientation", getOrientation().toString());
		return obj;
	}
	
	public abstract String getDescription(Direction direction);
	
	public String toString() {
	    return getClass().getSimpleName() + ": " + getLocation().toString() + ", " + getOrientation().toString(); 
	}
}
