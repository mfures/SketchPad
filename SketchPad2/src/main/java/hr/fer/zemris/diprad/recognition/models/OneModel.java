package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.recognition.Pattern;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.One;

public class OneModel implements Pattern<One> {
	private MyVector v1;
	private MyVector v2;
	private static final double MIN_NORM = 4.2;

	public OneModel() {
		v1 = new MyVector();
		v2 = new MyVector();
	}

	@Override
	public One recognize(BasicMovement bm) {
		initVector(0, 0, v1);

		List<Point> points = bm.getPoints();

		if (points.size() < 3) {
			return null;
		}
		List<MyVector> vectors = toListOfVectors(points);

		double cos;
		// NumberFormat formatter = new DecimalFormat("#0.0000");
		List<Point> breakPoints = new ArrayList<>();

		for (int i = 0; i < vectors.size() - 1; i++) {
			v1 = vectors.get(i);
			v2 = vectors.get(i + 1);
			cos = scalarProduct(v1, v2) / (v1.norm() * v2.norm());

			if (cos < 0.8) {
				breakPoints.add(new Point(v1.i2, v1.i2));
				// System.out.println("i:" + i + " cos:" + formatter.format(cos) + " v1:" + v1 +
				// " v2:" + v2);
			}
		}

		if (breakPoints.size() < 1) {
			return null;
		}

		List<Point> trueBreakPoints = new ArrayList<Point>();
		trueBreakPoints.add(breakPoints.get(0));

		for (int i = 1; i < breakPoints.size(); i++) {
			Point p = breakPoints.get(i);
			if (trueBreakPoints.get(trueBreakPoints.size() - 1).y + 3 >= p.x) {
				trueBreakPoints.get(trueBreakPoints.size() - 1).y = p.x;
			} else {
				trueBreakPoints.add(breakPoints.get(i));
			}
		}

		if (trueBreakPoints.get(0).x == 0) {
			trueBreakPoints.remove(0);
		}

		if (trueBreakPoints.get(trueBreakPoints.size() - 1).y == points.size() - 1) {
			trueBreakPoints.remove(trueBreakPoints.size() - 1);
		}

		if (trueBreakPoints.size() != 1) {
			return null;
		}

		List<Line> lines = LineModel.linesInPoints(points, trueBreakPoints, bm);

		if (lines.size() != 2) {
			return null;
		}

		Line l1 = lines.get(0);
		Line l2 = lines.get(1);

		if (l1.getSlope() >= (-5) && l1.getSlope() <= -0.3) {
			if (!(Math.abs(l2.getSlope()) > 5)) {
				return null;
			} else {
			}
		} else {
			if (l2.getSlope() >= (-5) && l2.getSlope() <= -0.3) {
				if (!(Math.abs(l1.getSlope()) > 5)) {
					return null;
				} else {
					Line tmp = l1;
					l1 = l2;
					l2 = tmp;
				}
			} else
				return null;
		}

		if (l1.length() < l2.length()) {
			Point averagePoint = new Point();

			averagePoint.x = 0;
			averagePoint.y = 0;

			for (Point p1 : bm.getPoints()) {
				averagePoint.x += p1.x;
				averagePoint.y += p1.y;
			}
			averagePoint.x = (int) Math.round(((double) averagePoint.x) / bm.getPoints().size());
			averagePoint.y = (int) Math.round(((double) averagePoint.y) / bm.getPoints().size());

			return new One(averagePoint, l2.length() / 2);
		}

		return null;
	}

	private void initVector(int x, int y, MyVector v) {
		v.v.x = x;
		v.v.y = y;
	}

	public static class MyVector {
		public Point v;
		public Point p1, p2;
		public int i1, i2;

		public MyVector() {
			v = new Point();
		}

		public double norm() {
			return Math.sqrt(v.x * v.x + v.y * v.y);
		}

		public void add(MyVector v1) {
			v.x += v1.v.x;
			v.y += v1.v.y;
		}

		public void setToVector(MyVector v1) {
			v.x = v1.v.x;
			v.y = v1.v.y;
		}

		public boolean semiSameDirection(MyVector v1) {
			return (v1.v.y * v.y >= 0) && (v1.v.x * v.x >= 0);
		}

		@Override
		public String toString() {
			return "x:" + v.x + " y:" + v.y + p1.toString() + " " + p2.toString();
		}
	}

	private static MyVector initNewVector(Point point, Point point2, int i1, int i2) {
		MyVector v = new MyVector();
		v.v.x = point2.x - point.x;
		v.v.y = point2.y - point.y;
		v.p1 = point;
		v.p2 = point2;
		v.i1 = i1;
		v.i2 = i2;
		return v;
	}

	private static List<MyVector> toListOfVectors(List<Point> points) {
		List<MyVector> vectors = new ArrayList<OneModel.MyVector>();

		for (int i = 0; i < points.size() - 1; i++) {
			vectors.add(initNewVector(points.get(i), points.get(i + 1), i, i + 1));
		}

		List<MyVector> vectorsAcumulated = new ArrayList<OneModel.MyVector>();
		vectorsAcumulated.add(vectors.get(0));

		for (int i = 1, j = 0; i < vectors.size(); i++) {
			if (vectorsAcumulated.get(j).norm() < MIN_NORM) {
				if (vectorsAcumulated.get(j).semiSameDirection(vectors.get(i))) {
					vectorsAcumulated.get(j).add(vectors.get(i));
					vectorsAcumulated.get(j).p2 = vectors.get(i).p2;
					vectorsAcumulated.get(j).i2 = vectors.get(i).i2;
				} else {
					j++;
					vectorsAcumulated.add(vectors.get(i));
				}
			} else {
				j++;
				vectorsAcumulated.add(vectors.get(i));
			}
		}

		return vectorsAcumulated;
	}

	private static double scalarProduct(MyVector v1, MyVector v2) {
		return v1.v.x * v2.v.x + v1.v.y * v2.v.y;
	}
}
