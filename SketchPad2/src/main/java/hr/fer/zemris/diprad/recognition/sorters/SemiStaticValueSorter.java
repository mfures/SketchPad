package hr.fer.zemris.diprad.recognition.sorters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hr.fer.zemris.diprad.recognition.LineSorter;
import hr.fer.zemris.diprad.recognition.objects.Line;

/**
 * Sorter that sorts values by their SemiStatic value
 * 
 * @author Matej
 *
 */
public class SemiStaticValueSorter implements LineSorter {
	@Override
	public void sort(List<Line> lines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				return Double.compare(o1.getSemiStaticValue(), o2.getSemiStaticValue());
			}
		});

	}
}
