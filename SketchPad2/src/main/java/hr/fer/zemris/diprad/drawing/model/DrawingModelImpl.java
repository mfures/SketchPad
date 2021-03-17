package hr.fer.zemris.diprad.drawing.model;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.diprad.actions.AppendAction.MyCounter;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;

public class DrawingModelImpl implements DrawingModel {
	private List<GraphicalObject> objects = new ArrayList<>();
	private List<DrawingModelListener> listeners = new ArrayList<>();
	private boolean modified;

	@Override
	public int getSize() {
		return objects.size();
	}

	@Override
	public GraphicalObject getObject(int index) {
		return objects.get(index);
	}

	@Override
	public void add(GraphicalObject object) {
		objects.add(object);
		object.addGraphicalObjectListener(this);
		modified = true;
		listeners.forEach(l -> l.objectsAdded(this, objects.size(), objects.size()));
	}

	@Override
	public void remove(GraphicalObject object) {
		int index = objects.indexOf(object);
		if (objects.remove(object)) {
			modified = true;
			listeners.forEach(l -> l.objectsRemoved(this, index, index));
		}
	}

	@Override
	public void changeOrder(GraphicalObject object, int offset) {
		int index = objects.indexOf(object);
		if (index + offset < 0 || (index + offset > objects.size() - 1))
			return;

		Collections.swap(objects, index, index + offset);
		modified = true;
		listeners.forEach(l -> l.objectsChanged(this, index, index + offset));
	}

	@Override
	public int indexOf(GraphicalObject object) {
		return objects.indexOf(object);
	}

	@Override
	public void clear() {
		if (!objects.isEmpty()) {
			int index = objects.size() - 1;
			objects.clear();
			modified = true;
			listeners.forEach(l -> l.objectsRemoved(this, 0, index));
		}
	}

	@Override
	public void clearModifiedFlag() {
		modified = false;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void addDrawingModelListener(DrawingModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeDrawingModelListener(DrawingModelListener l) {
		listeners.remove(l);
	}

	@Override
	public void graphicalObjectChanged(GraphicalObject o) {
		listeners.forEach(l -> l.objectsChanged(this, objects.indexOf(o), objects.indexOf(o)));
	}

	@Override
	public List<BasicMovementWrapper> getObjectsInRecti(int minX, int maxX, int minY, int maxY) {
		List<BasicMovementWrapper> list = new ArrayList<>();

		for (int i = 0; i < objects.size(); i++) {
			GraphicalObject g = objects.get(i);
			if (g instanceof BasicMovement) {
				if (g.isInRect(minX, maxX, minY, maxY)) {
					list.add(new BasicMovementWrapper((BasicMovement) g, i));
				}
			}

		}

		return list;
	}

	@Override
	public List<GraphicalObject> checkForInterest(MouseEvent e) {
		List<GraphicalObject> obj = new ArrayList<GraphicalObject>();
		for (GraphicalObject o : objects) {
			if (o.youInterested(e.getPoint())) {
				obj.add(o);
			}
		}

		return obj;
	}

	@Override
	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append("!\n");
		for (GraphicalObject go : objects) {
			sb.append(go.print());
			sb.append("\n");
		}

		return sb.toString();
	}

	@Override
	public String printLabeled(MyCounter mc) {
		StringBuilder sb = new StringBuilder();
		sb.append("!\n");
		for (GraphicalObject go : objects) {
			if (go instanceof BasicMovement) {
				if (!((BasicMovement) go).getLabel().equals("")) {
					sb.append(mc.c);
					sb.append("_");
					sb.append(go.print());
					sb.append("\n");
					mc.c++;
				}
			}
		}

		if (sb.toString().equals("!\n")) {
			return "";
		}

		return sb.toString();
	}
}
