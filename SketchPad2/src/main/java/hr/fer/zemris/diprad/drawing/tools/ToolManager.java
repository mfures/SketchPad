package hr.fer.zemris.diprad.drawing.tools;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.List;

import hr.fer.zemris.diprad.drawing.Tool;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.drawing.model.JDrawingCanvas;

public class ToolManager implements Tool {
	private Tool activeTool;
	private boolean newClick = false;
	private DrawingModel model;
	private JDrawingCanvas canvas;

	public ToolManager(DrawingModel model) {
		this.model = model;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		List<GraphicalObject> objects = model.checkForInterest(e);
		if (objects.isEmpty()) {
			newClick = true;
			activeTool.mousePressed(e);
		} else {
			for (GraphicalObject o : objects) {
				o.handleIntrest(e.getPoint());
			}
			canvas.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (newClick) {
			activeTool.mouseReleased(e);
		}
		newClick = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (newClick) {
			activeTool.mouseClicked(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		activeTool.mouseMoved(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (newClick) {
			activeTool.mouseDragged(e);
		}
	}

	@Override
	public void paint(Graphics2D g2d) {
		activeTool.paint(g2d);

	}

	@Override
	public void cleanUp() {
	}

	public void setActiveTool(Tool activeTool) {
		if (newClick) {
			this.activeTool.cleanUp();
		}
		newClick = false;
		this.activeTool = activeTool;
	}

	public void init(Tool activeTool) {
		this.activeTool = activeTool;
	}

	public void setCanvas(JDrawingCanvas canvas) {
		this.canvas = canvas;

	}
}
