package hr.fer.zemris.diprad.recognition.supliers;

import hr.fer.zemris.diprad.recognition.LineValueSupplier;
import hr.fer.zemris.diprad.recognition.objects.Line;

public class LineAverageXSupplier implements LineValueSupplier {
	@Override
	public double getValue(Line line) {
		return line.getAverageX();
	}

}
