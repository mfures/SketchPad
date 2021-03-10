package hr.fer.zemris.diprad.recognition.testers;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.objects.Line;

public abstract class LineCoordinateDistanceTester implements Tester<Line> {
	protected double avgLineLength;

	public void setAvgLineLength(double avgLineLength) {
		this.avgLineLength = avgLineLength;
	}
}
