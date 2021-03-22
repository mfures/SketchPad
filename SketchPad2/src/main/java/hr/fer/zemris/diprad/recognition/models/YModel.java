package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;

import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class YModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		if (!LineModel.acumulateBreakPointsWhichAreClose(bmw1.getBm().getPoints()).isEmpty()) {
			System.out.println("1");
			return false;
		}
		if (!LineModel.acumulateBreakPointsWhichAreClose(bmw2.getBm().getPoints()).isEmpty()) {
			System.out.println("2. breakPoints size: "
					+ LineModel.acumulateBreakPointsWhichAreClose(bmw2.getBm().getPoints()).size());
			return false;
		}
		Line l1 = LineModel.recognize(bmw1);
		if (l1 == null) {
			System.out.println("3");
			return false;
		}

		Line l2 = LineModel.recognize(bmw2);
		if (l2 == null) {
			System.out.println("4");
			return false;
		}
		if (l1.getSlope() / l2.getSlope() > 0) {
			System.out.println("5");
			return false;
		}

		if (l2.length() > l1.length()) {
			Line tmp = l1;
			l1 = l2;
			l2 = tmp;
		}

		if (l1.getSlope() > 0) {
			System.out.println("Bad orientation");
			return false;
		}

		if (l1.getType() == LineType.HORIZONTAL && l2.getType() == LineType.VERTICAL) {
			System.out.println("6");
			return false;
		}

		if (l2.getType() == LineType.HORIZONTAL && l1.getType() == LineType.VERTICAL) {
			System.out.println("7");
			return false;
		}

		if (!(l1.getSlope() >= (-20) && l1.getSlope() <= -0.3)) {
			// System.out.println(l1.getSlope());
			System.out.println("8");
			return false;
		}
		if (!(l2.getSlope() <= (20) && l2.getSlope() >= 0.3)) {
			System.out.println("9");
			return false;
		}

		double lRatio = l2.length() / l1.length();
		double minRatio = 0.25;
		double maxRatio = 0.6;
		if (lRatio > maxRatio || lRatio < minRatio) {
			System.out.println(lRatio + " 10");
			return false;
		}

		Point p = l2.getP1();
		if (l2.getP2().x > l2.getP1().x) {
			p = l2.getP2();
		}

		double dist = l1.distanceFromPointToLine(p);
		if (dist > l1.length() * 0.10) {
			System.out.println("Bad overlap");
			return false;
		}
		if (l2.getAverageY() > l1.getAverageY()) {
			System.out.println("L2 to low");
			return false;
		}

		double minL1y = Math.min(l1.getP1().y, l1.getP2().y);
		double minL2y = Math.min(l2.getP1().y, l2.getP2().y);

		if (minL2y - minL1y > 0.2 * l2.length()) {
			System.out.println("l2 to high");
			return false;
		}

		return true;
	}
}
