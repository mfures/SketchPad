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
	 * PointDouble which is closer to (0,0)
	 */
	private PointDouble p1;
	/**
	 * PointDouble further away from (0,0) in positive (+,+) direction
	 */
	private PointDouble p2;

	/**
	 * Point which is closer to (0,0)
	 */
	private Point ip1;
	/**
	 * Point further away from (0,0) in positive (+,+) direction
	 */
	private Point ip2;

	/**
	 * Rectangles width (delta x)
	 */
	private double width;
	/**
	 * Rectangles height (delta y)
	 */
	private double height;

	/**
	 * Constructor that will cast Point to PointDouble
	 * 
	 * @param p1 Point which is closer to (0,0)
	 * @param p2 Point further away from (0,0) in positive (+,+) direction
	 */
	public Rectangle(Point ip1, Point ip2) {
		this.ip1 = ip1;
		this.ip2 = ip2;
		this.p1 = new PointDouble(ip1.x, ip1.y);
		this.p2 = new PointDouble(ip2.x, ip2.y);

		this.width = ip2.x - ip1.x;
		this.height = ip2.y - ip1.y;
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
		this.ip1 = new Point((int) p1.x, (int) p1.y);
		this.ip2 = new Point((int) p2.x, (int) p2.y);

		this.width = ip2.x - ip1.x;
		this.height = ip2.y - ip1.y;
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

	public Point getIp1() {
		return ip1;
	}

	public Point getIp2() {
		return ip2;
	}

	public static boolean areOverlaping(Rectangle rec1, Rectangle rec2) {
		if (rec1.p1.x >= rec2.p2.x || rec2.p1.x >= rec1.p2.x) {
			return false;
		}

		if (rec1.p1.y >= rec2.p2.y || rec2.p1.y >= rec1.p2.y) {
			return false;
		}
		return true;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	/**
	 * Writes in format (x1,y1),(x2,y2)
	 */
	@Override
	public String toString() {
		return p1.toString() + "," + p2.toString();
	}
}
