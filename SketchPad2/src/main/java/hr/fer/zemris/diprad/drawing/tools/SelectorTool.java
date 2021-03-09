package hr.fer.zemris.diprad.drawing.tools;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.Tool;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.recognition.models.KTableModel;

public class SelectorTool implements Tool {
	private SelectionRectangle rectangle;
	private SketchPad2 sP;

	public SelectorTool(SketchPad2 sP) {
		this.sP = sP;
		rectangle = new SelectionRectangle();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		rectangle.setStart(e.getPoint());
		rectangle.setEnd(e.getPoint());
		sP.getModel().add(rectangle);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		cleanUp();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		rectangle.setEnd(e.getPoint());
		sP.getCanvas().repaint();

	}

	@Override
	public void paint(Graphics2D g2d) {
		// TODO AKO neće raditi!
	}

	@Override
	public void cleanUp() {
		sP.getModel().remove(rectangle);
		(new KTableModel(sP)).recognize(rectangle.getStart(), rectangle.getEnd());
	}
}