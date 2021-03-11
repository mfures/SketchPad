package hr.fer.zemris.diprad.drawing.visitors;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import hr.fer.zemris.diprad.drawing.Colors;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable.Value;

public class GraphicalObjectPainter implements GraphicalObjectVisitor {
	private Point a;
	private Point b;
	private Point c;
	private Image img0;
	private Image img1;
	private Image imgx;

	private Graphics2D g2d;

	public GraphicalObjectPainter(Graphics2D g2d) {
		c = new Point();
		this.g2d = g2d;
		try {
			img0 = ImageIO.read(getClass().getResourceAsStream("/zero.png"));
			img1 = ImageIO.read(getClass().getResourceAsStream("/one.png"));
			imgx = ImageIO.read(getClass().getResourceAsStream("/x.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(BasicMovement basicMovement) {
		List<Point> points = basicMovement.getPoints();
		g2d.setColor(basicMovement.getColor());

		if (points.size() == 1) {
			g2d.drawLine(points.get(0).x, points.get(0).y, points.get(0).x, points.get(0).y);
			return;
		}

		for (int i = 0; i < points.size() - 1; i++) {
			g2d.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
		}
	}

	@Override
	public void visit(SelectionRectangle sR) {
		g2d.setColor(sR.getColor());
		a = sR.getStart();
		b = sR.getEnd();

		c.x = Math.min(a.x, b.x);
		c.y = Math.min(a.y, b.y);

		g2d.drawRect(c.x, c.y, Math.abs(a.x - b.x), Math.abs(a.y - b.y));
	}

	@Override
	public void visit(KTable table) {
		g2d.setColor(table.getColor());

		int width = table.getWidth();
		int height = table.getHeight();
		a = table.getP();
		g2d.drawRect(a.x, a.y, width, height);

		double avgHeight = table.getHeight() / (table.getNumOfHorisontalLines() - 1);
		double avgWidth = table.getWidth() / (table.getNumOfVerticalLines() - 1);

		for (int i = 1, y; i < table.getNumOfHorisontalLines() - 1; i++) {
			y = (int) (a.y + i * avgHeight);
			g2d.drawLine(a.x, y, a.x + width, y);
		}

		for (int i = 1, x; i < table.getNumOfVerticalLines() - 1; i++) {
			x = (int) (a.x + i * avgWidth);
			g2d.drawLine(x, a.y, x, a.y + height);
		}

		Value[][] values = table.getValues();

		int r = table.getR();
		int s = table.getS();
		double xOffset = (avgWidth - 16) / 2;
		double yOffset = (avgHeight - 16) / 2;

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				drawPicForTable(values[i][j].value, avgWidth, avgHeight, a, xOffset, yOffset, i, j);
			}
		}
	}

	private void drawPicForTable(int value, double avgWidth, double avgHeight, Point a2, double xOffset, double yOffset,
			int i, int j) {
		int x = (int) (a2.x + xOffset + j * avgWidth);
		int y = (int) (a2.y + yOffset + i * avgHeight);
		Image img = null;
		if (value == 0) {
			img = img0;
		} else if (value == 1) {
			img = img1;
		} else if (value == 2) {
			img = imgx;
		} else {
			return;
		}
		g2d.drawImage(img, x, y, null);
	}
}
