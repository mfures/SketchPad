package hr.fer.zemris.diprad.recognition.sorters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hr.fer.zemris.diprad.recognition.LineSorter;
import hr.fer.zemris.diprad.recognition.objects.Line;

/**
 * Sorter that sorts lines by ther smallest Y coordinate
 * 
 * @author Matej
 *
 */
public class CoordinateAverageYSorter implements LineSorter {
	@Override
	public void sort(List<Line> lines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				return Double.compare(o1.getAverageY(), o2.getAverageY());
			}
		});

	}
}
