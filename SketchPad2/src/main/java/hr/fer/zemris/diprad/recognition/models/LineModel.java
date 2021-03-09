package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineModel {
	public static List<Line> linesInPoints(List<Point> points, List<Point> breakPoints, BasicMovement bm) {
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
				p2 = points.get(breakPoints.get(k).x);
			} else {
				p2 = points.get(points.size() - 1);
			}
			slope = (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
			intercept = p1.y - slope * p1.x;

			if (k < breakPoints.size()) {
				error = calculateError(points, error, start, breakPoints.get(k).x, slope, intercept);
			} else {
				error = calculateError(points, error, start, points.size() - 1, slope, intercept);
			}

			error /= points.size();

			if (error > 15) {
				continue;
			} else {
				lines.add(new Line(p1, p2, slope, intercept, bm));

			}

			if (k < breakPoints.size())
				start = breakPoints.get(k).y;
		}

		return lines;

	}

	private static double calculateError(List<Point> points, double error, int start, int limit, double slope,
			double intercept) {

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

	public static Line recognize(BasicMovement bm) {
		List<Point> points = bm.getPoints();
		Point p1 = points.get(0);
		Point p2 = points.get(points.size() - 1);
		double slope = (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
		double intercept = p1.y - slope * p1.x;
		double error = 0.0;

		if (Math.abs(slope) <= 1.0) {
			for (Point pp : points) {
				error += Math.pow(slope * pp.x + intercept - pp.y, 2);
			}
		} else {
			if (Double.isInfinite(slope)) {
				for (Point pp : points) {
					error += Math.pow(p1.x - pp.x, 2);
				}
			} else {
				for (Point pp : points) {
					error += Math.pow((pp.y - intercept) / slope - pp.x, 2);
				}
			}
		}

		error /= points.size();

		if (error > 350) {
			return null;
		}

		return new Line(p1, p2, slope, intercept, bm);
	}
}
