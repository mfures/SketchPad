package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineDistanceTester implements Tester<Line> {
	@Override
	public boolean test(Line l1, Line l2) {
		return l1.length() * (1 - KTableModel.LINE_LENGTH_SCALE) > (l2.length() * (1 + KTableModel.LINE_LENGTH_SCALE));
	}

}
