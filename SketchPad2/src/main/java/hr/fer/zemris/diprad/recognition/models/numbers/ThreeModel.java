package hr.fer.zemris.diprad.recognition.models.numbers;

import hr.fer.zemris.diprad.recognition.models.BreakPointsUtil;
import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ThreeModel {
	private static final String THREE = "3";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		int breakPosition = BreakPointsUtil.calculateBestBreakPoint(bmw.getBm().getPoints());
		// System.out.println("Našo: " + bp);

		if (breakPosition <= 0 || breakPosition >= bmw.getBm().getPoints().size() - 1) {
			// System.out.println("No break pointŁ");
			return null;
		}

		CircularObject co1, co2;
		int deltaX;
		if (bmw.getBm().getPoints().get(0).y < bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).y) {
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), breakPosition, bmw.getBm().getPoints().size() - 1,
					bmw);
			deltaX = bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x
					- bmw.getBm().getPoints().get(0).x;
		} else {
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), breakPosition, bmw.getBm().getPoints().size() - 1,
					bmw);
			deltaX = -(bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).x
					- bmw.getBm().getPoints().get(0).x);
		}

		if (co1 == null || co2 == null) {
			// System.out.println("Some are not circular");
			return null;
		}

		double avgNorm = (co1.getTotalNorm() + co2.getTotalNorm()) / 2;
		// Jesu li prošli srednju točku s lijeve strane
		if (bmw.getBm().getPoints().get(0).x > bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.2) {
			// System.out.println("1");
			return null;
		}
		if (bmw.getBm().getPoints().get(
				bmw.getBm().getPoints().size() - 1).x > bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.2) {
			// System.out.println("2");
			return null;
		}
		// Jesu li prošli srednju točku s desne strane
		if (co1.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.2) {
			// System.out.println("11");
			return null;
		}
		if (co2.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.2) {
			// System.out.println("22");
			return null;
		}

		// je li donja točka dalja od gornje
		if (deltaX - 0.35 * avgNorm > 0) {
			// System.out.println("3. 1 point to far left in comparioson to last");
			return null;
		}

		if (!(angleAndMinMaxTest(co1) && angleAndMinMaxTest(co2))) {
			return null;
		}

		if (!(co1.getTheta() > 95 || co1.getTheta() < -135)) {
			// System.out.println("1Bad opening position: " + co1.getTheta());
			return null;
		}

		if (!(co2.getTheta() > 110 || co2.getTheta() < -95)) {
			// System.out.println("2Bad opening position: " + co2.getTheta());
			return null;
		}

		if (co2.getBoundingBox().getWidth() < co1.getBoundingBox().getWidth() * 0.35) {
			// System.out.println("Upper part too wide");
			return null;
		}
		if (co2.getBoundingBox().getWidth() > co1.getBoundingBox().getWidth() * 3.5) {
			// System.out.println(co2.getBoundingBox().getWidth() /
			// co1.getBoundingBox().getWidth());
			// System.out.println("Lower part too wide");
			return null;
		}
		if (co2.getBoundingBox().getHeight() < co1.getBoundingBox().getHeight() * 0.2) {
			// System.out.println("Upper part too high");
			return null;
		}
		if (co2.getBoundingBox().getHeight() > co1.getBoundingBox().getHeight() * 3.1) {
			// System.out.println("Lower part too high");
			return null;
		}
		if (co2.getBoundingBox().getP2().x < co1.getBoundingBox().getP2().x - 0.65 * avgNorm) {
			// System.out.println("1111");
			return null;
		}

		return new CharacterModel(THREE, bmw);
	}

	private static boolean angleAndMinMaxTest(CircularObject co1) {
		if (co1.getTotalAngle() < 205 || co1.getTotalAngle() > 345) {
			// System.out.println("invalid angle: " + co1.getTotalAngle());
			return false;
		}
		if (co1.getMinMaxRatio() > 0.8) {
			// System.out.println("Bad min max:" + co1.getMinMaxRatio());
			return false;
		}

		return true;
	}
}
