package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LinesAverageXDistanceTester extends LineCoordinateDistanceTester implements Tester<Line> {
	@Override
	public boolean test(Line l1, Line l2) {
		return l1.getAverageX() + avgLineLength * (-KTableModel.LINES_MIN_X_DISTANCE_SCALE) > (l2.getAverageX()
				+ avgLineLength * (KTableModel.LINES_MIN_X_DISTANCE_SCALE));
	}
}
