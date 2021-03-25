package hr.fer.zemris.diprad.recognition.models.numbers;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ThreeModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Integer> breakPoints = CircularModel.generateAcumulatedBreakPoints(bmw);
		if (breakPoints.size() != 3) {
			System.out.println("Bad number of break points: " + breakPoints.size());
			return false;
		}
		CircularObject co1, co2;
		if (bmw.getBm().getPoints().get(0).y < bmw.getBm().getPoints().get(bmw.getBm().getPoints().size() - 1).y) {
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPoints.get(1), bmw);
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), breakPoints.get(1), breakPoints.get(2), bmw);
		} else {
			co2 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPoints.get(1), bmw);
			co1 = CircularModel.recognize(bmw.getBm().getPoints(), breakPoints.get(1), breakPoints.get(2), bmw);
		}

		if (co1 == null || co2 == null) {
			System.out.println("Some are not circular");
			return false;
		}

		if (!(angleAndMinMaxTest(co1) && angleAndMinMaxTest(co2))) {
			return false;
		}

		System.out.println("c1:" + co1.getTheta());
		if (!(co1.getTheta() > 130 || co1.getTheta() < -145)) {
			System.out.println("Bad opening position: " + co1.getTheta());
			return false;
		}

		System.out.println("c2:" + co2.getTheta());
		if (!(co2.getTheta() > 145 || co2.getTheta() < -130)) {
			System.out.println("Bad opening position: " + co2.getTheta());
			return false;
		}
		return true;
	}

	private static boolean angleAndMinMaxTest(CircularObject co1) {
		if (co1.getTotalAngle() < 240 || co1.getTotalAngle() > 305) {
			System.out.println("invalid angle: " + co1.getTotalAngle());
			return false;
		}
		if (co1.getMinMaxRatio() > 0.55 || co1.getMinMaxRatio() < 0.18) {
			System.out.println("Bad min max:" + co1.getMinMaxRatio());
			return false;
		}

		return true;
	}
}
