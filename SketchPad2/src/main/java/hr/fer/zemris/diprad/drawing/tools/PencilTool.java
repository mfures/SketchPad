package hr.fer.zemris.diprad.drawing.tools;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.drawing.Tool;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.drawing.model.JDrawingCanvas;

public class PencilTool implements Tool {
	private List<Point> points;
	private JDrawingCanvas canvas;
	private DrawingModel model;
	private BasicMovement bm;

	public PencilTool(JDrawingCanvas canvas, DrawingModel model) {
		this.canvas = canvas;
		this.model = model;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		points = new ArrayList<>();
		points.add(e.getPoint());
		bm = new BasicMovement(points);
		model.add(bm);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (bm != null) {
			bm.initBoundingBox();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		points.add(e.getPoint());
		canvas.repaint();
	}

	@Override
	public void paint(Graphics2D g2d) {
		// TODO AKO neće raditi!
	}

	@Override
	public void cleanUp() {
	}
}
