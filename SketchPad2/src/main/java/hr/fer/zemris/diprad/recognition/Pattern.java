package hr.fer.zemris.diprad.recognition;

import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public interface Pattern<T> {
	public T recognize(BasicMovementWrapper bm);
}
