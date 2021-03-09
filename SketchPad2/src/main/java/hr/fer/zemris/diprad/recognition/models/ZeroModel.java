package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.recognition.Pattern;
import hr.fer.zemris.diprad.recognition.objects.Zero;

public class ZeroModel implements Pattern<Zero> {
	private Point averagePoint = new Point();
	private double min, max, avg, dist;

	@Override
	public Zero recognize(BasicMovement bm) {
		averagePoint.x = 0;
		averagePoint.y = 0;

		for (Point p1 : bm.getPoints()) {
			averagePoint.x += p1.x;
			averagePoint.y += p1.y;
		}
		averagePoint.x = (int) Math.round(((double) averagePoint.x) / bm.getPoints().size());
		averagePoint.y = (int) Math.round(((double) averagePoint.y) / bm.getPoints().size());

		max = 0;
		min = Integer.MAX_VALUE;

		for (Point p1 : bm.getPoints()) {
			dist = Math.sqrt(Math.pow(p1.x - averagePoint.x, 2) + Math.pow(p1.y - averagePoint.y, 2));
			if (min > dist) {
				min = dist;
			}
			if (max < dist) {
				max = dist;
			}
			avg += dist;
		}

		avg /= bm.getPoints().size();

		if (!(min / max > 0.3)) {
			// System.out.println("Bad max/min ratio:" + min / max);
			return null;
		}

		double s1, s2;
		List<Point> points = bm.getPoints();
		double angle;
		int checkSum = 0;
		for (int i = 0; i < bm.getPoints().size() - 1; i++) {
			s1 = calculateSlope(points.get(i), averagePoint);
			s2 = calculateSlope(averagePoint, points.get(i + 1));
			if (Double.isInfinite(s1)) {
				angle = Math.atan(1 / s2);
			} else if (Double.isInfinite(s2)) {
				angle = Math.atan(-1 / s1);
			} else {
				angle = Math.atan((s1 - s2) / (1 + s1 * s2));
			}

			if (angle > 0)
				checkSum++;
			else
				checkSum--;
		}

		if ((checkSum / (points.size() - 1.0) > 0.9)) {
			return new Zero(averagePoint, avg);
		}

		//System.out.println((checkSum / (points.size() - 1.0)));
		return null;
	}

	public static double calculateSlope(Point p1, Point p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}
}
