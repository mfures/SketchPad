package hr.fer.zemris.diprad.recognition.models.numbers;

import java.awt.Point;
import java.util.ArrayList;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.BreakPointsUtil;
import hr.fer.zemris.diprad.recognition.models.CharacterModel;
import hr.fer.zemris.diprad.recognition.models.LinearModel;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class OneModel {
	private static final String ONE = "1";

	public static CharacterModel recognize(BasicMovementWrapper bmw) {
		List<Point> points = bmw.getBm().getPoints();

		int bp = BreakPointsUtil.calculateBestBreakPoint(bmw.getBm().getPoints());
		//System.out.println("Našo: " + bp);
		
		if(bp<=0||bp>=bmw.getBm().getPoints().size()-1) {
			//System.out.println("No break pointŁ");
			return null;
		}
		
		List<Integer> acumulatedBreakPoints = new ArrayList<>();
		acumulatedBreakPoints.add(0);
		acumulatedBreakPoints.add(bp);
		acumulatedBreakPoints.add(bmw.getBm().getPoints().size()-1);

		

		List<Line> lines = LinearModel.linesInPoints(points, acumulatedBreakPoints, bmw);

		if (lines.size() != 2) {
			//System.out.println("Bad line number:" + lines.size());
			return null;
		}

		Line l1 = lines.get(0);
		// System.out.println(l1.getSlope());
		Line l2 = lines.get(1);
		// System.out.println(l2.getSlope());

		if (!(l1.getSlope() >= (-4.5) && l1.getSlope() <= -0.3 && Math.abs(l2.getSlope()) > 4.5)) {
			//System.out.println("1");
			return null;
		}

		// System.out.println(l1.length() / l2.length());
		if (l1.length() / l2.length() > 0.9 || l1.length() / l2.length() < 0.15) {
			//System.out.println("ratio: "+l1.length()/l2.length());
			return null;
		}

		if (l1.getP2().y > l1.getP1().y) {
			//System.out.println("3");
			return null;
		}
		if (l2.getP2().y < l2.getP2().y) {
//			System.out.println("4");
			return null;
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

		return new CharacterModel(ONE, bmw);
	}
}
