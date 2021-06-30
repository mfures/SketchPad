package hr.fer.zemris.diprad.drawing.visitors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.drawing.graphical.objects.TruthTable;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.util.Rounding;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable.Value;

public class GraphicalObjectPainter implements GraphicalObjectVisitor {
	private Point a;
	private Point b;
	private Point c;
	private Image img0;
	private Image img1;
	private Image imgx;
	private static final int TRANS = 50;

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

		if (width / table.getS() < 80) {
			width = 80 * table.getS();
			table.setWidth(width);
		}
		if (height / table.getR() < 80) {
			height = 80 * table.getR();
			table.setHeight(height);
		}

		a = table.getP();
		g2d.drawRect(a.x, a.y, width, height);

		int letterFontHalfSize = 18;
		int numberFontHalfSize = 12;
		int numberHeightDifference = 4;

		Font f1 = new Font("Courier New", Font.PLAIN, letterFontHalfSize * 2);
		Font f2 = new Font("Courier New", Font.PLAIN, numberFontHalfSize * 2);

		Line l = table.getSeparationLine();
		if (l != null) {
			g2d.drawLine(l.getMinX(), l.getMinY(), a.x, a.y);
		}
		if (!table.getFunctionName().isEmpty()) {
			int len = drawnStringLength(table.getFunctionName(), letterFontHalfSize, numberFontHalfSize);
			drawVariables(table.getFunctionName(), f1, f2, letterFontHalfSize, numberFontHalfSize,
					l.getMinX() - len / 2, l.getMinY() - 20, g2d, numberHeightDifference);
		}
		if (table.getLeftVariables() != null) {
			StringBuilder sb = new StringBuilder();
			for (String s : table.getLeftVariables()) {
				sb.append(s);
			}
			String vars = sb.toString();
			int len = drawnStringLength(vars, letterFontHalfSize, numberHeightDifference);
			drawVariables(vars, f1, f2, letterFontHalfSize, numberFontHalfSize, (int) (l.getAverageX() - len - 25),
					(int) (l.getAverageY() + letterFontHalfSize), g2d, numberHeightDifference);
		}
		if (table.getRightVariables() != null) {
			StringBuilder sb = new StringBuilder();
			for (String s : table.getRightVariables()) {
				sb.append(s);
			}
			String vars = sb.toString();
			drawVariables(vars, f1, f2, letterFontHalfSize, numberFontHalfSize, (int) (l.getAverageX() + 35),
					(int) (l.getAverageY() + letterFontHalfSize), g2d, numberHeightDifference);
		}

		int lineShortner = 5;

		if (!table.getTopVariable().isEmpty()) {
			int len = drawnStringLength(table.getTopVariable(), letterFontHalfSize, numberFontHalfSize);
			if (table.isTopIsCenter()) {
				g2d.drawLine(a.x + lineShortner + width / 4, a.y - 5, a.x + (3 * width) / 4 - lineShortner, a.y - 5);
				drawVariables(table.getTopVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + width / 2 - len / 2), (int) (a.y - 11), g2d, numberHeightDifference);
			} else {
				g2d.drawLine(a.x + lineShortner + width / 2, a.y - 5, a.x + width - lineShortner, a.y - 5);
				drawVariables(table.getTopVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + (3 * width) / 4 - len / 2), (int) (a.y - 11), g2d, numberHeightDifference);
			}
		}

		if (!table.getDownVariable().isEmpty()) {
			int len = drawnStringLength(table.getDownVariable(), letterFontHalfSize, numberFontHalfSize);
			if (!table.isTopIsCenter()) {
				g2d.drawLine(a.x + lineShortner + width / 4, a.y + height + 5, a.x + (3 * width) / 4 - lineShortner,
						a.y + height + 5);
				drawVariables(table.getDownVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + width / 2 - len / 2), (int) (a.y + height + 11 + letterFontHalfSize), g2d,
						numberHeightDifference);
			} else {
				g2d.drawLine(a.x + lineShortner + width / 2, a.y + height + 5, a.x + width - lineShortner,
						a.y + height + 5);
				drawVariables(table.getDownVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + (3 * width) / 4 - len / 2), (int) (a.y + height + 11 + letterFontHalfSize), g2d,
						numberHeightDifference);
			}
		}

		if (!table.getLeftVariable().isEmpty()) {
			int len = drawnStringLength(table.getLeftVariable(), letterFontHalfSize, numberFontHalfSize);
			if (table.isLeftIsCenter()) {
				g2d.drawLine(a.x - 5, a.y + lineShortner + height / 4, a.x - 5, a.y - lineShortner + (3 * height) / 4);
				drawVariables(table.getLeftVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x - 5 - len - 3), (int) (a.y + height / 2 + letterFontHalfSize / 2), g2d,
						numberHeightDifference);
			} else {
				g2d.drawLine(a.x - 5, a.y + lineShortner + height / 2, a.x - 5, a.y + height - lineShortner);
				drawVariables(table.getLeftVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x - 5 - len - 3), (int) (a.y + (3 * height) / 4 + letterFontHalfSize / 2), g2d,
						numberHeightDifference);
			}
		}
		if (!table.getRightVariable().isEmpty()) {
			if (!table.isLeftIsCenter()) {
				g2d.drawLine(a.x + 5 + width, a.y + lineShortner + height / 4, a.x + 5 + width,
						a.y - lineShortner + (3 * height) / 4);
				drawVariables(table.getRightVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + 5 + width + 3), (int) (a.y + height / 2 + letterFontHalfSize / 2), g2d,
						numberHeightDifference);
			} else {
				g2d.drawLine(a.x + 5 + width, a.y + lineShortner + height / 2, a.x + 5 + width,
						a.y + height - lineShortner);
				drawVariables(table.getRightVariable(), f1, f2, letterFontHalfSize, numberFontHalfSize,
						(int) (a.x + 5 + width + 3), (int) (a.y + (3 * height) / 4 + letterFontHalfSize / 2), g2d,
						numberHeightDifference);
			}
		}

		int[][] margins = new int[table.getS()][table.getR()];
		int cornerDist = 3;
		for (int r = 0; r < table.getR(); r++) {
			for (int s = 0; s < table.getS(); s++) {
				margins[s][r] = 2;
			}
		}

		if (table.getCornersRounding() != null) {
			draw4Corners(margins, g2d, a.x, a.y, width, height, cornerDist);
		}

		int avgWidth = width / table.getS();
		int avgHeight = height / table.getR();

		if (table.getInnerRoundings() != null) {
			var roundings = table.getInnerRoundings();

			for (int i = roundings.size() - 1; i >= 0; i--) {
				drawInnerRounding(roundings.get(i), margins, g2d, a.x, a.y, avgWidth, avgHeight, cornerDist);
			}
		}

		if (table.getOuterRoundingsLeft() != null) {
			var roundings = table.getOuterRoundingsLeft();

			for (int i = roundings.size() - 1; i >= 0; i--) {
				drawOuterRoundingsLeft(roundings.get(i), margins, g2d, a.x, a.y, avgWidth, avgHeight, cornerDist,
						width);
			}
		}

		if (table.getOuterRoundingsTop() != null) {
			var roundings = table.getOuterRoundingsTop();

			for (int i = roundings.size() - 1; i >= 0; i--) {
				drawOuterRoundingsTop(roundings.get(i), margins, g2d, a.x, a.y, avgWidth, avgHeight, cornerDist,
						height);
			}
		}

		g2d.setColor(Color.black);
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

	private void drawOuterRoundingsTop(Rounding rounding, int[][] margins, Graphics2D g2d2, int x, int y,
			int avgWidthInt, int avgHeightInt, int cornerDist, int height) {
		int margin = getMaxMarginAndIncAllMarginsTop(rounding, margins);
		int[] xPoints = new int[6];
		int[] yPoints = new int[6];

		xPoints[0] = x + margin + avgHeightInt * rounding.getP1().x;
		yPoints[0] = y;
		xPoints[1] = x + margin + avgHeightInt * rounding.getP1().x;
		yPoints[1] = y - margin - cornerDist + avgHeightInt;
		xPoints[2] = x + margin + cornerDist + avgHeightInt * rounding.getP1().x;
		yPoints[2] = y - margin + avgHeightInt;
		xPoints[3] = x - margin - cornerDist + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[3] = y - margin + avgHeightInt;
		xPoints[4] = x - margin + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[4] = y - margin - cornerDist + avgHeightInt;
		xPoints[5] = x - margin + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[5] = y;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 6);

		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 6);

		y += height;

		xPoints[0] = x + margin + avgHeightInt * rounding.getP1().x;
		yPoints[0] = y;
		xPoints[1] = x + margin + avgHeightInt * rounding.getP1().x;
		yPoints[1] = y - (-margin - cornerDist + avgHeightInt);
		xPoints[2] = x + margin + cornerDist + avgHeightInt * rounding.getP1().x;
		yPoints[2] = y - (-margin + avgHeightInt);
		xPoints[3] = x - margin - cornerDist + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[3] = y - (-margin + avgHeightInt);
		xPoints[4] = x - margin + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[4] = y - (-margin - cornerDist + avgHeightInt);
		xPoints[5] = x - margin + avgHeightInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[5] = y;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 6);

		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 6);
	}

	private int getMaxMarginAndIncAllMarginsTop(Rounding rounding, int[][] margins) {
		int maxMargin = 0;
		for (int i = 0; i < rounding.getP2().x; i++) {
			maxMargin = Math.max(maxMargin, margins[rounding.getP1().x + i][0]);
			margins[rounding.getP1().x + i][0] += 2;

			maxMargin = Math.max(maxMargin, margins[rounding.getP1().x + i][3]);
			margins[rounding.getP1().x + i][3] += 2;
		}
		return maxMargin;
	}

	private void drawOuterRoundingsLeft(Rounding rounding, int[][] margins, Graphics2D g2d2, int x, int y,
			int avgWidthInt, int avgHeightInt, int cornerDist, int width) {
		int margin = getMaxMarginAndIncAllMarginsLeft(rounding, margins);
		int[] xPoints = new int[6];
		int[] yPoints = new int[6];

		xPoints[0] = x;
		yPoints[0] = y + margin + avgHeightInt * rounding.getP1().y;
		xPoints[1] = x;
		yPoints[1] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[2] = x - margin - cornerDist + avgWidthInt;
		yPoints[2] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[3] = x - margin + avgWidthInt;
		yPoints[3] = y - margin - cornerDist + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[4] = x - margin + avgWidthInt;
		yPoints[4] = y + margin + cornerDist + avgHeightInt * rounding.getP1().y;
		xPoints[5] = x - margin - cornerDist + avgWidthInt;
		yPoints[5] = y + margin + avgHeightInt * rounding.getP1().y;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 6);

		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 6);

		x += width;

		xPoints[0] = x;
		yPoints[0] = y + margin + avgHeightInt * rounding.getP1().y;
		xPoints[1] = x;
		yPoints[1] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[2] = x - (-margin - cornerDist + avgWidthInt);
		yPoints[2] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[3] = x - (-margin + avgWidthInt);
		yPoints[3] = y - margin - cornerDist + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[4] = x - (-margin + avgWidthInt);
		yPoints[4] = y + margin + cornerDist + avgHeightInt * rounding.getP1().y;
		xPoints[5] = x - (-margin - cornerDist + avgWidthInt);
		yPoints[5] = y + margin + avgHeightInt * rounding.getP1().y;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 6);

		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 6);
	}

	private int getMaxMarginAndIncAllMarginsLeft(Rounding rounding, int[][] margins) {
		int maxMargin = 0;
		for (int j = 0; j < rounding.getP2().y; j++) {
			maxMargin = Math.max(maxMargin, margins[rounding.getP1().x][rounding.getP1().y + j]);
			margins[rounding.getP1().x][rounding.getP1().y + j] += 2;

			maxMargin = Math.max(maxMargin, margins[3][rounding.getP1().y + j]);
			margins[3][rounding.getP1().y + j] += 2;
		}
		return maxMargin;
	}

	private void drawInnerRounding(Rounding rounding, int[][] margins, Graphics2D g2d, int x, int y, int avgWidthInt,
			int avgHeightInt, int cornerDist) {
		int margin = getMaxMarginAndIncAllMargins(rounding, margins);
		int[] xPoints = new int[8];
		int[] yPoints = new int[8];

		xPoints[0] = x + margin + avgWidthInt * rounding.getP1().x;
		yPoints[0] = y - margin - cornerDist + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[1] = x + margin + avgWidthInt * rounding.getP1().x + cornerDist;
		yPoints[1] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);

		xPoints[2] = x - margin - cornerDist + avgWidthInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[2] = y - margin + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);
		xPoints[3] = x - margin + avgWidthInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[3] = y - margin - cornerDist + avgHeightInt * (rounding.getP1().y + rounding.getP2().y);

		xPoints[4] = x - margin + avgWidthInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[4] = y + margin + cornerDist + avgHeightInt * rounding.getP1().y;
		xPoints[5] = x - margin - cornerDist + avgWidthInt * (rounding.getP1().x + rounding.getP2().x);
		yPoints[5] = y + margin + avgHeightInt * rounding.getP1().y;

		xPoints[6] = x + margin + cornerDist + avgWidthInt * rounding.getP1().x;
		yPoints[6] = y + margin + avgHeightInt * rounding.getP1().y;
		xPoints[7] = x + margin + avgWidthInt * rounding.getP1().x;
		yPoints[7] = y + margin + cornerDist + avgHeightInt * rounding.getP1().y;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 8);

		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 8);
	}

	private int getMaxMarginAndIncAllMargins(Rounding rounding, int[][] margins) {
		int maxMargin = 0;
		for (int i = 0; i < rounding.getP2().x; i++) {
			for (int j = 0; j < rounding.getP2().y; j++) {
				maxMargin = Math.max(maxMargin, margins[rounding.getP1().x + i][rounding.getP1().y + j]);
				margins[rounding.getP1().x + i][rounding.getP1().y + j] += 2;
			}
		}
		return maxMargin;
	}

	private void draw4Corners(int[][] margins, Graphics2D g2d, int x, int y, int width, int height, int cornerDist) {
		int[] xPoints = new int[5];
		int[] yPoints = new int[5];

		xPoints[0] = x;
		yPoints[0] = y;
		xPoints[1] = x;
		yPoints[1] = y + height / 4 - margins[0][0];
		xPoints[2] = x + width / 4 - cornerDist;
		yPoints[2] = y + height / 4 - margins[0][0];
		xPoints[3] = x + width / 4 - margins[0][0];
		yPoints[3] = y + height / 4 - cornerDist;
		xPoints[4] = x + width / 4 - margins[0][0];
		yPoints[4] = y;
		margins[0][0] += 2;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 5);
		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 5);

		xPoints[0] = x + (3 * (width / 4)) + margins[0][3];
		yPoints[0] = y;
		xPoints[1] = x + (3 * (width / 4)) + margins[0][3];
		yPoints[1] = y + height / 4 - cornerDist;
		xPoints[2] = x + (3 * (width / 4)) + cornerDist;
		yPoints[2] = y + height / 4 - margins[0][3];
		xPoints[3] = x + width;
		yPoints[3] = y + height / 4 - margins[0][3];
		xPoints[4] = x + width;
		yPoints[4] = y;
		margins[0][3] += 2;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 5);
		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 5);

		xPoints[0] = x;
		yPoints[0] = y + height;
		xPoints[1] = x + width / 4 - margins[3][0];
		yPoints[1] = y + height;
		xPoints[2] = x + width / 4 - margins[3][0];
		yPoints[2] = y + (3 * (height / 4)) + margins[3][0] + cornerDist;
		xPoints[3] = x + width / 4 - cornerDist;
		yPoints[3] = y + (3 * (height / 4)) + margins[3][0];
		xPoints[4] = x;
		yPoints[4] = y + (3 * (height / 4)) + margins[3][0];
		margins[3][0] += 2;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 5);
		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 5);

		xPoints[0] = x + width;
		yPoints[0] = y + height;
		xPoints[1] = x + width;
		yPoints[1] = y + 3 * (height / 4) + margins[3][3];
		xPoints[2] = x + (3 * (width / 4)) + cornerDist;
		yPoints[2] = y + 3 * (height / 4) + margins[3][3];
		xPoints[3] = x + (3 * (width / 4)) + margins[3][3];
		yPoints[3] = y + 3 * (height / 4) + cornerDist;
		xPoints[4] = x + (3 * (width / 4)) + margins[3][3];
		yPoints[4] = y + height;
		margins[3][3] += 2;

		g2d.setColor(Color.BLACK);
		g2d.drawPolygon(xPoints, yPoints, 5);
		g2d.setColor(new Color(100, 0, 0, TRANS));
		g2d.fillPolygon(xPoints, yPoints, 5);

//		g2d.drawLine(x + (3 * (width / 4)) + cornerDist, y + 3 * (height / 4) + margins[3][3], x + width,
//				y + 3 * (height / 4) + margins[3][3]);
//		g2d.drawLine(x + (3 * (width / 4)) + margins[3][3], y + 3 * (height / 4) + cornerDist,
//				x + (3 * (width / 4)) + margins[3][3], y + height);
//		g2d.drawLine(x + (3 * (width / 4)) + cornerDist, y + 3 * (height / 4) + margins[3][3],
//				x + (3 * (width / 4)) + margins[3][3], y + 3 * (height / 4) + cornerDist);
	}

	private void drawVariables(String functionName, Font f1, Font f2, int letterFontHalfSize, int numberFontHalfSize,
			int x, int y, Graphics2D g2d, int numberHeightDifference) {
		var strings = functionName.split("");
		for (String s : strings) {
			if (Character.isDigit(s.charAt(0))) {
				g2d.setFont(f2);
				g2d.drawString(s, x, y + numberHeightDifference);
				x += numberFontHalfSize;
			} else {
				g2d.setFont(f1);
				g2d.drawString(s, x, y);
				x += letterFontHalfSize;
			}
		}
	}

	private int drawnStringLength(String s, int fLetter, int fNumber) {
		var strings = s.split("");
		int totalLength = 0;

		for (String character : strings) {
			if (Character.isDigit(character.charAt(0))) {
				totalLength += fNumber;
			} else {
				totalLength += fLetter;
			}
		}

		return totalLength;
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

	private void drawPicForTruthTable(int value, double avgWidth, double avgHeight, Point a2, double xOffset,
			double yOffset, int i, int j) {
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
			img = img0;
		}
		g2d.drawImage(img, x, y, null);
	}

	@Override
	public void visit(TruthTable truthTable) {
		Point a = truthTable.getP();
		int varHeight = truthTable.getVarHeight();
		int valHeight = truthTable.getValHeight();
		g2d.setColor(Color.black);
		Set<String> vars = truthTable.variables();
		int variableCount = vars.size();
		int width = truthTable.width();
		int valRowCount = (int) Math.pow(2, variableCount);
		int height = truthTable.height();
		g2d.drawRect(a.x, a.y, width, height);
		int stepY = varHeight;
		g2d.drawLine(a.x, a.y + stepY, a.x + width, a.y + stepY);
		for (int i = 1; i < valRowCount; i++) {
			stepY += valHeight;
			g2d.drawLine(a.x, a.y + stepY, a.x + width, a.y + stepY);
		}
		int stepX = 0;
		for (int i = 0; i < variableCount; i++) {
			stepX += varHeight;
			g2d.drawLine(a.x + stepX, a.y, a.x + stepX, a.y + height);
		}

		int letterFontHalfSize = 18;
		int numberFontHalfSize = 12;
		int numberHeightDifference = 4;

		Font f1 = new Font("Courier New", Font.PLAIN, letterFontHalfSize * 2);
		Font f2 = new Font("Courier New", Font.PLAIN, numberFontHalfSize * 2);

		int len = drawnStringLength(truthTable.getFunctionName(), letterFontHalfSize, numberHeightDifference);
		drawVariables(truthTable.getFunctionName(), f1, f2, letterFontHalfSize, numberFontHalfSize,
				a.x + variableCount * varHeight + (varHeight - len) / 2, a.y + (varHeight - letterFontHalfSize), g2d,
				numberHeightDifference);

		int k = 0;
		for (String varName : vars) {
			len = drawnStringLength(varName, letterFontHalfSize, numberHeightDifference);
			drawVariables(varName, f1, f2, letterFontHalfSize, numberFontHalfSize,
					a.x + k * varHeight + (varHeight - len) / 2, a.y + (varHeight - letterFontHalfSize), g2d,
					numberHeightDifference);

			k++;
		}

		double xOffset = (varHeight - 16) / 2;
		double yOffset = (valHeight - 16) / 2;

		Point b = new Point(a);
		b.y += varHeight;
		for (int i = 0; i < valRowCount; i++) {
			if (valRowCount == 16) {
				drawPicForTruthTable(i / 8, varHeight, valHeight, b, xOffset, yOffset, i, 0);
				drawPicForTruthTable((i / 4) % 2, varHeight, valHeight, b, xOffset, yOffset, i, 1);
				drawPicForTruthTable((i / 2) % 2, varHeight, valHeight, b, xOffset, yOffset, i, 2);
				drawPicForTruthTable(i % 2, varHeight, valHeight, b, xOffset, yOffset, i, 3);
				drawPicForTruthTable(truthTable.getValueAt(i / 8, (i / 4) % 2, (i / 2) % 2, i % 2), varHeight, valHeight, b,
						xOffset, yOffset, i, 4);
			} else if (valRowCount == 8) {
				drawPicForTruthTable(i / 4, varHeight, valHeight, b, xOffset, yOffset, i, 0);
				drawPicForTruthTable((i / 2) % 2, varHeight, valHeight, b, xOffset, yOffset, i, 1);
				drawPicForTruthTable(i % 2, varHeight, valHeight, b, xOffset, yOffset, i, 2);
				drawPicForTruthTable(truthTable.getValueAt((i / 4), (i / 2) % 2, i % 2), varHeight, valHeight, b, xOffset,
						yOffset, i, 3);
			} else {
				drawPicForTruthTable(i / 2, varHeight, valHeight, b, xOffset, yOffset, i, 0);
				drawPicForTruthTable(i % 2, varHeight, valHeight, b, xOffset, yOffset, i, 1);
				drawPicForTruthTable(truthTable.getValueAt((i / 2), i % 2), varHeight, valHeight, b, xOffset, yOffset, i, 2);
			}
		}

	}
}
