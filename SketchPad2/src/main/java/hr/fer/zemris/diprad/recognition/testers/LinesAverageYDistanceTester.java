package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LinesAverageYDistanceTester extends LineCoordinateDistanceTester implements Tester<Line> {
	@Override
	public boolean test(Line l1, Line l2) {
		return l1.getAverageY() + avgLineLength * (-KTableModel.LINES_MIN_Y_DISTANCE_SCALE) > (l2.getAverageY()
				+ avgLineLength * (KTableModel.LINES_MIN_Y_DISTANCE_SCALE));
	}
}
