package hr.fer.zemris.diprad.recognition.models.letters;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class HModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Integer> breakPoints = CircularModel.generateAcumulatedBreakPoints(bmw);
		if (breakPoints.size() != 3) {
			// breakPoints.forEach((x) -> System.out.print(x + " "));
			// System.out.println("Bad number of break points: " + breakPoints.size());
			return false;
		}
		CircularObject co;
		Line l;

		if (bmw.getBm().getPoints().get(0).y < bmw.getBm().getPoints().get(breakPoints.get(2)).y) {
			if (bmw.getBm().getPoints().get(0).x > bmw.getBm().getPoints().get(breakPoints.get(2)).x) {
				return false;
			}

			l = LinearModel.recognize(bmw, 0, breakPoints.get(1));
			co = CircularModel.recognize(bmw.getBm().getPoints(), breakPoints.get(1), breakPoints.get(2), bmw);
		} else {
			if (bmw.getBm().getPoints().get(breakPoints.get(2)).x > bmw.getBm().getPoints().get(0).x) {
				return false;
			}

			l = LinearModel.recognize(bmw, breakPoints.get(1), breakPoints.get(2));
			co = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPoints.get(1), bmw);
		}

		if (co == null || l == null) {
			// System.out.println("Some are null");
			return false;
		}

		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
			// System.out.println("co minMax: " + co.getMinMaxRatio());
			return false;
		}

		if (l.getType() != LineType.VERTICAL) {
			// System.out.println("Line not vertical");
			return false;
		}

		if (co.getTotalAngle() < 220 || co.getTotalAngle() > 305) {
			// System.out.println("invalid angle: " + co.getTotalAngle());
			return false;
		}

		if (co.getTheta() > 125 || co.getTheta() < 55) {
			// System.out.println("Bad opening position: " + co.getTheta());
			return false;
		}
		if (co.getBoundingBox().getWidth() > 0.7 * l.length() || co.getBoundingBox().getWidth() < 0.2 * l.length()) {
			// System.out.println("bad co width ratio: " + co.getBoundingBox().getWidth() /
			// l.length());
		}
		if (co.getBoundingBox().getHeight() > 0.65 * l.length() || co.getBoundingBox().getHeight() < 0.3 * l.length()) {
			// System.out.println("bad co height ratio: " + co.getBoundingBox().getHeight()
			// / l.length());
		}

		double deltaY = co.getBoundingBox().getP2().y - l.getMaxY();
		if (Math.abs(deltaY) > 0.15 * l.length()) {
			// System.out.println("Delta y");
			return false;
		}

		return true;
	}
}
