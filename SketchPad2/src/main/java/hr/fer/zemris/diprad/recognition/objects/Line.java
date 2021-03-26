package hr.fer.zemris.diprad.recognition.objects;

import java.awt.Point;

import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class Line {
	public static final double MAX_TAN = 0.2;

	private Point p1;
	private Point p2;
	private double averageX;
	private double averageY;

	private LineType type;
	private Double length;
	private double slope;
	private double intercept;
	private double tan;
	private Rectangle boundingBox;

	private BasicMovementWrapper bmw;

	public Line(Point p1, Point p2, double slope, double intercept, BasicMovementWrapper bmw) {
		this.p1 = p1;
		this.p2 = p2;
		this.slope = slope;
		this.intercept = intercept;
		this.bmw = bmw;

		this.averageY = 0.5 * (p1.y + p2.y);
		this.averageX = 0.5 * (p1.x + p2.x);

		this.tan = calculateTan();
		this.type = calculateType();
		this.length = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
		initBoundingBox();
	}

	public Line(PointDouble p1, PointDouble p2, double slope, double intercept, BasicMovementWrapper bmw) {
		this.p1 = p1.toPoint();
		this.p2 = p2.toPoint();
		this.slope = slope;
		this.intercept = intercept;
		this.bmw = bmw;

		this.averageY = 0.5 * (p1.y + p2.y);
		this.averageX = 0.5 * (p1.x + p2.x);

		this.tan = calculateTan();
		this.type = calculateType();
		this.length = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
		initBoundingBox();
	}

	private LineType calculateType() {
		if (tan < MAX_TAN) {
			if (Math.abs(slope) <= 1.0) {
				return LineType.HORIZONTAL;
			}

			return LineType.VERTICAL;
		}

		if (slope > 0) {
			return LineType.DIAGONAL_PLUS;
		}

		return LineType.DIAGONAL_MINUS;
	}

	private double calculateTan() {
		double tmp = Math.abs(slope);

		if (tmp <= 1.0)
			return tmp;

		return 1. / slope;
	}

	public Point getP1() {
		return p1;
	}

	public Point getP2() {
		return p2;
	}

	public LineType getType() {
		return type;
	}

	public double getSlope() {
		return slope;
	}

	public BasicMovementWrapper getBmw() {
		return bmw;
	}

	public double getIntercept() {
		return intercept;
	}

	public double getTan() {
		return tan;
	}

	public double forX(double x) {
		return slope * x + intercept;
	}

	public double forY(double y) {
		if (slope == 0) {
			return Double.POSITIVE_INFINITY;// Should throw an exception
		}

		if (Double.isInfinite(slope)) {
			return p1.x;
		}

		return (y - intercept) / slope;
	}

	public double distanceFromPointToLine(Point p) {
		if (Double.isInfinite(slope)) {
			return Math.abs(this.p1.x - p.x);
		}

		return Math.abs(-slope * p.x + p.y - intercept) / Math.sqrt(slope * slope + 1);
	}

	public Double length() {
		return length;
	}

	public double getAverageX() {
		return averageX;
	}

	public double getAverageY() {
		return averageY;
	}

	public int getMinX() {
		return Math.min(p1.x, p2.x);
	}

	public int getMaxX() {
		return Math.max(p1.x, p2.x);
	}

	public int getMinY() {
		return Math.min(p1.y, p2.y);
	}

	public int getMaxY() {
		return Math.max(p1.y, p2.y);
	}

	public void initBoundingBox() {
		this.boundingBox = new Rectangle(new Point(getMinX(), getMinY()), new Point(getMaxX(), getMaxY()));
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
		result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line other = (Line) obj;
		if (p1 == null) {
			if (other.p1 != null)
				return false;
		} else if (!p1.equals(other.p1))
			return false;
		if (p2 == null) {
			if (other.p2 != null)
				return false;
		} else if (!p2.equals(other.p2))
			return false;
		return true;
	}
}
