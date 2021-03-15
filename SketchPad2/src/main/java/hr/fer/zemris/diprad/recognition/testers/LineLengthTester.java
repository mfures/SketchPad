package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineLengthTester implements Tester<Line> {
	@Override
	public boolean test(Line l1, Line l2) {
		return l1.length() * KTableModel.LENGTH_MIN > (l2.length() * KTableModel.LENGTH_MAX);
	}

}
