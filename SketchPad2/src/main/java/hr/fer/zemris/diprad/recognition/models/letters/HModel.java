package hr.fer.zemris.diprad.recognition.models.letters;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class HModel {
	private static final String H = "h";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		int breakPosition = CircularModel.generateBestBreakPoint(bmw);
		if (breakPosition == -1) {
			// System.out.println("Bad number of break points: ");
			return null;
		}
		CircularObject co;
		Line l;

		if (bmw.getBm().getPoints().get(0).y < bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).y) {
			if (bmw.getBm().getPoints().get(0).x > bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x) {
				return null;
			}

			l = LinearModel.recognize(bmw, 0, breakPosition);
			co = CircularModel.recognize(bmw.getBm().getPoints(), breakPosition, bmw.getBm().getPoints().size() - 1,
					bmw);
		} else {
			if (bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x > bmw.getBm().getPoints().get(0).x) {
				return null;
			}

			l = LinearModel.recognize(bmw, breakPosition, bmw.getBm().getPoints().size() - 1);
			co = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
		}

		if (co == null || l == null) {
			// System.out.println("Some are null");
			return null;
		}

		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
			// System.out.println("co minMax: " + co.getMinMaxRatio());
			return null;
		}

		if (l.getType() != LineType.VERTICAL) {
			// System.out.println("Line not vertical");
			return null;
		}

		if (co.getTotalAngle() < 220 || co.getTotalAngle() > 305) {
			// System.out.println("invalid angle: " + co.getTotalAngle());
			return null;
		}

		if (co.getTheta() > 125 || co.getTheta() < 55) {
			// System.out.println("Bad opening position: " + co.getTheta());
			return null;
		}
		if (co.getBoundingBox().getWidth() > 0.9 * l.length() || co.getBoundingBox().getWidth() < 0.2 * l.length()) {
			// System.out.println("bad co width ratio: " + co.getBoundingBox().getWidth() /
			// l.length());
		}
		if (co.getBoundingBox().getHeight() > 0.7 * l.length() || co.getBoundingBox().getHeight() < 0.3 * l.length()) {
			// System.out.println("bad co height ratio: " + co.getBoundingBox().getHeight()
			// / l.length());
		}

		double deltaY = co.getBoundingBox().getP2().y - l.getMaxY();
		if (Math.abs(deltaY) > 0.30 * l.length()) {
			// System.out.println("Delta y");
//			System.out.println(Math.abs(deltaY));
//			System.out.println(0.30 * l.length());
			return null;
		}

		return new CharacterModel(H, bmw);
	}
}
