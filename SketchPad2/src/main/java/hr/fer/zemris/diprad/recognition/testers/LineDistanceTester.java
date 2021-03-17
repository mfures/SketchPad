package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.objects.Line;

public abstract class LineDistanceTester implements Tester<Line> {
	double minLength, maxLength;

	public void setLengths(double minLength, double maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
}
