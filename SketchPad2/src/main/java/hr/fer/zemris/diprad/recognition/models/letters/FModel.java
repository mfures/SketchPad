package hr.fer.zemris.diprad.recognition.models.letters;

import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.JShape;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class FModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		Line l = LinearModel.recognize(bmw1);
		JShape jShape = null;
		if (l != null) {
			if (l.getType() != LineType.HORIZONTAL) {
				l = LinearModel.recognize(bmw2);
				if (l != null) {
					if (l.getType() != LineType.HORIZONTAL) {
						// System.out.println("No horisontal line");
						return false;
					} else {
						jShape = JModel.recognize(bmw1);
					}
				}
			} else {
				jShape = JModel.recognize(bmw2);
			}
		} else {
			l = LinearModel.recognize(bmw2);
			if (l != null) {
				if (l.getType() != LineType.HORIZONTAL) {
					// System.out.println("No horisontal line");
					return false;
				} else {
					jShape = JModel.recognize(bmw1);
				}
			} else {
				// System.out.println("No line found");
				return false;
			}
		}

		if (jShape == null) {
			// System.out.println("No jshape found");
			return false;
		}

		if (!jShape.isForF()) {
			// System.out.println("Shape was for g not f");
			return false;
		}

		double jWidthHeightRatio = jShape.getBoundingBox().getWidth() / jShape.getBoundingBox().getHeight();
		if (jWidthHeightRatio < 0.1 || jWidthHeightRatio > 0.70) {
			// System.out.println("J inapropriate jWidthHeightRatio: " + jWidthHeightRatio);
			return false;
		}

		double lengthHeightRatio = l.length() / jShape.getBoundingBox().getHeight();
		if (lengthHeightRatio < 0.05 || lengthHeightRatio > 0.45) {
			// System.out.println("1.:" + lengthHeightRatio);
			return false;
		}

		double xAtAverageY = jShape.getL().forY(l.getAverageY());
		double forcedPassedLength = 0.2 * l.length();
		// System.out.println(l.getMinX() + " " + forcedPassedLength + " " +
		// xAtAverageY);
		// System.out.println(l.getMaxX() + " " + forcedPassedLength + " " +
		// xAtAverageY);

		if (l.getMinX() + forcedPassedLength > xAtAverageY || l.getMaxX() - forcedPassedLength < xAtAverageY) {
			// System.out.println("Horisontal line doesnt cross");
			return false;
		}

		if (l.getAverageY() < jShape.getBoundingBox().getP1().y + 0.35 * jShape.getBoundingBox().getHeight()) {
			// System.out.println("L too high");
			return false;
		}
		if (l.getAverageY() > jShape.getBoundingBox().getP1().y + 0.8 * jShape.getBoundingBox().getHeight()) {
			// System.out.println("L too low");
			return false;
		}

		return true;
	}
}
