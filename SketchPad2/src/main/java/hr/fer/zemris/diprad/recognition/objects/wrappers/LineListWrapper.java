package hr.fer.zemris.diprad.recognition.objects.wrappers;

import java.util.List;

import hr.fer.zemris.diprad.recognition.models.KTableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineListWrapper {
	public List<Line> lines;
	public double minLength;
	public double maxLength;
	public double avgCoordinateValue;

	public LineListWrapper(List<Line> lines, Boolean averageX) {
		if (lines.isEmpty()) {
			throw new RuntimeException("Lines empty.");
		}

		this.lines = lines;
		initLengths();
		if (averageX != null) {
			if (averageX == true) {
				avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageX()).average().getAsDouble();
			} else {
				avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageY()).average().getAsDouble();
			}
		}
	}

	private void initLengths() {
		double lCurrent = lines.get(0).length();
		double lMin = lCurrent, lMax = lCurrent;

		for (int j = 1; j < lines.size(); j++) {// No checks needed
			lCurrent = lines.get(j).length();
			if (lCurrent > lMax) {
				lMax = lCurrent;
			} else if (lCurrent < lMin) {
				lMin = lCurrent;
			}
		}

		minLength = lMax * KTableModel.LENGTH_MIN;
		maxLength = lMin * KTableModel.LENGTH_MAX;
	}
}
