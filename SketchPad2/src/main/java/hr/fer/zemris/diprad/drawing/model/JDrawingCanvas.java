package hr.fer.zemris.diprad.drawing.model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import hr.fer.zemris.diprad.drawing.Tool;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;
import hr.fer.zemris.diprad.drawing.visitors.GraphicalObjectPainter;

public class JDrawingCanvas extends JComponent implements DrawingModelListener {
	private static final long serialVersionUID = -2231128936670103930L;
	private DrawingModel model;
	private Tool toolManager;

	public JDrawingCanvas(DrawingModel model, Tool toolManager) {
		this.model = model;
		model.addDrawingModelListener(this);
		this.toolManager = toolManager;

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toolManager.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				toolManager.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				toolManager.mouseReleased(e);
			}
		});

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				toolManager.mouseMoved(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				toolManager.mouseDragged(e);
			}
		});
	}

	@Override
	public void objectsAdded(DrawingModel source, int index0, int index1) {
		repaint();
		toolManager.paint((Graphics2D) getGraphics());
	}

	@Override
	public void objectsRemoved(DrawingModel source, int index0, int index1) {
		repaint();
		toolManager.paint((Graphics2D) getGraphics());
	}

	@Override
	public void objectsChanged(DrawingModel source, int index0, int index1) {
		repaint();
		toolManager.paint((Graphics2D) getGraphics());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		GraphicalObjectVisitor painter = new GraphicalObjectPainter(g2d);

		for (int i = 0; i < model.getSize(); i++) {
			model.getObject(i).accept(painter);
		}

		toolManager.paint(g2d);
	}
}
