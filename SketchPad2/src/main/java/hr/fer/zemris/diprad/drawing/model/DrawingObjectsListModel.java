package hr.fer.zemris.diprad.drawing.model;

import javax.swing.AbstractListModel;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;

public class DrawingObjectsListModel extends AbstractListModel<GraphicalObject> implements DrawingModelListener {
	private static final long serialVersionUID = 2388806121849013771L;
	private DrawingModel drawingModel;

	/**
	 * Constructor
	 * 
	 * @param drawingModel to track
	 */
	public DrawingObjectsListModel(DrawingModel drawingModel) {
		this.drawingModel = drawingModel;
		drawingModel.addDrawingModelListener(this);
	}

	@Override
	public int getSize() {
		return drawingModel.getSize();
	}

	@Override
	public GraphicalObject getElementAt(int index) {
		return drawingModel.getObject(index);
	}

	@Override
	public void objectsAdded(DrawingModel source, int index0, int index1) {
		fireIntervalAdded(source, index0, index1);
	}

	@Override
	public void objectsRemoved(DrawingModel source, int index0, int index1) {
		fireIntervalRemoved(source, index0, index1);
	}

	@Override
	public void objectsChanged(DrawingModel source, int index0, int index1) {
		fireContentsChanged(source, index0, index1);
	}

}
