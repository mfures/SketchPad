package hr.fer.zemris.diprad.recognition;

import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;

public interface Pattern<T> {
	public T recognize(BasicMovement bm);
}
