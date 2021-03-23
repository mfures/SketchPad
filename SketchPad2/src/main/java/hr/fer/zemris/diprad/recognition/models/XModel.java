package hr.fer.zemris.diprad.recognition.models;

import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class XModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		if (LinearModel.acumulateBreakPointsWhichAreClose(bmw1.getBm().getPoints()).size() != 2) {
			// System.out.println("1: " +
			// LineModel.acumulateBreakPointsWhichAreClose(bmw1.getBm().getPoints()).size());
			return false;
		}
		if (LinearModel.acumulateBreakPointsWhichAreClose(bmw2.getBm().getPoints()).size() != 2) {
			// System.out.println("w: " +
			// LineModel.acumulateBreakPointsWhichAreClose(bmw2.getBm().getPoints()).size());
			return false;
		}

		Line l1 = LinearModel.recognize(bmw1);
		if (l1 == null) {
			// System.out.println("3");
			return false;
		}

		Line l2 = LinearModel.recognize(bmw2);
		if (l2 == null) {
			// System.out.println("4");
			return false;
		}
		if (l1.getSlope() / l2.getSlope() > 0) {
			// System.out.println("5");
			return false;
		}

		if (l2.getSlope() < 0) {
			Line tmp = l1;
			l1 = l2;
			l2 = tmp;
		}

		if (l1.getType() == LineType.HORIZONTAL && l2.getType() == LineType.VERTICAL) {
			// System.out.println("6");
			return false;
		}

		if (l2.getType() == LineType.HORIZONTAL && l1.getType() == LineType.VERTICAL) {
			// System.out.println("7");
			return false;
		}

		if (!(l1.getSlope() >= (-20) && l1.getSlope() <= -0.3)) {
			// System.out.println("8");
			return false;
		}
		if (!(l2.getSlope() <= (20) && l2.getSlope() >= 0.3)) {
			// System.out.println("9");
			return false;
		}

		double lRatio = l1.length() / l2.length();
		double lengthTolerance = 0.5;
		if (lRatio > (1 + lengthTolerance) || lRatio < (1 / (1 + lengthTolerance))) {
			// System.out.println("10");
			return false;
		}

		int minX = Math.min(l1.getP1().x, l1.getP2().x);
		int maxX = Math.max(l1.getP1().x, l1.getP2().x);
		int minY = Math.min(l1.getP1().y, l1.getP2().y);
		int maxY = Math.max(l1.getP1().y, l1.getP2().y);
		if (l2.getAverageX() >= maxX || l2.getAverageX() <= minX || l2.getAverageY() >= maxY
				|| l2.getAverageY() <= minY) {
			// System.out.println("11");
			return false;
		}

		double maxRatio = 2;
		double minRatio = 1 / maxRatio;
		double distanceRatioX = (maxX - l2.getAverageX()) / (l2.getAverageX() - minX);
		double distanceRatioY = (maxY - l2.getAverageY()) / (l2.getAverageY() - minY);

		if (distanceRatioX > maxRatio || distanceRatioY > maxRatio || distanceRatioY < minRatio
				|| distanceRatioY < minRatio) {
			// System.out.println("12");
			return false;
		}

		return true;
	}
}
