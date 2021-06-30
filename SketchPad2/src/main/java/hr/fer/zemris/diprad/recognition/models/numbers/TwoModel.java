package hr.fer.zemris.diprad.recognition.models.numbers;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.BreakPointsUtil;
import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class TwoModel {
	private static final String TWO = "2";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		int bp = BreakPointsUtil.calculateBestBreakPoint(bmw.getBm().getPoints());
		//System.out.println("Našo: " + bp);
		
		if(bp<=0||bp>=bmw.getBm().getPoints().size()-1) {
			//System.out.println("No break pointŁ");
			return null;
		}

		Line l = LinearModel.recognize(bmw, bp, bmw.getBm().getPoints().size()-1);
		CircularObject co = CircularModel.recognize(bmw.getBm().getPoints(), 0, bp, bmw);
		if (co == null || l == null) {
			// System.out.println("Some are null:" + co + " " + l);
			return null;
		}
		if (l.getType() != LineType.HORIZONTAL) {
			// System.out.println("Line not horizonatl");
			return null;
		}
		if (co.getMinMaxRatio() > 0.65) {
			// System.out.println("co minMax: " + co.getMinMaxRatio());
			return null;
		}

		if (co.getTotalAngle() < 210 || co.getTotalAngle() > 340) {
			// System.out.println("invalid angle: " + co.getTotalAngle());
			return null;
		}

		if (co.getTheta() > 185 || co.getTheta() < 95) {
			// System.out.println("Bad opening position: " + co.getTheta());
			return null;
		}

		double heightWidthRatio = co.getBoundingBox().getHeight() / l.getBoundingBox().getWidth();
		if (heightWidthRatio > 3 || heightWidthRatio < 0.5) {
			// System.out.println("Bad heightWidthRatio: " + heightWidthRatio);
			return null;
		}

		double widthWidthRatio = co.getBoundingBox().getWidth() / l.getBoundingBox().getWidth();
		if (widthWidthRatio > 2.1 || widthWidthRatio < 0.2) {
			// System.out.println("Bad widthWidthRatio: " + widthWidthRatio);
			return null;
		}

		return new CharacterModel(TWO, bmw);
	}
}
