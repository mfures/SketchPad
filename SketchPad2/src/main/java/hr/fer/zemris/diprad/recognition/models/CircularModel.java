package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;

public class CircularModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		return recognize(bmw.getBm().getPoints(), 0, bmw.getBm().getPoints().size() - 1, bmw);
	}

	public static boolean recognize(List<Point> points, int startIndex, int endIndex, BasicMovementWrapper bmw) {
		List<MyVector> vectors = MyVector.initVectorList(points, startIndex, endIndex);
		double totalNorm = 0;
		for (int i = 0; i < vectors.size(); i++) {
			totalNorm += vectors.get(i).norm();
		}

		for (int k = 0; k < (endIndex - startIndex + 1.0) / 2; k++) {
			for (int i = startIndex; i <= endIndex; i++) {

			}
		}

		Point averagePoint = new Point(0, 0);
		for (int i = startIndex; i <= endIndex; i++) {
			Point p = points.get(i);
			averagePoint.x += p.x;
			averagePoint.y += p.y;
		}
		averagePoint.x = (int) Math.round((averagePoint.x) / (endIndex - startIndex + 1.0));
		averagePoint.y = (int) Math.round((averagePoint.y) / (endIndex - startIndex + 1.0));

		double min = Integer.MAX_VALUE, max = 0;
		int minIndex = 0, maxIndex = 0;

		for (int i = startIndex; i <= endIndex; i++) {
			Point p = points.get(i);
			double dist = Math.sqrt(Math.pow(p.x - averagePoint.x, 2) + Math.pow(p.y - averagePoint.y, 2));
			if (min > dist) {
				minIndex = i;
				min = dist;
			}
			if (max < dist) {
				maxIndex = i;
				max = dist;
			}
		}

		if (!(min / max > 0.1)) {
			System.out.println("Bad max/min ratio:" + min / max);
			return false;
		}

		System.out.println("Average point:        " + averagePoint);
		System.out.println("Min point:            " + points.get(minIndex));
		Point minToCenter = new Point(averagePoint.x - points.get(minIndex).x, averagePoint.y - points.get(minIndex).y);
		System.out.println("Min to center vector: " + minToCenter);

		double s1, s2;
		double angle;
		int checkSum = 0;
		for (int i = startIndex; i < endIndex; i++) {
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

		// (endIndex - startIndex + 1.0)
		System.out.println("Cheksum:" + (checkSum / ((double) (endIndex - startIndex))));
		if (Math.abs((checkSum / ((double) (endIndex - startIndex)))) > 0.) {
			return true;
		}

		System.out.println("Bad check sum");
		return false;
	}

	public static double calculateSlope(Point p1, Point p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}
}
