package hr.fer.zemris.diprad.recognition.objects;

import java.util.List;

public class LineListWrapper {
	public List<Line> lines;
	public double avgLength;
	public double avgCoordinateValue;

	public LineListWrapper(List<Line> lines, Boolean averageX) {
		this.lines = lines;
		avgLength = lines.stream().mapToDouble(l -> l.length()).average().getAsDouble();
		if (averageX != null) {
			if (averageX == true) {
				avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageX()).average().getAsDouble();
			} else {
				avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageY()).average().getAsDouble();
			}
		}
	}
}
