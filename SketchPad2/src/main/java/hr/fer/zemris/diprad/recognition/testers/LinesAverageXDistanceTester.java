package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LinesAverageXDistanceTester implements Tester<Line> {
	@Override
	public boolean test(Line l1, Line l2) {
		return l1.getAverageX() * (KTableModel.COORDINATE_MIN) > (l2.getAverageX() * (KTableModel.COORDINATE_MAX));
	}
}
