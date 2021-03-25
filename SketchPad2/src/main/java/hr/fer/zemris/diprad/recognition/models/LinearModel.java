package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.recognition.testers.StrongPositiveColinearityTester;
import hr.fer.zemris.diprad.util.MyVector;

public class LinearModel {

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

			if (error > KTableModel.MAX_AVERAGE_SQUARE_ERROR) {
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

		if (error > KTableModel.MAX_AVERAGE_SQUARE_ERROR) {
			return null;
		}

		return new Line(p1, p2, slope, intercept, bmw);
	}

	public static List<Integer> acumulateBreakPointsWhichAreClose(List<Point> points, Tester<MyVector> t) {
		List<Integer> breakPoints = BreakPointsUtil.calculateBreakPoints(points, t);
		// System.out.println("Initial breakPoints: " + breakPoints.size());
		if (breakPoints.size() == 2) {// first and last index (0 and size-1)
			return breakPoints;
		}

		double totalLength = LinearModel.calculateTotalLength(points, breakPoints);

		List<Integer> trueBreakPoints = new ArrayList<>();

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
			if (activeNorm > KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength) {
				trueBreakPoints.add(breakPoints.get(i));
			} else {
				totalLength -= activeNorm;
				totalLength -= MyVector.norm(points.get(breakPoints.get(i)), points.get(breakPoints.get(i + 1)));
				totalLength += MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
						points.get(breakPoints.get(i + 1)));
			}
		}

		return addLastPointAndCheckEdgeCases(points, totalLength, trueBreakPoints);
	}

	private static List<Integer> addLastPointAndCheckEdgeCases(List<Point> points, double totalLength,
			List<Integer> trueBreakPoints) {
		double activeNorm;
		if (trueBreakPoints.size() == 1) {
			trueBreakPoints.add(points.size() - 1);
			return trueBreakPoints;
		}
		if (trueBreakPoints.size() == 2) {
			activeNorm = MyVector.norm(points.get(0), points.get(trueBreakPoints.get(1)));
			if (activeNorm > (1 - KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE) * totalLength) {
				trueBreakPoints.set(1, points.size() - 1);
				return trueBreakPoints;
			}
		}

		activeNorm = MyVector.norm(points.get(trueBreakPoints.get(trueBreakPoints.size() - 1)),
				points.get(points.size() - 1));
		if (activeNorm > KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalLength) {
			trueBreakPoints.add(points.size() - 1);
		} else {
			trueBreakPoints.set(trueBreakPoints.size() - 1, points.size() - 1);
		}
		return trueBreakPoints;
	}

	public static double calculateTotalLength(List<Point> points, List<Integer> breakPoints) {
		double totalLength = 0;
		for (int i = 0; i < breakPoints.size() - 1; i++) {
			totalLength += MyVector.norm(points.get(breakPoints.get(i)), points.get(breakPoints.get(i + 1)));
		}
		return totalLength;
	}

	public static List<Integer> acumulateBreakPointsWhichAreClose(List<Point> points) {
		return acumulateBreakPointsWhichAreClose(points, new StrongPositiveColinearityTester());
	}
}
