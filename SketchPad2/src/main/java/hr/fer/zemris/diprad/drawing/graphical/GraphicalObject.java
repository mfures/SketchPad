package hr.fer.zemris.diprad.drawing.graphical;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.drawing.Colors;

public abstract class GraphicalObject {
	private Color color = Colors.DEFAULT;

	protected List<GraphicalObjectListener> listeners = new ArrayList<>();

	public abstract void accept(GraphicalObjectVisitor v);

	public void addGraphicalObjectListener(GraphicalObjectListener l) {
		listeners.add(l);
	}

	public void removeGraphicalObjectListener(GraphicalObjectListener l) {
		listeners.remove(l);
	}

	public abstract boolean isInRect(int minX, int maxX, int minY, int max);

	public abstract boolean youInterested(Point p);

	public abstract void handleIntrest(Point point);

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public abstract String print();
}
