package hr.fer.zemris.diprad.recognition.objects;

import java.awt.Point;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;

public class Line implements Comparable<Line> {
	public static final double MAX_TAN = 0.2;

	private Point p1;
	private Point p2;

	private LineType type;
	private Double semiStaticValue;
	private Double length;
	private double slope;
	private double intercept;
	private double tan;

	private BasicMovement bm;

	public Line(Point p1, Point p2, double slope, double intercept, BasicMovement bm) {
		this.p1 = p1;
		this.p2 = p2;
		this.slope = slope;
		this.intercept = intercept;
		this.bm = bm;

		this.tan = calculateTan();
		this.type = calculateType();
		this.length = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));

		if (type == LineType.HORISONTAL) {
			semiStaticValue = 0.5 * (p1.y + p2.y);
		} else {
			semiStaticValue = 0.5 * (p1.x + p2.x);
		}
	}

	private LineType calculateType() {
		if (tan < MAX_TAN) {
			if (Math.abs(slope) <= 1.0) {
				return LineType.HORISONTAL;
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

	public Double getSemiStaticValue() {
		return semiStaticValue;
	}

	public double getSlope() {
		return slope;
	}

	public BasicMovement getBm() {
		return bm;
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

	public Double length() {
		return length;
	}

	@Override
	public int compareTo(Line o) {
		return this.semiStaticValue.compareTo(o.getSemiStaticValue());
	}

	public int getMinX() {
		return Math.min(p1.x, p2.x);
	}

	public int getMinY() {
		return Math.min(p1.y, p2.y);
	}
}
