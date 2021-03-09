package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineDistanceTester implements Tester<Line> {
	private static final double LINE_LENGTH_SCALE = 0.1;

	@Override
	public boolean test(Line l1, Line l2) {
		return l1.length() * (1 - LINE_LENGTH_SCALE) > (l2.length() * (1 + LINE_LENGTH_SCALE));
	}

}
