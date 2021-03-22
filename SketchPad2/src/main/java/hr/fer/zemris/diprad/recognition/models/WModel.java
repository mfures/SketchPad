package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class WModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();
		List<Integer> acumulatedBreakPoints = LineModel.acumulateBreakPointsWhichAreClose(points);
		if (acumulatedBreakPoints.size() != 5) {
			System.out.println("No points: " + acumulatedBreakPoints.size());
			return false;
		}

		List<Line> lines = LineModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 4) {
			System.out.println("No lines");
			return false;
		}

		Line l1 = lines.get(0), l2 = lines.get(1), l3 = lines.get(2), l4 = lines.get(3);

		if (!(l1.getSlope() <= (20) && l1.getSlope() >= 0.9)) {
			System.out.println("Bad l1 slope: " + l1.getSlope());
			return false;
		}
		if (!(l2.getSlope() >= (-20) && l2.getSlope() <= -0.9)) {
			System.out.println("Bad l2 slope: " + l2.getSlope());
			return false;
		}
		if (!(l3.getSlope() <= (20) && l3.getSlope() >= 0.9)) {
			System.out.println("Bad l3 slope: " + l3.getSlope());
			return false;
		}
		if (!(l4.getSlope() >= (-20) && l4.getSlope() <= -0.9)) {
			System.out.println("Bad l4 slope: " + l4.getSlope());
			return false;
		}

		if (!((l1.getP2().x < l2.getP2().x) && (l2.getP2().x < l3.getP2().x) && (l3.getP2().x < l4.getP2().x))) {
			System.out.println("ja");
			return false;
		}

		double averageLength = (l1.length() + l2.length() + l3.length() + l4.length()) / 4;
		double lenghtTolerance = 0.2;
		double coefMx = (1 + lenghtTolerance);
		double coefMn = (1 - lenghtTolerance);
		for (Line l : lines) {
			if (l.length() < coefMn * averageLength || l.length() > coefMx * averageLength) {
				System.out.println("Bad lengths");
				return false;
			}
		}

		return true;
	}
}
