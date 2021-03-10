package hr.fer.zemris.diprad.util;

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
}
