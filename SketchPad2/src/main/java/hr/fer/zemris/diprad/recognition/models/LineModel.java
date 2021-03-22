package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;

public class LineModel {
	public static final double COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE = 0.15;
	private static final double MAX_AVERAGE_SQUARE_ERROR = 350;

	public static List<Line> linesInPoints(List<Point> points, List<Integer> breakPoints, BasicMovementWrapper bmw) {
		Point p1;
		Point p2;
		double slope;
		double intercept;

		List<Line> lines = new ArrayList<>();
		double error = 0.0;

		int start = 0;

		for (int k = 0; k <= breakPoints.size(); k++) {
			p1 = points.get(start);
			if (k < breakPoints.size()) {
				p2 = points.get(breakPoints.get(k));
			} else {
				p2 = points.get(points.size() - 1);
			}

			slope = (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
			intercept = p1.y - slope * p1.x;

			if (k < breakPoints.size()) {
				error = calculateError(points, start, breakPoints.get(k), slope, intercept);
			} else {
				error = calculateError(points, start, points.size() - 1, slope, intercept);
			}

			error /= points.size();

			if (error > MAX_AVERAGE_SQUARE_ERROR) {
				// System.out.println(error);
			} else {
				lines.add(new Line(p1, p2, slope, intercept, bmw));

			}

			if (k < breakPoints.size()) {
				start = breakPoints.get(k);
			}
		}

		return lines;

	}

	private static double calculateError(List<Point> points, int start, int limit, double slope, double intercept) {
		double error = 0;
		if (Math.abs(slope) <= 1.0) {
			for (int i = start; i <= limit; i++) {
				error += Math.pow(slope * points.get(i).x + intercept - points.get(i).y, 2);
			}
		} else {
			if (Double.isInfinite(slope)) {
				for (int i = start; i <= limit; i++) {
					error += Math.pow(points.get(i).x - points.get(i).x, 2);
				}
			} else {
				for (int i = start; i <= limit; i++) {
					error += Math.pow((points.get(i).y - intercept) / slope - points.get(i).x, 2);
				}
			}
		}
		return error;
	}

	public static Line recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();
		Point p1 = points.get(0);
		Point p2 = points.get(points.size() - 1);

		double slope = (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
		double intercept = p1.y - slope * p1.x;

		double error = calculateError(points, 0, points.size() - 1, slope, intercept);
		error /= points.size();

		if (error > MAX_AVERAGE_SQUARE_ERROR) {
			return null;
		}

		return new Line(p1, p2, slope, intercept, bmw);
	}

	/**
	 * This function calculates break points.
	 * 
	 * @param points
	 * @return
	 */
	public static List<Integer> calculateBreakPoints(List<Point> points) {
		if (points.size() < 3) {
			return new ArrayList<>();
		}

		List<MyVector> vectors = MyVector.listOfPointsToListOfVectors(points);
		// System.out.println("Gotovi vektori");
		// vectors.forEach((x) -> System.out.print(x + " "));
		// System.out.println("\n");
		MyVector v1 = new MyVector();
		MyVector v2 = new MyVector();

		double cos;
		// NumberFormat formatter = new DecimalFormat("#0.0000");
		List<Integer> breakPoints = new ArrayList<>();

		for (int i = 0; i < vectors.size() - 1; i++) {
			v1 = vectors.get(i);
			v2 = vectors.get(i + 1);
			cos = MyVector.scalarProduct(v1, v2) / (v1.norm() * v2.norm());

			if (cos < 0.8) {
				breakPoints.add(v1.i2);
				// System.out.println("i:" + i + " cos:" + formatter.format(cos) + " v1:" + v1 +
				// " v2:" + v2);
			}
		}

		return breakPoints;
	}

	public static List<Integer> acumulateBreakPointsWhichAreClose(List<Point> points) {
		List<Integer> breakPoints = LineModel.calculateBreakPoints(points);
		System.out.println("Total inital number of breakPoints: " + breakPoints.size());
		int numOfPoints = points.size();

		List<Integer> trueBreakPoints = new ArrayList<>();
		if (breakPoints.isEmpty()) {
			return trueBreakPoints;
		}

		int index = 0;
		while (breakPoints.get(index) <= COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * numOfPoints) {
			index++;

			if (index == breakPoints.size()) {
				return trueBreakPoints;
			}
		}

		if (index == breakPoints.size() - 1) {
			if (numOfPoints - breakPoints.get(index) <= COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * numOfPoints) {
				return trueBreakPoints;
			}
		}

		trueBreakPoints.add(breakPoints.get(index));
		// System.out.println(
		// trueBreakPoints.get(0) + " min:" +
		// COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalNumOfPoints);
		int counter = 0;// index of last int in trueBreakPoints

		for (int i = index + 1; i < breakPoints.size(); i++) {
			int p = breakPoints.get(i);
			if (((numOfPoints - p) > (COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * numOfPoints))) {
				if (((p - trueBreakPoints.get(counter)) > COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE
						* numOfPoints)) {
					trueBreakPoints.add(p);
					counter++;
				} else {
					trueBreakPoints.set(counter, p);
				}
			}

		}

		return trueBreakPoints;
	}
}
