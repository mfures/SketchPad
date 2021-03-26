package hr.fer.zemris.diprad.recognition.objects;

import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class JShape {
	private CircularObject co;
	private Line l;
	private boolean forF;
	private Rectangle boundingBox;

	public JShape(CircularObject co, Line l, boolean forF) {
		this.co = co;
		this.l = l;
		this.forF = forF;

		initBoundingBox();
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

	public void initBoundingBox() {
		double minX = Math.min(co.getBoundingBox().getP1().x, l.getBoundingBox().getP1().x);
		double minY = Math.min(co.getBoundingBox().getP1().y, l.getBoundingBox().getP1().y);
		double maxX = Math.max(co.getBoundingBox().getP2().x, l.getBoundingBox().getP2().x);
		double maxY = Math.max(co.getBoundingBox().getP2().y, l.getBoundingBox().getP2().y);

		this.boundingBox = new Rectangle(new PointDouble(minX, minY), new PointDouble(maxX, maxY));
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}
}
