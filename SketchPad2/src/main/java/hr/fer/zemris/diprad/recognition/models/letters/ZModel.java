package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ZModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();
		List<Integer> acumulatedBreakPoints = LinearModel.acumulateBreakPointsWhichAreClose(points);
		if (acumulatedBreakPoints.size() != 4) {
			return false;
		}

		List<Line> lines = LinearModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 3) {
			return false;
		}

		Line l1 = lines.get(0), l2 = lines.get(1), l3 = lines.get(2);

		if (l1.getType() != LineType.HORIZONTAL || l3.getType() != LineType.HORIZONTAL) {
			return false;
		}

		if (!(l2.getSlope() >= (-5) && l2.getSlope() <= -0.3)) {
			return false;
		}

		if (l1.getAverageY() > l3.getAverageY()) {
			return false;
		}

		double l13ratio = l1.length() / l3.length();
		double lengthTolerance = 0.5;
		if (l13ratio > (1 + lengthTolerance) || l13ratio < (1 / (1 + lengthTolerance))) {
			return false;
		}

		double maxLength = Math.max(l1.length(), l3.length());
		double l2maxLengthRatio = l2.length() / maxLength;
		if (l2maxLengthRatio > (1 + lengthTolerance) || l2maxLengthRatio < (1 / (1 + lengthTolerance))) {
			return false;
		}

		return true;
	}
}
