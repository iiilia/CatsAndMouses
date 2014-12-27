package game.server.tracktiles;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;

import game.server.Direction;
import game.server.TrackState;

public class Straight extends TrackTile {

	private static final Rectangle vertical = new Rectangle(TRACK_WIDTH,
			SEGMENT_LENGTH * 3);
	private static final Rectangle horizontal = new Rectangle(SEGMENT_LENGTH * 3,
			TRACK_WIDTH);

	public Straight(Direction orientation) {
		super(orientation);
	}

	/**
	 * получить площадь прямоугольника в вертикальном направлении,
	 * если направление Up и Down
	 * иначе - горизонтальное
	 */
	@Override
	public Area getArea() {
		int x = getLocation().x;
		int y = getLocation().y;
		Rectangle rect;
		if (getOrientation() == Direction.UP
				|| getOrientation() == Direction.DOWN) {
			rect = new Rectangle(vertical);
		} else {
			rect = new Rectangle(horizontal);
		}
		rect.setLocation(x, y);
		return new Area(rect);
	}

	/**
	 * проверить ориентацию
	 */
	@Override
	public boolean checkDirection(Direction direction) {
		return direction == getOrientation();
	}

	/**
	 * получить относительное положение
	 * если ориентация карты совпадает с вертикальным направлением, 
	 * то перемещаем точку вверх на SEGMENT_LENGTH * 3
	 * с горизонтальным на SEGMENT_LENGTH * 3
	 */
	@Override
	public Point getRelativeConnect(Direction direction) {
		if (!checkDirection(direction)) {
			throw new IllegalArgumentException("Cannot move "
					+ direction.toString() + " on a straight going "
					+ getOrientation().toString() + ".");
		}
		Point pos = new Point();
		if (getOrientation() == Direction.UP || getOrientation() == Direction.DOWN) {
			if (direction == Direction.DOWN) {
				pos.translate(0, SEGMENT_LENGTH * 3);
			}
		} else {
			if (direction == Direction.RIGHT) {
				pos.translate(SEGMENT_LENGTH * 3, 0);
			}
		}
		return pos;
	}

	@Override
	public void calcLocation(TrackState state) {
		if (!checkDirection(state.getDirection())) {
			throw new IllegalArgumentException("Cannot move "
					+ state.getDirection().toString() + " on a straight going "
					+ getOrientation().toString() + ".");
		}
		if (getOrientation() == Direction.UP || getOrientation() == Direction.DOWN) {
			if (state.getDirection() == Direction.UP) {
				setLocation(state.getLocation().x, state.getLocation().y
						- SEGMENT_LENGTH * 3);
			} else {
				setLocation(state.getLocation());
			}
		} else {
			if (state.getDirection() == Direction.LEFT) {
				setLocation(state.getLocation().x - SEGMENT_LENGTH * 3,
						state.getLocation().y);
			} else {
				setLocation(state.getLocation());
			}
		}
	}

	@Override
	public TrackState getConnect(Direction direction) {
		Point res = new Point(getLocation());
		Point rel = getRelativeConnect(direction);
		res.translate(rel.x, rel.y);
		return new TrackState(res, direction);
	}
	
	public String getDescription(Direction direction) {
	    return "straight";
	}
}
