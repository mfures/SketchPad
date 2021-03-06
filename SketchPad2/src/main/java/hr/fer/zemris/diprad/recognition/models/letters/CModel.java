package hr.fer.zemris.diprad.recognition.models.letters;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class CModel {
	private static final String C = "C";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		CircularObject co = CircularModel.recognize(bmw);
		if (co == null) {
			//System.out.println("No circular object found");
			return null;
		}
		if (co.getTotalAngle() < 215 || co.getTotalAngle() > 325) {
			//System.out.println("invalid angle: " + co.getTotalAngle());
			return null;
		}
		if (co.getMinMaxRatio() > 0.65) {
			//System.out.println("Bad min max:" + co.getMinMaxRatio());
			return null;
		}

		if (co.getTheta() > 50 || co.getTheta() < -50) {
			//System.out.println("Bad opening position: " + co.getTheta());
			return null;
		}

		double widthHeightRatio = co.getBoundingBox().getWidth() / co.getBoundingBox().getHeight();
		if (widthHeightRatio > 2.5 || widthHeightRatio < 0.35) {
			//System.out.println("Bad widthHeight: " + widthHeightRatio);
			return null;
		}

		return new CharacterModel(C, bmw);
	}
}
