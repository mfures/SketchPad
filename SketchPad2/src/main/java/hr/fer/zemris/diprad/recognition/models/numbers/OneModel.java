package hr.fer.zemris.diprad.recognition.models.numbers;

import java.awt.Point;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class OneModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();
		List<Integer> acumulatedBreakPoints = LinearModel.acumulateBreakPointsWhichAreClose(points);

		if (acumulatedBreakPoints.size() != 3) {
			// System.out.println("Bad breakpoint size:" + acumulatedBreakPoints.size());
			return false;
		}

		List<Line> lines = LinearModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 2) {
			// System.out.println("Bad line number:" + lines.size());
			return false;
		}

		Line l1 = lines.get(0);
		// System.out.println(l1.getSlope());
		Line l2 = lines.get(1);
		// System.out.println(l2.getSlope());

		if (!(l1.getSlope() >= (-4.5) && l1.getSlope() <= -0.3 && Math.abs(l2.getSlope()) > 4.5)) {
			return false;
		}

		// System.out.println(l1.length() / l2.length());
		if (l1.length() / l2.length() > 0.85 || l1.length() / l2.length() < 0.15) {
			return false;
		}

		if (l1.getP2().y > l1.getP1().y) {
			return false;
		}
		if (l2.getP2().y < l2.getP2().y) {
			return false;
		}

		Point averagePoint = new Point();

		averagePoint.x = 0;
		averagePoint.y = 0;

		for (Point p1 : bmw.getBm().getPoints()) {
			averagePoint.x += p1.x;
			averagePoint.y += p1.y;
		}
		averagePoint.x = (int) Math.round(((double) averagePoint.x) / bmw.getBm().getPoints().size());
		averagePoint.y = (int) Math.round(((double) averagePoint.y) / bmw.getBm().getPoints().size());

		return true;
	}
}