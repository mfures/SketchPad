package hr.fer.zemris.diprad.drawing.graphical.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;

public class BasicMovement extends GraphicalObject {
	private List<Point> points;
	private String label = "";

	public BasicMovement(List<Point> points) {
		this.points = points;
	}

	@Override
	public void accept(GraphicalObjectVisitor v) {
		v.visit(this);
	}

	public List<Point> getPoints() {
		return points;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public boolean isInRect(int minX, int maxX, int minY, int maxY) {
		for (Point p : points) {
			if (p.x > minX && p.x < maxX && p.y > minY && p.y < maxY)
				return true;
		}

		return false;
	}

	@Override
	public boolean youInterested(Point p) {
		return false;
	}

	@Override
	public void handleIntrest(Point point) {
	}

	@Override
	public String toString() {
		return "BasicMovement:" + this.hashCode();
	}

	@Override
	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append("BM");
		sb.append(label);
		sb.append(":");
		for (Point p : points) {
			sb.append(p.x);
			sb.append("\s");
			sb.append(p.y);
			sb.append("#");
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static BasicMovement parseBasicMovement(String line) {
		try {
			String[] parts = line.split(":");
			String[] points = parts[1].split("#");

			List<Point> list = new ArrayList<>();
			for (int i = 0; i < points.length; i++) {
				String[] values = points[i].split("\s");
				if (values.length != 2) {
					throw new RuntimeException("Invalid format");
				}

				Point p = new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
				list.add(p);
			}

			BasicMovement bm = new BasicMovement(list);
			bm.setLabel(parts[0].substring(2));

			return bm;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't parse line to basic movement. " + e.getMessage());
		}
	}
}
