package hr.fer.zemris.diprad.recognition;

import hr.fer.zemris.diprad.recognition.objects.Line;

public interface LineValueSupplier {
	double getValue(Line line);
}