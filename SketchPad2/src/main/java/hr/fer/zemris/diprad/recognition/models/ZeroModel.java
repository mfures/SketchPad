package hr.fer.zemris.diprad.recognition.models;

import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ZeroModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			//System.out.println("No circular object found");
			return false;
		}
		if (!co.isFullCircle()) {
			//System.out.println("Not ful circle");
			return false;
		}
		if (co.getMinMaxRatio() > 0.65 || co.getMinMaxRatio() < 0.25) {
			//System.out.println("Bad min max:" + co.getMinMaxRatio());
		}
		if (Math.abs(co.getThetaMaxDistance()) > 125 || Math.abs(co.getThetaMaxDistance()) < 55) {
			//System.out.println("Bad angle:" + co.getThetaMaxDistance());
			return false;
		}

		return true;
	}
}
