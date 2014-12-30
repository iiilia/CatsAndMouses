package game.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class CollisionTools {

	public static int isRight(Line2D line, Point2D c) {
		Vector2D ab = new Vector2D.Cartesian(line.getP1(), line.getP2());
		Vector2D b = new Vector2D.Cartesian(c.getY(), -c.getX());
		b = b.subtract(new Vector2D.Cartesian(line.getP1().getY(), -line
				.getP1().getX()));
		return (int) Math.signum(ab.dotProduct(b));
	}
}
