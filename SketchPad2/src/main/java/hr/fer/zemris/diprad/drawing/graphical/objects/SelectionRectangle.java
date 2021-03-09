package hr.fer.zemris.diprad.drawing.graphical.objects;

import java.awt.Point;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;

public class SelectionRectangle extends GraphicalObject {
	private Point start;
	private Point end;

	@Override
	public void accept(GraphicalObjectVisitor v) {
		v.visit(this);
	}

	public Point getStart() {
		return start;
	}

	public void setStart(Point start) {
		this.start = start;
	}

	public Point getEnd() {
		return end;
	}

	public void setEnd(Point end) {
		this.end = end;
	}

	@Override
	public boolean isInRect(int minX, int maxX, int minY, int maxY) {
		return false;
	}

	@Override
	public boolean youInterested(Point p) {
		return false;
	}

	@Override
	public void handleIntrest(Point point) {
	}

	@Override
	public String toString() {
		return "Rectangle:" + this.hashCode();
	}

	@Override
	public String print() {
		return "";
	}
}
