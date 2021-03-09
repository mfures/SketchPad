package hr.fer.zemris.diprad.drawing.model;

import java.awt.event.MouseEvent;
import java.util.List;

import hr.fer.zemris.diprad.actions.AppendAction.MyCounter;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectListener;

public interface DrawingModel extends GraphicalObjectListener {
	public int getSize();

	public GraphicalObject getObject(int index);

	public void add(GraphicalObject object);

	public void remove(GraphicalObject object);

	public void changeOrder(GraphicalObject object, int offset);

	public int indexOf(GraphicalObject object);

	public void clear();

	public void clearModifiedFlag();

	public boolean isModified();

	public void addDrawingModelListener(DrawingModelListener l);

	public void removeDrawingModelListener(DrawingModelListener l);

	public List<GraphicalObject> getObjectsInRecti(int minX, int maxX, int minY, int maxY);

	public List<GraphicalObject> checkForInterest(MouseEvent e);

	public String print();

	public String printLabeled(MyCounter mc);
}
