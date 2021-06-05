package hr.fer.zemris.diprad.drawing.graphical.objects;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObjectVisitor;
import hr.fer.zemris.diprad.recognition.models.VariableModel;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.ColRow;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;
import hr.fer.zemris.diprad.util.Rounding;

public class KTable extends GraphicalObject {
	private Point p;
	private int numOfVerticalLines;
	private int numOfHorisontalLines;
	private int width;
	private int height;
	private int r;
	private int s;
	private Value[][] values;
	private List<BasicMovementWrapper> bmws;
	private String functionName = "";
	private Line separationLine;
	private List<String> leftVariables, rightVariables;
	private Set<String> boolTableVariables;
	private String leftVariable = "", rightVariable = "", topVariable = "", downVariable = "";
	private boolean leftIsCenter = false;
	private boolean topIsCenter = false;
	private List<Rounding> innerRoundings;
	private List<Rounding> outerRoundingsLeft;
	private List<Rounding> outerRoundingsTop;
	private Rounding cornersRounding;

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

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getLeftVariable() {
		return leftVariable;
	}

	public String getRightVariable() {
		return rightVariable;
	}

	public String getTopVariable() {
		return topVariable;
	}

	public String getDownVariable() {
		return downVariable;
	}

	public boolean isLeftIsCenter() {
		return leftIsCenter;
	}

	public boolean isTopIsCenter() {
		return topIsCenter;
	}

	public Line getSeparationLine() {
		return this.separationLine;
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

	public String getFunctionName() {
		return this.functionName;
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
				this.value = MIN_VALUE - 1;
			}
		}

		public static boolean checkValue(int value) {
			return value >= MIN_VALUE - 1 && value <= MAX_VALUE;
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

	public List<BasicMovementWrapper> getBmws() {
		return bmws;
	}

	public void setBms(List<BasicMovementWrapper> bmws) {
		this.bmws = bmws;
	}

	public Rectangle getBoundingRectangle() {
		return new Rectangle(new Point(p), new Point(p.x + width, p.y + height));
	}

	public Rectangle getExpandedBoundingRectangle() {
		double widthScaled = width * 0.25;
		double heightScaled = height * 0.25;

		return new Rectangle(new PointDouble(p.x - widthScaled, p.y - heightScaled),
				new PointDouble(p.x + width + widthScaled, p.y + height + heightScaled));
	}

	public double getAvgWidth() {
		return width / (numOfVerticalLines - 1.0);
	}

	public double getAvgHeight() {
		return height / (numOfHorisontalLines - 1.0);
	}

	public void initValues(int[][] values2) {
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				values[i][j] = new Value(values2[i][j]);
			}
		}
	}

	public void setFunctionName(List<VariableModel> topVMs) {
		if (topVMs.size() == 1) {
			functionName = topVMs.get(0).getVariable();
			if (!(functionName.startsWith("f") || functionName.startsWith("g") || functionName.startsWith("h"))) {
				functionName = "f0";
				throw new RuntimeException("Invalid function name: " + topVMs.get(0).getVariable());
			}
		} else if (topVMs.isEmpty()) {
			functionName = "f0";
		} else {
			throw new RuntimeException("Too many variables for function name given (number): " + topVMs.size());
		}
	}

	public void setSeparationLine(Line l) {
		this.separationLine = l;
	}

	public List<String> getLeftVariables() {
		return leftVariables;
	}

	public List<String> getRightVariables() {
		return rightVariables;
	}

	public Set<String> getBoolTableVariables() {
		return boolTableVariables;
	}

	public void initVariableNames(List<VariableModel> leftVMs, List<VariableModel> rightVMs) {
		if (leftVMs.size() * 2 != this.r) {
			throw new RuntimeException("Bad variables left(count): " + leftVMs.size());
		}
		if (rightVMs.size() * 2 != this.s) {
			throw new RuntimeException("Bad variables right(count): " + rightVMs.size());
		}

		boolTableVariables = new TreeSet<>();
		for (VariableModel vm : leftVMs) {
			boolTableVariables.add(vm.getVariable());
		}
		for (VariableModel vm : rightVMs) {
			boolTableVariables.add(vm.getVariable());
		}

		if (boolTableVariables.size() != leftVMs.size() + rightVMs.size()) {
			boolTableVariables = null;
			throw new RuntimeException("Duplicate variable given");
		}

		leftVariables = new ArrayList<>();
		for (VariableModel vm : leftVMs) {
			leftVariables.add(vm.getVariable());
		}

		rightVariables = new ArrayList<>();
		for (VariableModel vm : rightVMs) {
			rightVariables.add(vm.getVariable());
		}
	}

	public void initLeftRightVariables(VariableModel vmLeft, VariableModel vmRight) {
		if (vmRight == null) {
			if (leftVariables.contains(vmLeft.getVariable())) {
				leftVariable = vmLeft.getVariable();
			} else {
				throw new RuntimeException("Left Variable doesnt match");
			}
		} else {
			if (vmLeft.getVariable().equals(vmRight.getVariable())) {
				throw new RuntimeException("Left and Right variable are equal");
			}

			if (leftVariables.contains(vmLeft.getVariable()) && leftVariables.contains(vmRight.getVariable())) {
				leftVariable = vmLeft.getVariable();
				rightVariable = vmRight.getVariable();
				leftIsCenter = vmLeft.isCenter();
			} else {
				throw new RuntimeException("Left or Right Variable doesnt match");
			}

		}
	}

	public void initTopDownVariables(VariableModel vmTop, VariableModel vmDown) {
		if (vmDown == null) {
			if (rightVariables.contains(vmTop.getVariable())) {
				topVariable = vmTop.getVariable();
			} else {
				throw new RuntimeException("Top Variable doesnt match");
			}
		} else {
			if (vmTop.getVariable().equals(vmDown.getVariable())) {
				throw new RuntimeException("Top and Down variable are equal");
			}

			if (rightVariables.contains(vmTop.getVariable()) && rightVariables.contains(vmDown.getVariable())) {
				topVariable = vmTop.getVariable();
				downVariable = vmDown.getVariable();
				topIsCenter = vmTop.isCenter();
			} else {
				throw new RuntimeException("Top or Down Variable doesnt match");
			}
		}
	}

	public List<Rounding> getInnerRoundings() {
		return innerRoundings;
	}

	public void setInnerRoundings(List<Rounding> innerRoundings) {
		this.innerRoundings = innerRoundings;
	}

	public List<Rounding> getOuterRoundingsLeft() {
		return outerRoundingsLeft;
	}

	public void setOuterRoundingsLeft(List<Rounding> outerRoundingsLeft) {
		this.outerRoundingsLeft = outerRoundingsLeft;
	}

	public Rounding getCornersRounding() {
		return cornersRounding;
	}

	public void setCornersRounding(Rounding cornersRounding) {
		this.cornersRounding = cornersRounding;
	}

	public List<Rounding> getOuterRoundingsTop() {
		return outerRoundingsTop;
	}

	public void setOuterRoundingsTop(List<Rounding> outerRoundingsTop) {
		this.outerRoundingsTop = outerRoundingsTop;
	}

	public void writeValues() {
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < s; j++) {
				System.out.print(values[i][j] + " ");
			}
			System.out.println();
		}
	}

	public List<ColRow> xRangeYrange(String variable, int val) {
		List<ColRow> range = new ArrayList<>();
		if (val == 1) {
			if (variable.equals(topVariable)) {
				if (topIsCenter) {
					range.add(new ColRow(true, 1));
					range.add(new ColRow(true, 2));
				} else {
					if (s == 4) {
						range.add(new ColRow(true, 2));
						range.add(new ColRow(true, 3));
					} else {
						range.add(new ColRow(true, 1));
					}
				}
			}
			if (variable.equals(downVariable)) {
				if (!topIsCenter) {
					range.add(new ColRow(true, 1));
					range.add(new ColRow(true, 2));
				} else {
					if (s == 4) {
						range.add(new ColRow(true, 2));
						range.add(new ColRow(true, 3));
					} else {
						range.add(new ColRow(true, 1));
					}
				}
			}

			if (variable.equals(leftVariable)) {
				if (leftIsCenter) {
					range.add(new ColRow(false, 1));
					range.add(new ColRow(false, 2));
				} else {
					if (r == 4) {
						range.add(new ColRow(false, 2));
						range.add(new ColRow(false, 3));
					} else {
						range.add(new ColRow(false, 1));
					}
				}
			}
			if (variable.equals(rightVariable)) {
				if (!leftIsCenter) {
					range.add(new ColRow(false, 1));
					range.add(new ColRow(false, 2));
				} else {
					if (r == 4) {
						range.add(new ColRow(false, 2));
						range.add(new ColRow(false, 3));
					} else {
						range.add(new ColRow(false, 1));
					}
				}
			}
		} else {
			if (variable.equals(topVariable)) {
				if (topIsCenter) {
					range.add(new ColRow(true, 0));
					range.add(new ColRow(true, 3));
				} else {
					if (s == 4) {
						range.add(new ColRow(true, 0));
						range.add(new ColRow(true, 1));
					} else {
						range.add(new ColRow(true, 0));
					}
				}
			}
			if (variable.equals(downVariable)) {
				if (!topIsCenter) {
					range.add(new ColRow(true, 0));
					range.add(new ColRow(true, 3));
				} else {
					if (s == 4) {
						range.add(new ColRow(true, 0));
						range.add(new ColRow(true, 1));
					} else {
						range.add(new ColRow(true, 0));
					}
				}
			}

			if (variable.equals(leftVariable)) {
				if (leftIsCenter) {
					range.add(new ColRow(false, 0));
					range.add(new ColRow(false, 3));
				} else {
					if (r == 4) {
						range.add(new ColRow(false, 0));
						range.add(new ColRow(false, 1));
					} else {
						range.add(new ColRow(false, 0));
					}
				}
			}
			if (variable.equals(rightVariable)) {
				if (!leftIsCenter) {
					range.add(new ColRow(false, 0));
					range.add(new ColRow(false, 3));
				} else {
					if (r == 4) {
						range.add(new ColRow(false, 0));
						range.add(new ColRow(false, 1));
					} else {
						range.add(new ColRow(false, 0));
					}
				}
			}
		}
		return range;
	}
}
