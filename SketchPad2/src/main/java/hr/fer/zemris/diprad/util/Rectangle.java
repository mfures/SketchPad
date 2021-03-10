package hr.fer.zemris.diprad.util;

import java.awt.Point;

/**
 * Utility class that describes a simple rectangle
 * 
 * @author Matej
 *
 */
public class Rectangle {
	/**
	 * Point which is closer to (0,0)
	 */
	private PointDouble p1;
	/**
	 * Point further away from (0,0) in positive (+,+) direction
	 */
	private PointDouble p2;

	/**
	 * Constructor that will cast Point to PointDouble
	 * 
	 * @param p1 Point which is closer to (0,0)
	 * @param p2 Point further away from (0,0) in positive (+,+) direction
	 */
	public Rectangle(Point p1, Point p2) {
		this.p1 = new PointDouble(p1.x, p1.y);
		this.p2 = new PointDouble(p2.x, p2.y);
	}

	/**
	 * Normal constructor
	 * 
	 * @param p1 Point which is closer to (0,0)
	 * @param p2 Point further away from (0,0) in positive (+,+) direction
	 */
	public Rectangle(PointDouble p1, PointDouble p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public PointDouble getP1() {
		return p1;
	}

	public void setP1(PointDouble p1) {
		this.p1 = p1;
	}

	public PointDouble getP2() {
		return p2;
	}

	public void setP2(PointDouble p2) {
		this.p2 = p2;
	}

	/**
	 * Writes in format (x1,y1),(x2,y2)
	 */
	@Override
	public String toString() {
		return p1.toString() + "," + p2.toString();
	}
}
