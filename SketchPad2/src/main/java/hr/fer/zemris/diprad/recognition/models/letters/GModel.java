package hr.fer.zemris.diprad.recognition.models.letters;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.JShape;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class GModel {
	public static boolean recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		CircularObject co = CircularModel.recognize(bmw1);
		JShape jShape = null;
		if (co != null) {
			//System.out.println("11");
			if (!co.isFullCircle()) {
				//System.out.println("21");
				co = CircularModel.recognize(bmw2);
				if (co != null) {
					//System.out.println("31");
					if (!co.isFullCircle()) {
						//System.out.println("No full circles");
						return false;
					} else {
						jShape = JModel.recognize(bmw1);
					}
				} else {
					//System.out.println("No circles found aaa");
					return false;
				}
			} else {
				//System.out.println("22");
				jShape = JModel.recognize(bmw2);
			}
		} else {
			//System.out.println("12");
			co = CircularModel.recognize(bmw2);
			if (co != null) {
				if (!co.isFullCircle()) {
					//System.out.println("No full circles");
					return false;
				} else {
					jShape = JModel.recognize(bmw1);
				}
			} else {
				//System.out.println("No circular objects found");
				return false;
			}
		}

		if (jShape == null) {
			//System.out.println("No jshape found");
			return false;
		}

		if (jShape.isForF()) {
			//System.out.println("Shape was for f not g");
			return false;
		}

		double jWidthHeightRatio = jShape.getBoundingBox().getWidth() / jShape.getBoundingBox().getHeight();

		if (jWidthHeightRatio < 0.1 || jWidthHeightRatio > 0.7) {
			//System.out.println("J inapropriate jWidthHeightRatio: " + jWidthHeightRatio);
			return false;
		}

		double heightHeightRatio = co.getBoundingBox().getHeight() / jShape.getBoundingBox().getHeight();
		if (heightHeightRatio < 0.25 || heightHeightRatio > 0.7) {
			//System.out.println("Circle inapropriate height: " + heightHeightRatio);
			return false;
		}

		double widthHeightRatio = co.getBoundingBox().getWidth() / jShape.getBoundingBox().getHeight();
		if (widthHeightRatio < 0.25 || widthHeightRatio > 0.7) {
			//System.out.println("Circle inapropriate width: " + widthHeightRatio);
			return false;
		}

		double widthWidthRatio = co.getBoundingBox().getWidth() / jShape.getBoundingBox().getWidth();
		if (widthWidthRatio < 0.8 || widthWidthRatio > 2.3) {
			//System.out.println("Circle widthWidthRatio: " + widthWidthRatio);
			return false;
		}

		if (co.getMinMaxRatio() < 0.45) {
			//System.out.println("Circle not enought circle: " + co.getMinMaxRatio());
			return false;
		}

		if (co.getBoundingBox().getP1().y
				+ 0.2 * jShape.getBoundingBox().getHeight() <= jShape.getBoundingBox().getP1().y) {
			//System.out.println("Circle too high");
			return false;
		}
		if (co.getBoundingBox().getP2().y >= jShape.getBoundingBox().getP1().y
				+ 0.7 * jShape.getBoundingBox().getHeight()) {
			//System.out.println("Circle too low");
			return false;
		}

		double xAtAverageY = jShape.getL().forY(co.getAveragePoint().y);
		if (xAtAverageY - 0.15 * co.getBoundingBox().getWidth() < co.getAveragePoint().x) {
			//System.out.println("Circle too right");
			return false;
		}
		if (co.getAveragePoint().x + 0.55 * co.getBoundingBox().getWidth() < xAtAverageY) {
			//System.out.println("Circle too left");
			return false;
		}

		return true;
	}
}
