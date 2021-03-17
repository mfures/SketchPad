package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.List;

import hr.fer.zemris.diprad.recognition.Pattern;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.One;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class OneModel implements Pattern<One> {
	@Override
	public One recognize(BasicMovementWrapper bmw) {

		List<Point> points = bmw.getBm().getPoints();

		List<Integer> breakPoints = LineModel.calculateBreakPoints(points);

		if (breakPoints.size() < 1) {
			return null;
		}

		List<Integer> acumulatedBreakPoints = LineModel.acumulateBreakPointsWhichAreClose(
				LineModel.calculateBreakPoints(bmw.getBm().getPoints()), bmw.getBm().getPoints().size());

		if (acumulatedBreakPoints.size() != 1) {
			return null;
		}

		List<Line> lines = LineModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 2) {
			return null;
		}

		Line l1 = lines.get(0);
		Line l2 = lines.get(1);

		if (l1.getSlope() >= (-5) && l1.getSlope() <= -0.3) {
			if (!(Math.abs(l2.getSlope()) > 5)) {
				return null;
			} else {
			}
		} else {
			if (l2.getSlope() >= (-5) && l2.getSlope() <= -0.3) {
				if (!(Math.abs(l1.getSlope()) > 5)) {
					return null;
				} else {
					Line tmp = l1;
					l1 = l2;
					l2 = tmp;
				}
			} else
				return null;
		}

		if (l1.length() < l2.length()) {
			Point averagePoint = new Point();

			averagePoint.x = 0;
			averagePoint.y = 0;

			for (Point p1 : bmw.getBm().getPoints()) {
				averagePoint.x += p1.x;
				averagePoint.y += p1.y;
			}
			averagePoint.x = (int) Math.round(((double) averagePoint.x) / bmw.getBm().getPoints().size());
			averagePoint.y = (int) Math.round(((double) averagePoint.y) / bmw.getBm().getPoints().size());

			return new One(averagePoint, l2.length() / 2);
		}

		return null;
	}
}
