package hr.fer.zemris.diprad.recognition.models.numbers;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ThreeModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		int breakPosition = CircularModel.generateBestBreakPoint(bmw);

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
			return false;
		}

		double avgNorm = (co1.getTotalNorm() + co2.getTotalNorm()) / 2;
		// Jesu li prošli srednju točku s lijeve strane
		if (bmw.getBm().getPoints().get(0).x > bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			// System.out.println("1");
			return false;
		}
		if (bmw.getBm().getPoints().get(
				bmw.getBm().getPoints().size() - 1).x > bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			// System.out.println("2");
			return false;
		}
		// Jesu li prošli srednju točku s desne strane
		if (co1.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			// System.out.println("11");
			return false;
		}
		if (co2.getBoundingBox().getP2().x < bmw.getBm().getPoints().get(breakPosition).x + avgNorm * 0.05) {
			// System.out.println("22");
			return false;
		}

		// je li donja točka dalja od gornje
		if (deltaX - 0.1 * avgNorm > 0) {
			// System.out.println("3. 1 point to far left in comparioson to last");
			return false;
		}

		if (!(angleAndMinMaxTest(co1) && angleAndMinMaxTest(co2))) {
			return false;
		}

		if (!(co1.getTheta() > 120 || co1.getTheta() < -155)) {
			// System.out.println("1Bad opening position: " + co1.getTheta());
			return false;
		}

		if (!(co2.getTheta() > 135 || co2.getTheta() < -120)) {
			// System.out.println("2Bad opening position: " + co2.getTheta());
			return false;
		}

		if (co2.getBoundingBox().getWidth() < co1.getBoundingBox().getWidth() * 0.8) {
			// System.out.println("Upper part too wide");
			return false;
		}
		if (co2.getBoundingBox().getWidth() > co1.getBoundingBox().getWidth() * 2.1) {
			// System.out.println(co2.getBoundingBox().getWidth() /
			// co1.getBoundingBox().getWidth());
			// System.out.println("Lower part too wide");
			return false;
		}
		if (co2.getBoundingBox().getHeight() < co1.getBoundingBox().getHeight() * 0.8) {
			// System.out.println("Upper part too high");
			return false;
		}
		if (co2.getBoundingBox().getHeight() > co1.getBoundingBox().getHeight() * 2.5) {
			// System.out.println("Lower part too high");
			return false;
		}
		if (co2.getBoundingBox().getP2().x < co1.getBoundingBox().getP2().x - 0.2 * avgNorm) {
			return false;
		}

		return true;
	}

	private static boolean angleAndMinMaxTest(CircularObject co1) {
		if (co1.getTotalAngle() < 240 || co1.getTotalAngle() > 305) {
			// System.out.println("invalid angle: " + co1.getTotalAngle());
			return false;
		}
		if (co1.getMinMaxRatio() > 0.55 || co1.getMinMaxRatio() < 0.18) {
			// System.out.println("Bad min max:" + co1.getMinMaxRatio());
			return false;
		}

		return true;
	}
}
