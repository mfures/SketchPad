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

public class GModel {
	private static final String G = "g";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		int breakPosition = CircularModel.generateBestBreakPoint(bmw);
		if (breakPosition == -1) {
			// System.out.println("Bad position " + breakPosition);
		}

		CircularObject co1 = CircularModel.recognize(bmw.getBm().getPoints(), 0, breakPosition, bmw);
		if (co1 == null) {
			// System.out.println("1.Null je");
			return null;
		}
		if (co1.getTotalAngle() < 295) {
			// System.out.println(co1.getTotalAngle());
			// System.out.println("Not full circle");
			return null;
		}

		List<Integer> segmentIndexes = toSegments(bmw.getBm().getPoints(), breakPosition);
		if (segmentIndexes == null) {
			return null;
		}

		CircularObject co = CircularModel.recognize(bmw.getBm().getPoints(), segmentIndexes.get(1),
				segmentIndexes.get(2), bmw);
		if (co == null) {
			// System.out.println("2.Null je");
			return null;
		}

		Line l1 = LinearModel.recognize(bmw, segmentIndexes.get(0), segmentIndexes.get(1));
		Line l2 = LinearModel.recognize(bmw, segmentIndexes.get(2), segmentIndexes.get(3));
		if (l1 == null || l2 == null) {
			// System.out.println(l1 + " " + l2);
			return null;
		}
		if (l1.getType() != LineType.VERTICAL) {
			// System.out.println("Not vertical");
			return null;
		}
		if (l2.getSlope() > 0.2 || l2.getSlope() < -2) {
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

		double widthWidthRatio = co.getBoundingBox().getWidth() / co1.getBoundingBox().getWidth();
		double heightHeightRatio = co.getBoundingBox().getHeight() / co1.getBoundingBox().getHeight();
		if (widthWidthRatio < 0.2 || widthWidthRatio > 4.5) {
			// System.out.println("ww:" + widthWidthRatio);
			return null;
		}
		if (heightHeightRatio < 0.2 || heightHeightRatio > 4.5) {
			// System.out.println("hh:" + heightHeightRatio);
			return null;
		}

		if (co1.getBoundingBox().getP2().y > co.getBoundingBox().getP1().y) {
			return null;
		}

		return new CharacterModel(G, bmw);
	}

	private static List<Integer> toSegments(List<Point> points, int start) {
		for (int i = start, j = points.size() - 1; i < j;) {
			if (points.get(i).y < points.get(j).y) {
				i++;
			} else if (points.get(i).x < points.get(j).x) {
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
