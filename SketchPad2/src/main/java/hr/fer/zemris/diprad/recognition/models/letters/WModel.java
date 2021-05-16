package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class WModel {
	private static final String W = "W";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();
		List<Integer> acumulatedBreakPoints = LinearModel.acumulateBreakPointsWhichAreClose(points);
		if (acumulatedBreakPoints.size() != 5) {
			// System.out.println("No points: " + acumulatedBreakPoints.size());
			return null;
		}

		List<Line> lines = LinearModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 4) {
			// System.out.println("No lines");
			return null;
		}

		Line l1 = lines.get(0), l2 = lines.get(1), l3 = lines.get(2), l4 = lines.get(3);

		if (!(l1.getSlope() <= (30) && l1.getSlope() >= 0.9)) {
			// System.out.println("Bad l1 slope: " + l1.getSlope());
			return null;
		}
		if (!(l2.getSlope() >= (-30) && l2.getSlope() <= -0.9)) {
			// System.out.println("Bad l2 slope: " + l2.getSlope());
			return null;
		}
		if (!(l3.getSlope() <= (30) && l3.getSlope() >= 0.9)) {
			// System.out.println("Bad l3 slope: " + l3.getSlope());
			return null;
		}
		if (!(l4.getSlope() >= (-30) && l4.getSlope() <= -0.9)) {
			// System.out.println("Bad l4 slope: " + l4.getSlope());
			return null;
		}

		if (!((l1.getP2().x < l2.getP2().x) && (l2.getP2().x < l3.getP2().x) && (l3.getP2().x < l4.getP2().x))) {
			// System.out.println("ja");
			return null;
		}

		double averageLength = (l1.length() + l2.length() + l3.length() + l4.length()) / 4;
		double lenghtTolerance = 0.2;
		double coefMx = (1 + lenghtTolerance);
		double coefMn = (1 - lenghtTolerance);
		for (Line l : lines) {
			if (l.length() < coefMn * averageLength || l.length() > coefMx * averageLength) {
				// System.out.println("Bad lengths");
				return null;
			}
		}

		return new CharacterModel(W, bmw);
	}
}
