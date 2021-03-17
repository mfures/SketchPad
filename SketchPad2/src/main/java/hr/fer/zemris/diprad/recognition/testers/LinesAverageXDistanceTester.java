package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LinesAverageXDistanceTester extends LineDistanceTester {

	@Override
	public boolean test(Line l1, Line l2) {
		return l1.getAverageX() - minLength * (1 - KTableModel.COORDINATE_MIN) > (l2.getAverageX()
				+ maxLength * (KTableModel.COORDINATE_MAX) - 1);
	}
}
