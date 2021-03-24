package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;

import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class DModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		CircularObject co = initCO(bmw1);
		if (co == null) {
			co = initCO(bmw2);
			if (co == null) {
				// System.out.println("1.Invalid circular objects");
				return false;
			}

			return testForCircularObjectAndBMW(co, bmw1);
		}

		return testForCircularObjectAndBMW(co, bmw2);
	}

	// line, co
	private static CircularObject initCO(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			// System.out.println("co was null");
			return null;
		}

		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
			// System.out.println("co minMax: " + co.getMinMaxRatio());
			return null;
		}

		return co;
	}

	private static boolean testForCircularObjectAndBMW(CircularObject co, BasicMovementWrapper bmw) {
		Line l = LinearModel.recognize(bmw);
		if (l.getType() != LineType.VERTICAL) {
			// System.out.println("Line not vertical");
			return false;
		}

		if (co.getTotalAngle() < 220 || co.getTotalAngle() > 305) {
			// System.out.println("invalid angle: " + co.getTotalAngle());
			return false;
		}

		if (!(co.getTheta() > 145 || co.getTheta() < -145)) {
			// System.out.println("Bad opening position: " + co.getTheta());
			return false;
		}

		Point p1 = co.getBmw().getBm().getPoints().get(0);
		Point p2 = co.getBmw().getBm().getPoints().get(co.getBmw().getBm().getPoints().size() - 1);

		double dist1 = l.distanceFromPointToLine(p1);
		double dist2 = l.distanceFromPointToLine(p2);
		if (l.length() * 0.9 > Math.abs(p1.y - p2.y)) {
			return false;
		}

		// System.out.println(dist1 + " " + dist2);
		// System.out.println(co.getTotalNorm());

		if (dist1 - 0.15 * co.getTotalNorm() > 0 || dist2 - 0.15 * co.getTotalNorm() > 0) {
			return false;
		}

		// System.out.println(p1.x + " " + co.getTotalNorm() + " " + l.forY(p1.y));
		// System.out.println(p2.x + " " + co.getTotalNorm() + " " + l.forY(p2.y));

		if (p1.x - l.forY(p1.y) - 0.075 * co.getTotalNorm() > 0
				|| p2.x - l.forY(p2.y) - 0.075 * co.getTotalNorm() > 0) {
			return false;
		}

		return true;
	}
}
