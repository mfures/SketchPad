package hr.fer.zemris.diprad.recognition.models;

import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class CModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			System.out.println("No circular object found");
			return false;
		}
		if (co.getTotalAngle() < 240 || co.getTotalAngle() > 305) {
			System.out.println("invalid angle: " + co.getTotalAngle());
			return false;
		}
		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.25) {
			System.out.println("Bad min max:" + co.getMinMaxRatio());
			return true;
		}

		if (co.getTheta() > 35 || co.getTheta() < -35) {
			System.out.println("Bad opening position: " + co.getTheta());
			return false;
		}

		return true;
	}
}
