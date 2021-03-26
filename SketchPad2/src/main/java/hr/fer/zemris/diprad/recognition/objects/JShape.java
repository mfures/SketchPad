package hr.fer.zemris.diprad.recognition.objects;

public class JShape {
	private CircularObject co;
	private Line l;
	private boolean forF;

	public JShape(CircularObject co, Line l, boolean forF) {
		this.co = co;
		this.l = l;
		this.forF = forF;
	}

	public CircularObject getCo() {
		return co;
	}

	public Line getL() {
		return l;
	}

	public boolean isForF() {
		return forF;
	}
}
