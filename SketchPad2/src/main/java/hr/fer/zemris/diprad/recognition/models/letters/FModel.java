package hr.fer.zemris.diprad.recognition.models.letters;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.CircularModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class FModel {
	private static final String F = "f";

	public static CharacterModel recognize(BasicMovementWrapper bmw, BasicMovementWrapper bmw2) {
		Line lHorizontal = LinearModel.recognize(bmw2);
		if (lHorizontal == null) {
			// System.out.println("Lh is null");
			return null;
		}
		if (lHorizontal.getType() != LineType.HORIZONTAL) {
			// System.out.println("Not horizontal");
			return null;
		}

		List<Integer> segmentIndexes = toSegments(bmw.getBm().getPoints(), 0);
		if (segmentIndexes == null) {
			return null;
		}

		CircularObject co = CircularModel.recognize(bmw.getBm().getPoints(), segmentIndexes.get(1),
				segmentIndexes.get(2), bmw);
		if (co == null) {
			// System.out.println("2.Null je");
			return null;
		}

		Line l2 = LinearModel.recognize(bmw, segmentIndexes.get(0), segmentIndexes.get(1));
		Line l1 = LinearModel.recognize(bmw, segmentIndexes.get(2), segmentIndexes.get(3));
		if (l1 == null || l2 == null) {
			// System.out.println(l1 + " " + l2);
			return null;
		}
		if (l1.getType() != LineType.VERTICAL) {
			// System.out.println("Not vertical");
			return null;
		}
		if (l2.getSlope() > 0.2 || l2.getSlope() < -15) {
			// System.out.println("l2 bad slope: " + l2.getSlope());
			return null;
		}
		if (l1.length() * 1.1 < l2.length() || l2.length() * 25 < l1.length()) {
			// System.out.println("Bad lengths: " + l1.length() + " " + l2.length());
			return null;
		}
		if (co.getBoundingBox().getP1().y > l1.getMaxY()) {
			return null;
		}

		if (lHorizontal.getAverageY() < l2.getMaxY()) {
			// System.out.println("To high");
			return null;
		}
		if (lHorizontal.getAverageY() > l1.getMaxY() - 0.25 * l1.length()) {
			// System.out.println("To low");
			return null;
		}

		double xAtAverageY = l1.forY(lHorizontal.getAverageY());
		double forcedPassedLength = 0.05 * l1.length();
		if (lHorizontal.getMinX() + forcedPassedLength > xAtAverageY
				|| lHorizontal.getMaxX() - forcedPassedLength < xAtAverageY) {
			// System.out.println("Horisontal line doesnt cross");
			return null;
		}

		double widthWidthRatio = lHorizontal.length() / bmw.getBm().getBoundingBox().getWidth();
		if (widthWidthRatio < 0.2 || widthWidthRatio > 4) {
			// System.out.println("ww:" + widthWidthRatio);
			return null;
		}

		return new CharacterModel(F, bmw, bmw2);
	}

	private static List<Integer> toSegments(List<Point> points, int start) {
		for (int i = start, j = points.size() - 1; i < j;) {
			if (points.get(i).x < points.get(j).x) {
				i++;
			} else if (points.get(i).y < points.get(j).y) {
				j--;
			} else {
				if (i < 2 + start || j > points.size() - 3) {
					return null;
				}

				List<Integer> list = new ArrayList<>();
				list.add(start);
				list.add(i - 1);
				list.add(j + 1);
				list.add(points.size() - 1);
				return list;
			}
		}

		return null;
	}
}
