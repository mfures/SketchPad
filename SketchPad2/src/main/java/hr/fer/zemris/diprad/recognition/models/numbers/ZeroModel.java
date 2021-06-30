package hr.fer.zemris.diprad.recognition.models.numbers;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class ZeroModel {
	private static final String ZERO = "0";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			// System.out.println("No circular object found");
			return null;
		}
		if (!co.isFullCircle()) {
			// System.out.println("Not ful circle");
			return null;
		}
		if (co.getMinMaxRatio() > 0.8) {
			// System.out.println("Bad min max:" + co.getMinMaxRatio());
		}
		if (Math.abs(co.getThetaMaxDistance()) > 145 || Math.abs(co.getThetaMaxDistance()) < 35) {
			// System.out.println("Bad angle:" + co.getThetaMaxDistance());
			return null;
		}

		return new CharacterModel(ZERO, bmw);
	}
}
