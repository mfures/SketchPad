package hr.fer.zemris.diprad.util;

import java.awt.Point;

/**
 * Describes a point (x,y) where x and y are type double
 * 
 * @author Matej
 *
 */
public class PointDouble {
	public double x;
	public double y;

	public PointDouble(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public PointDouble(Point point) {
		this.x = point.x;
		this.y = point.y;
	}

	/**
	 * Returns string in format (x,y)
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		sb.append(x);
		sb.append(",");
		sb.append(y);
		sb.append(")");

		return sb.toString();
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return a+b
	 */
	public static PointDouble addPoints(PointDouble a, PointDouble b) {
		return new PointDouble(a.x + b.x, a.y + b.y);
	}

	public static PointDouble addPoints(Point a, PointDouble b) {
		return new PointDouble(a.x + b.x, a.y + b.y);
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return a-b
	 */
	public static PointDouble subPoints(PointDouble a, PointDouble b) {
		return new PointDouble(a.x - b.x, a.y - b.y);
	}

	public static PointDouble subPoints(Point a, PointDouble b) {
		return new PointDouble(a.x - b.x, a.y - b.y);
	}

	public static PointDouble subPoints(Point a, Point b) {
		return new PointDouble(a.x - b.x, a.y - b.y);
	}

	public Point toPoint() {
		return new Point((int) Math.round(x), (int) Math.round(y));
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return b*a
	 */
	public static PointDouble mulPoint(PointDouble a, double b) {
		return new PointDouble(b * a.x, b * a.y);
	}

	public static PointDouble normalizedPoint(PointDouble p) {
		double norm = Math.sqrt(p.x * p.x + p.y * p.y);
		return PointDouble.mulPoint(p, 1 / norm);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
}
