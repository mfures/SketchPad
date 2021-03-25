package hr.fer.zemris.diprad.recognition.models.numbers;

import java.awt.Point;
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

//		CircularObject co = CircularModel.recognize(bmw);
//		if (co == null) {
//			//System.out.println("No circular object found");
//			return false;
//		}
//		if (!co.isFullCircle()) {
//			//System.out.println("Not ful circle");
//			return false;
//		}
//		if (co.getMinMaxRatio() > 0.65 || co.getMinMaxRatio() < 0.25) {
//			//System.out.println("Bad min max:" + co.getMinMaxRatio());
//		}
//		if (Math.abs(co.getThetaMaxDistance()) > 125 || Math.abs(co.getThetaMaxDistance()) < 55) {
//			//System.out.println("Bad angle:" + co.getThetaMaxDistance());
//			return false;
//		}

		return true;
	}
}
