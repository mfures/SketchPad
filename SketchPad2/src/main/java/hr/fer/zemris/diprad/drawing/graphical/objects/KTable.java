package hr.fer.zemris.diprad.drawing.graphical.objects;

import java.awt.Point;
import java.util.List;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;

public class KTable extends GraphicalObject {
	private Point p;
	private int numOfVerticalLines;
	private int numOfHorisontalLines;
	private int width;
	private int height;
	private int r;
	private int s;
	private Value[][] values;
	private List<BasicMovement> bms;

	public KTable(Point p, int numOfVerticalLines, int numOfHorisontalLines, int width, int height) {
		this.p = p;
		this.numOfVerticalLines = numOfVerticalLines;
		s = numOfVerticalLines - 1;
		this.numOfHorisontalLines = numOfHorisontalLines;
		r = numOfHorisontalLines - 1;
		this.width = width;
		this.height = height;
		values = new Value[r][s];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				values[i][j] = new Value();
			}
		}

	}

	public KTable(Point p, int width, int height, int r, int s, String[] values) {
		this.p = p;
		this.width = width;
		this.height = height;
		this.r = r;
		this.s = s;
		this.numOfVerticalLines = s + 1;
		this.numOfHorisontalLines = r + 1;
		this.values = new Value[r][s];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				int v = Integer.parseInt(values[i * s + j]);
				if (v == -1) {
					this.values[i][j] = new Value();
				} else {
					this.values[i][j] = new Value(Integer.parseInt(values[i * s + j]));
				}
			}
		}
	}

	public Point getP() {
		return p;
	}

	public int getNumOfVerticalLines() {
		return numOfVerticalLines;
	}

	public int getNumOfHorisontalLines() {
		return numOfHorisontalLines;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void accept(GraphicalObjectVisitor v) {
		v.visit(this);
	}

	@Override
	public boolean isInRect(int minX, int maxX, int minY, int maxY) {
		return false;
	}

	public void setValueAt(Position p, int v) {
		values[p.r][p.s].value = v;
	}

	public static class Value {
		public int value;

		public static final int MAX_VALUE = 2;
		public static final int MIN_VALUE = 0;

		public Value() {
			this.value = MIN_VALUE - 1;
		}

		public Value(int value) {
			if (checkValue(value)) {
				this.value = value;
			} else {
				this.value = MIN_VALUE;
			}
		}

		public static boolean checkValue(int value) {
			return value >= MIN_VALUE && value <= MAX_VALUE;
		}

		public void incValue() {
			value = (value + 1) % (MAX_VALUE + 1);
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	public int getR() {
		return this.r;
	}

	public int getS() {
		return this.s;
	}

	public Value[][] getValues() {
		return this.values;
	}

	@Override
	public boolean youInterested(Point p) {
		if (p.x > this.p.x && p.y > this.p.y && p.x < (this.p.x + width) && p.y < (this.p.y + height))
			return true;

		return false;
	}

	@Override
	public void handleIntrest(Point point) {
		Position pos = getPosition(width, point.x - p.x, s, height, point.y - p.y, r);

		values[pos.r][pos.s].incValue();
	}

	public Position getPosition(Point point) {
		return getPosition(width, point.x - p.x, s, height, point.y - p.y, r);
	}

	private Position getPosition(int width, int i, int s, int height, int j, int r) {
		return new Position(binary(height, j, r), binary(width, i, s));
	}

	public static int binary(int max, int val, int n) {
		int index = n / 2;
		int s = 2;
		double step = (max * 1.0) / n;
		double current = max / 2;

		if (val < step)
			return 0;

		while (s <= n) {
			if (val < current) {
				s *= 2;
				index -= n / s;
				current -= (n * step) / s;
			} else if (val >= current + step) {
				s *= 2;
				index += n / s;
				current += (n * step) / s;
			} else {
				break;
			}
		}
		return index;
	}

	public static class Position {
		public int r, s;

		public Position(int r, int s) {
			this.r = r;
			this.s = s;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + r;
			result = prime * result + s;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (r != other.r)
				return false;
			if (s != other.s)
				return false;
			return true;
		}
	}

	@Override
	public String toString() {
		return "KTable:" + this.hashCode();
	}

	@Override
	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append("KT:");

		sb.append(p.x);
		sb.append("\s");
		sb.append(p.y);
		sb.append("#");
		sb.append(width);
		sb.append("#");
		sb.append(height);
		sb.append("#");
		sb.append(r);
		sb.append("#");
		sb.append(s);
		sb.append("#");

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				sb.append(values[i][j]);
				sb.append("\s");
			}
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static KTable parseKTable(String line) {
		try {
			String[] parts = line.split(":");
			String[] points = parts[1].split("#");// Point p, int numOfVerticalLines, int numOfHorisontalLines, int
													// width, int height

			Point p = new Point(Integer.parseInt(points[0].split("\s")[0]), Integer.parseInt(points[0].split("\s")[1]));
			int width = Integer.parseInt(points[1]);
			int height = Integer.parseInt(points[2]);
			int r = Integer.parseInt(points[3]);
			int s = Integer.parseInt(points[4]);
			String[] val = points[5].split("\s");

			return new KTable(p, width, height, r, s, val);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't parse line to basic movement. " + e.getMessage());
		}
	}

	public List<BasicMovement> getBms() {
		return bms;
	}

	public void setBms(List<BasicMovement> bms) {
		this.bms = bms;
	}
}
