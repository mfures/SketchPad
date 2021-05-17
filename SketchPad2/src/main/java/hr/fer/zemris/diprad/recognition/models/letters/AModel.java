package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;

public class AModel {
	private static final String A = "A";

	public static CharacterModel recognize(BasicMovementWrapper bmw1, BasicMovementWrapper bmw2) {
		//System.out.println("Test za A");
		List<Integer> bp1 = LinearModel.acumulateBreakPointsWhichAreClose(bmw1.getBm().getPoints());
		List<Integer> bp2 = LinearModel.acumulateBreakPointsWhichAreClose(bmw2.getBm().getPoints());

		if (!(bp1.size() == 3 && bp2.size() == 2)) {
			if (!(bp2.size() == 3 && bp1.size() == 2)) {
				 //System.out.println("Bad sizes: " + bp1.size() + " " + bp2.size());
				return null;
			}

			List<Integer> tmp = bp1;
			bp1 = bp2;
			bp2 = tmp;

			BasicMovementWrapper btmp = bmw1;
			bmw1 = bmw2;
			bmw2 = btmp;
		}

		List<Line> lines = LinearModel.linesInPoints(bmw1.getBm().getPoints(), bp1, bmw1);
		if (lines.size() != 2) {
			 //System.out.println("Bad lines");
			return null;
		}

		Line l1 = LinearModel.recognize(bmw2);
		if (l1 == null) {
			//System.out.println("Not a line");
			return null;
		}

		if (l1.getType() != LineType.HORIZONTAL) {
			 //System.out.println("Not horizontal");
			return null;
		}

		Line l01 = lines.get(0);
		Line l02 = lines.get(1);
		double maxLengthRatio = 1.7;
		double minLengthRatio = 1 / maxLengthRatio;
		double lengthRatio = l01.length() / l02.length();
		if (lengthRatio > maxLengthRatio || lengthRatio < minLengthRatio) {
			 //System.out.println("Bad length ratio: " + lengthRatio);
			return null;
		}

		if (l01.getSlope() > 0 && l02.getSlope() < 0) {
			Line tmp = l01;
			l01 = l02;
			l02 = tmp;
		}
		if (l01.getSlope() <= -25 || l01.getSlope() > -0.8) {
			 //System.out.println("Bad l01 slope: " + l01.getSlope());
			return null;
		}
		if (l02.getSlope() >= 25 || l02.getSlope() < 0.8) {
			 //System.out.println("Bad l02 slope: " + l02.getSlope());
			return null;
		}

		double baseLength = MyVector.norm(new Point(l01.getMinX(), l01.getMaxY()),
				new Point(l02.getMaxX(), l02.getMaxY()));
		if (l1.length() / baseLength > 2 || l1.length() / baseLength < 0.5) {
			 //System.out.println("Bad base length ratio: " + l1.length() / baseLength);
			return null;
		}

		double targetX1 = l01.forY(l1.getAverageY());
		double targetX2 = l02.forY(l1.getAverageY());
		double dist = targetX2 - targetX1;
		if (dist <= 0) {
			 //System.out.println("l1 to high");
			return null;
		}

		if (!(l1.getMinX() < (targetX1 + 0.20 * dist) && (l1.getMaxX() > (targetX2 - 0.20 * dist)))) {
			 //System.out.println("l1 doesnt cross both lines");
			return null;
		}
		if (l1.length() / dist > 3) {
			 //System.out.println("L1 too wide: " + l1.length() / dist);
			return null;
		}

		return new CharacterModel(A, bmw1,bmw2);
	}
}
