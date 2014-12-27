/*
 * 
 */
package game.server;

import java.awt.Point;

public class TrackState {
	private Point location;
	private Direction direction;

	// конструктор (координаты, направление)
	public TrackState(int x, int y, Direction direction) {
		this.location = new Point(x, y);
		this.direction = direction;
	}
	
	// конструктор (точка, направление)
	public TrackState(Point location, Direction direction) {
		this(location.x, location.y, direction);
	}

	// Получить местоположение
	public Point getLocation() {
		return new Point(location);
	}

	// Получить направление
	public Direction getDirection() {
		return direction;
	}

	// Сравнение объектов
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TrackState)) {
			return false;
		}
		TrackState other2 = (TrackState) other;
		return location.equals(other2.location)
				&& direction == other2.direction;
	}
}
