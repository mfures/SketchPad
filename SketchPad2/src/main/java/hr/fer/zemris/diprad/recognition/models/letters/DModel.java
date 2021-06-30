package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class DModel {
	private static final String D = "D";

	public static CharacterModel recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		CircularObject co = initCO(bmw1);
		if (co == null) {
			co = initCO(bmw2);
			if (co == null) {
				// System.out.println("1.Invalid circular objects");
				return null;
			}

			return testForCircularObjectAndBMW(co, bmw1, bmw2);
		}

		return testForCircularObjectAndBMW(co, bmw2, bmw1);
	}

	// line, co
	private static CircularObject initCO(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			// System.out.println("co was null");
			return null;
		}

		if (co.getMinMaxRatio() > 0.7 ) {
			// System.out.println("co minMax: " + co.getMinMaxRatio());
			return null;
		}

		return co;
	}

	private static CharacterModel testForCircularObjectAndBMW(CircularObject co, BasicMovementWrapper bmw,
			BasicMovementWrapper bmw2) {
		Line l = LinearModel.recognize(bmw);
		if(l==null) {
			return null;
		}
		
		if (l.getType() != LineType.VERTICAL) {
			// System.out.println("Line not vertical");
			return null;
		}

		if (co.getTotalAngle() < 205 || co.getTotalAngle() > 325) {
			// System.out.println("invalid angle: " + co.getTotalAngle());
			return null;
		}

		if (!(co.getTheta() > 130 || co.getTheta() < -130)) {
			// System.out.println("Bad opening position: " + co.getTheta());
			return null;
		}

		Point p1 = co.getBmw().getBm().getPoints().get(0);
		Point p2 = co.getBmw().getBm().getPoints().get(co.getBmw().getBm().getPoints().size() - 1);

		double dist1 = l.distanceFromPointToLine(p1);
		double dist2 = l.distanceFromPointToLine(p2);
		if (l.length() * 0.75 > Math.abs(p1.y - p2.y)) {
			return null;
		}

		// System.out.println(dist1 + " " + dist2);
		// System.out.println(co.getTotalNorm());

		if (dist1 - 0.35 * co.getTotalNorm() > 0 || dist2 - 0.35 * co.getTotalNorm() > 0) {
			return null;
		}

		// System.out.println(p1.x + " " + co.getTotalNorm() + " " + l.forY(p1.y));
		// System.out.println(p2.x + " " + co.getTotalNorm() + " " + l.forY(p2.y));

		if (p1.x - l.forY(p1.y) - 0.2 * co.getTotalNorm() > 0
				|| p2.x - l.forY(p2.y) - 0.2 * co.getTotalNorm() > 0) {
			return null;
		}

		double widthHeightRatio = co.getBoundingBox().getWidth() / co.getBoundingBox().getHeight();
		if (widthHeightRatio > 2 || widthHeightRatio < 0.2) {
			return null;
		}

		return new CharacterModel(D, bmw, bmw2);
	}
}
