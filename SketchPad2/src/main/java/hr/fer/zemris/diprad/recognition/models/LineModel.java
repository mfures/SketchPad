package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;

public class LineModel {
	public static final double COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE = 0.175;
	private static final double MAX_AVERAGE_SQUARE_ERROR = 350;

	public static List<Line> linesInPoints(List<Point> points, List<Integer> breakPoints, BasicMovementWrapper bmw) {
		Point p1;
		Point p2;
		double slope;
		double intercept;

		List<Line> lines = new ArrayList<>();
		double error = 0.0;

		int start = 0;

		// System.out.println("Radim linije. Dobio sam ovoliko točaka: " +
		// breakPoints.size());
		// breakPoints.forEach((x) -> System.out.print(x + " "));
		// System.out.println();
		for (int k = 1; k < breakPoints.size(); k++) {
			p1 = points.get(start);
			p2 = points.get(breakPoints.get(k));

			slope = (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
			intercept = p1.y - slope * p1.x;

			error = calculateError(points, start, breakPoints.get(k), slope, intercept);

			error /= points.size();

			if (error > MAX_AVERAGE_SQUARE_ERROR) {
				// System.out.println(error);
			} else {
				lines.add(new Line(p1, p2, slope, intercept, bmw));

			}

			start = breakPoints.get(k);
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
		breakPoints.add(0);

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

		breakPoints.add(points.size() - 1);
		return breakPoints;
	}

	public static List<Integer> acumulateBreakPointsWhichAreClose(List<Point> points) {
		List<Integer> breakPoints = LineModel.calculateBreakPoints(points);
		if (breakPoints.size() == 2) {// first and last index (0 and size-1)
			return breakPoints;
		}
		List<Integer> trueBreakPoints = new ArrayList<>();

		double totalLength = 0;
		for (int i = 0; i < breakPoints.size() - 1; i++) {
			totalLength += MyVector.norm(points.get(breakPoints.get(i)), points.get(breakPoints.get(i + 1)));
			// System.out.println(totalLength);
		}

		// System.out.println("Total inital number of breakPoints: " +
		// breakPoints.size());

		double activeNorm;
		trueBreakPoints.add(0);
		// System.out.println("Total length: " + totalLength);
		// System.out.println("Uvijet: " +
		// COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength);
		for (int i = 1; i < breakPoints.size() - 1; i++) {
			activeNorm = MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
					points.get(breakPoints.get(i)));
			// System.out.println("Trenutna norma: " + activeNorm);
			if (activeNorm > COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength) {
				trueBreakPoints.add(breakPoints.get(i));
			} else {
				totalLength -= activeNorm;
				totalLength -= MyVector.norm(points.get(breakPoints.get(i)), points.get(breakPoints.get(i + 1)));
				totalLength += MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
						points.get(breakPoints.get(i + 1)));
			}
		}

		// System.out.println("Ajde: " + trueBreakPoints.size());

		if (trueBreakPoints.size() > 2) {
			activeNorm = MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
					points.get(points.size() - 1));
			// System.out.println(activeNorm);
			if (activeNorm > COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength) {
				trueBreakPoints.add(points.size() - 1);
			} else {
				trueBreakPoints.set(trueBreakPoints.size() - 1, points.size() - 1);
			}
			return trueBreakPoints;
		}
		if (trueBreakPoints.size() == 1) {
			trueBreakPoints.add(points.size() - 1);
			return trueBreakPoints;
		}

		activeNorm = MyVector.norm(points.get(trueBreakPoints.get(1)), points.get(0));
		// System.out.println("Hejj?: " + activeNorm);
		if (activeNorm > (1 - COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE) * totalLength) {
			trueBreakPoints.set(1, points.size() - 1);
			return trueBreakPoints;
		}

		activeNorm = MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
				points.get(points.size() - 1));
		// System.out.println("Heeej: " + activeNorm);
		if (activeNorm > COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength) {
			trueBreakPoints.add(points.size() - 1);
		} else {
			trueBreakPoints.set(trueBreakPoints.size() - 1, points.size() - 1);
		}

		return trueBreakPoints;
	}
}
