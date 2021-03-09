package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LinesMinXDistanceTester extends LineCoordinateDistanceTester implements Tester<Line> {
	private static final double LINES_MIN_X_DISTANCE_SCALE = 0.05;

	@Override
	public boolean test(Line l1, Line l2) {
		return l1.getMinX() + avgLineLength * (-LINES_MIN_X_DISTANCE_SCALE) > (l2.getMinX()
				+ avgLineLength * (LINES_MIN_X_DISTANCE_SCALE));
	}
}
