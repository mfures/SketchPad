package hr.fer.zemris.diprad.recognition.models.numbers;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class TwoModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Integer> breakPoints = LinearModel.acumulateBreakPointsWhichAreClose(bmw.getBm().getPoints());
		if (breakPoints.size() < 3) {
			System.out.println("Bad bp size");
			return false;
		}

		int bpSize = breakPoints.size();
		if (bmw.getBm().getPoints().get(breakPoints.get(bpSize - 2)).x >= bmw.getBm().getPoints()
				.get(breakPoints.get(bpSize - 1)).x) {
			System.out.println("Bad line");
			return false;
		}

		Line l = LinearModel.recognize(bmw, breakPoints.get(bpSize - 2), breakPoints.get(bpSize - 1));
		CircularObject co = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPoints.get(bpSize - 2), bmw);
		if (l.getType() != LineType.HORIZONTAL) {
			System.out.println("Line not horizonatl");
			return false;
		}

		if (co == null || l == null) {
			System.out.println("Some are null:" + co + " " + l);
			return false;
		}

		if (co.getMinMaxRatio() > 0.55 || co.getMinMaxRatio() < 0.15) {
			System.out.println("co minMax: " + co.getMinMaxRatio());
			return false;
		}

		if (co.getTotalAngle() < 220 || co.getTotalAngle() > 330) {
			System.out.println("invalid angle: " + co.getTotalAngle());
			return false;
		}

		if (co.getTheta() > 175 || co.getTheta() < 105) {
			System.out.println("Bad opening position: " + co.getTheta());
			return false;
		}

		double heightWidthRatio = co.getBoundingBox().getHeight() / l.getBoundingBox().getWidth();
		if (heightWidthRatio > 2.5 || heightWidthRatio < 0.7) {
			System.out.println("Bad heightWidthRatio: " + heightWidthRatio);
			return false;
		}

		double widthWidthRatio = co.getBoundingBox().getWidth() / l.getBoundingBox().getWidth();
		if (widthWidthRatio > 1.5 || widthWidthRatio < 0.4) {
			System.out.println("Bad widthWidthRatio: " + widthWidthRatio);
			return false;
		}

		return true;
	}
}
