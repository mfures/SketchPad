package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.recognition.LineSorter;
import hr.fer.zemris.diprad.recognition.LineValueSupplier;
import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageXSorter;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageYSorter;
import hr.fer.zemris.diprad.recognition.supliers.LineAverageXSupplier;
import hr.fer.zemris.diprad.recognition.supliers.LineAverageYSupplier;
import hr.fer.zemris.diprad.recognition.testers.LineCoordinateDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LineDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageXDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageYDistanceTester;
import hr.fer.zemris.diprad.util.Pair;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class KTableModel {
	public static final double LINES_MIN_X_DISTANCE_SCALE = 0.15;
	public static final double LINES_MIN_Y_DISTANCE_SCALE = 0.15;
	public static final double LINE_LENGTH_SCALE = 0.15;

	public static final double MIN_VECTOR_NORM = 4.2;

	public static final double TOLERANCE = 0.25;
	public static final double COEF_MIN = (1 - TOLERANCE) / (1 + TOLERANCE);
	public static final double COEF_MAX = (1 + TOLERANCE) / (1 - TOLERANCE);

	private SketchPad2 sP;

	public KTableModel(SketchPad2 sP) {
		this.sP = sP;
	}

	public void recognize(Point a, Point b) {
		List<GraphicalObject> objects = getObjectsInRectangle(a, b, sP.getModel());
		List<BasicMovement> bms = handleGraphicalObjects(objects);

		List<KTable> tables = recognizeTables(bms);

		if (tables.isEmpty()) {
			return;
		}

		tables = checkForTableOverlaps(tables);
		if (tables == null) {
			return;
		}

		// System.out.println("Našao sam ovoliko tablica:" + tables.size());
		// TODO remove
		for (var table : tables) {
			debugDrawTable(table);
			// SketchPad2.debugDraw(new SelectionRectangle(table.getBoundingRectangle()));
			// SketchPad2.debugDraw(new
			// SelectionRectangle(table.getExpandedBoundingRectangle()));

		}
	}

	private List<KTable> checkForTableOverlaps(List<KTable> tables) {
		for (int i = 0; i < tables.size() - 1; i++) {
			for (int j = i + 1; j < tables.size(); j++) {
				if (Rectangle.areOverlaping(tables.get(i).getExpandedBoundingRectangle(),
						tables.get(j).getExpandedBoundingRectangle())) {
					System.out.println("Tabels 2 close");
					return null;
				}
			}
		}

		return tables;
	}

	private List<BasicMovement> handleGraphicalObjects(List<GraphicalObject> objects) {
		List<BasicMovement> bms = new ArrayList<>();

		for (GraphicalObject go : objects) {
			if (go instanceof BasicMovement) {
				bms.add((BasicMovement) go);
			}
		}

		objects.clear();
		return bms;
	}

	private List<KTable> recognizeTables(List<BasicMovement> bms) {
		List<Line> horizontalLines = new ArrayList<>();
		List<Line> verticalLines = new ArrayList<>();

		initLines(horizontalLines, verticalLines, bms);
		// System.out.println("NUM VERT:" + verticalLines.size());
		// System.out.println("NUM HOR:" + horizontalLines.size());

		List<LineListWrapper> verticalGroups = groupLinesByLength(verticalLines);
		List<LineListWrapper> horizontalGroups = groupLinesByLength(horizontalLines);
		// System.out.println("VERT GROUPS COUNT(dist):" + verticalGroups.size());
		// System.out.println("HOR GROUPS COUNT(dist):" + horizontalGroups.size());

		verticalGroups = groupLinesByYCoordinate(verticalGroups, new LinesAverageYDistanceTester(),
				new CoordinateAverageYSorter(), false);
		horizontalGroups = groupLinesByXCoordinate(horizontalGroups, new LinesAverageXDistanceTester(),
				new CoordinateAverageXSorter(), true);
		// System.out.println("VERT GROUPS COUNT(dist+x):" + verticalGroups.size());
		// System.out.println("HOR GROUPS COUNT(dist+x):" + horizontalGroups.size());

		List<Pair<LineListWrapper, LineListWrapper>> pairsVerHor = groupLinesValidPairs(verticalGroups,
				horizontalGroups);

		if (pairsVerHor == null) {
			return null;// No grid found
		}
		// System.out.println("Number of paired groups of lines:" + pairsVerHor.size());

		List<KTable> tables = new ArrayList<>();
		for (Pair<LineListWrapper, LineListWrapper> pairVerHor : pairsVerHor) {
			KTable table = createTableFromVerHorPair(pairVerHor.t, pairVerHor.k);
			if (table != null) {
				tables.add(table);
			}
		}

		return tables;
	}

	private boolean groupLinesByDistance(List<Line> lines, LineSorter sorter, LineValueSupplier supplier,
			double length) {
		if (!areInputDimensionsForKTableValid(lines)) {
			return false;
		}

		sorter.sort(lines);

		double dCurrent = supplier.getValue(lines.get(1)) - supplier.getValue(lines.get(0));
		double dMin = dCurrent, dMax = dCurrent;

		for (int j = 1; j < lines.size() - 1; j++) {
			dCurrent = supplier.getValue(lines.get(j + 1)) - supplier.getValue(lines.get(j));

			if (dCurrent < dMax * COEF_MIN) {
				return false;
			} else if (dCurrent > dMin * COEF_MAX) {
				return false;
			}

			if (dCurrent > dMax) {
				dMax = dCurrent;
			} else if (dCurrent < dMin) {
				dMin = dCurrent;
			}
		}

		double averageLength = length / (lines.size() - 1);
		if (averageLength > dMax * COEF_MIN && averageLength < dMin * COEF_MAX) {
			return true;
		}

		return false;
	}

	private KTable createTableFromVerHorPair(LineListWrapper verticalLinesWrap, LineListWrapper horizontalLinesWrap) {
		if (!(areInputDimensionsForKTableValid(verticalLinesWrap.lines)
				&& areInputDimensionsForKTableValid(horizontalLinesWrap.lines))) {
			return null;
		}
		double height = horizontalLinesWrap.lines.get(horizontalLinesWrap.lines.size() - 1).getAverageY()
				- horizontalLinesWrap.lines.get(0).getAverageY();
		double width = verticalLinesWrap.lines.get(verticalLinesWrap.lines.size() - 1).getAverageX()
				- verticalLinesWrap.lines.get(0).getAverageX();

		for (Line l : verticalLinesWrap.lines) {// No need to check horizontal lines
			if (!l.getBm().isDealtWith()) {
				if (l.getBm().isFractured()) {
					List<Line> lines = l.getBm().getFracturedLines();

					for (Line fl : lines) {
						boolean found = false;
						if (fl.getType() == LineType.VERTICAL) {
							for (Line vl : verticalLinesWrap.lines) {
								if (vl.equals(fl)) {
									found = true;
									break;
								}
							}
						} else {
							for (Line hl : horizontalLinesWrap.lines) {
								if (hl.equals(fl)) {
									found = true;
									break;
								}
							}
						}

						if (found == false) {
							return null;
						}
					}
					l.getBm().setDealtWith(true);
				}
			}
		}

		KTable table = new KTable(
				new Point((int) verticalLinesWrap.lines.get(0).getAverageX(),
						(int) horizontalLinesWrap.lines.get(0).getAverageY()),
				verticalLinesWrap.lines.size(), horizontalLinesWrap.lines.size(), (int) width, (int) height);

		return table;
	}

	@SuppressWarnings("unused")
	private void debugDrawTable(KTable table) {
		sP.getModel().add(table);
		sP.getCanvas().repaint();
	}

	@SuppressWarnings("unused")
	private void sortLinesByAverageValue(LineListWrapper verticalLinesWrap, LineListWrapper horizontalLinesWrap) {
		LineSorter sorterX = new CoordinateAverageXSorter();
		LineSorter sorterY = new CoordinateAverageYSorter();
		sorterY.sort(horizontalLinesWrap.lines);
		sorterX.sort(verticalLinesWrap.lines);
	}

	private boolean areInputDimensionsForKTableValid(List<Line> lines) {
		if (!(lines.size() > 2)) {
			return false;
		}

		if (!(isPowerOfTwo(lines.size() - 1))) {
			return false;
		}

		return true;
	}

	private List<Pair<LineListWrapper, LineListWrapper>> groupLinesValidPairs(List<LineListWrapper> verticalGroups,
			List<LineListWrapper> horizontalGroups) {
		List<Pair<Rectangle, LineListWrapper>> horizontalRectangles = createRectangles(verticalGroups, false);
		List<Pair<Rectangle, LineListWrapper>> verticalRectangles = createRectangles(horizontalGroups, true);

		// System.out.println("HOR rectangles found:" + horizontalRectangles.size());
		// debugWriteRectangles(horizontalRectangles);//
		// System.out.println("VERT rectangles found:" + verticalRectangles.size());
		// debugWriteRectangles(verticalRectangles);//

		List<Pair<LineListWrapper, LineListWrapper>> pairs = new ArrayList<Pair<LineListWrapper, LineListWrapper>>();

		for (Pair<Rectangle, LineListWrapper> ph : horizontalRectangles) {
			for (Pair<Rectangle, LineListWrapper> pv : verticalRectangles) {
				Pair<LineListWrapper, LineListWrapper> pair = rectangleOverlapLines(ph, pv);
				if (pair != null) {
					pairs.add(pair);
				}
			}
		}

		for (int i = 0; i < pairs.size() - 1; i++) {
			for (int j = i + 1; j < pairs.size(); j++) {
				Boolean subset = isSubset(pairs.get(i), pairs.get(j));
				if (subset != null) {
					if (subset == true) {
						pairs.remove(i);
						i--;
						break;
					} else {
						pairs.remove(j);
						j--;
					}
				}
			}
		}

		return pairs;
	}

	private Boolean isSubset(Pair<LineListWrapper, LineListWrapper> p1, Pair<LineListWrapper, LineListWrapper> p2) {

		if (p1.t.lines.size() < p2.t.lines.size()) {
			if (p1.k.lines.size() <= p2.k.lines.size()) {
				if (listSubsetTest(p1.t.lines, p2.t.lines) && listSubsetTest(p1.k.lines, p2.k.lines)) {
					return true;
				}
			}
		} else {
			if (p1.t.lines.size() > p2.t.lines.size()) {
				if (p1.k.lines.size() >= p2.k.lines.size()) {
					if (listSubsetTest(p2.t.lines, p1.t.lines) && listSubsetTest(p2.k.lines, p1.k.lines)) {
						return false;
					}
				}
			} else {
				if (p1.k.lines.size() <= p2.k.lines.size()) {
					if (listSubsetTest(p1.t.lines, p2.t.lines) && listSubsetTest(p1.k.lines, p2.k.lines)) {
						return true;
					}
				} else {
					if (listSubsetTest(p2.t.lines, p1.t.lines) && listSubsetTest(p2.k.lines, p1.k.lines)) {
						return false;
					}
				}
			}
		}

		return null;
	}

	private boolean listSubsetTest(List<Line> smaller, List<Line> bigger) {
		for (Line l : smaller) {
			if (!bigger.contains(l)) {
				return false;
			}
		}

		return true;
	}

	private Pair<LineListWrapper, LineListWrapper> rectangleOverlapLines(Pair<Rectangle, LineListWrapper> ph,
			Pair<Rectangle, LineListWrapper> pv) {
		Rectangle overlap = new Rectangle(new PointDouble(pv.t.getP1().x, ph.t.getP1().y),
				new PointDouble(pv.t.getP2().x, ph.t.getP2().y));
		// debugTestRectangle(overlap);

		List<Line> verticalLines = new ArrayList<>();
		for (Line l : ph.k.lines) {
			if (isInRectangle(overlap, l, false)) {
				verticalLines.add(l);
			}
		}

		if (verticalLines.size() < 3) {
			return null;
		}

		List<Line> horizontalLines = new ArrayList<>();
		for (Line l : pv.k.lines) {
			if (isInRectangle(overlap, l, true)) {
				horizontalLines.add(l);
			}
		}

		if (horizontalLines.size() < 3) {
			return null;
		}

		LineListWrapper horizontal = new LineListWrapper(horizontalLines, true);
		if (!groupLinesByDistance(verticalLines, new CoordinateAverageXSorter(), new LineAverageXSupplier(),
				horizontal.avgLength)) {
			return null;
		}

		LineListWrapper vertical = new LineListWrapper(verticalLines, false);
		if (!groupLinesByDistance(horizontalLines, new CoordinateAverageYSorter(), new LineAverageYSupplier(),
				vertical.avgLength)) {
			return null;
		}

		return new Pair<KTableModel.LineListWrapper, KTableModel.LineListWrapper>(vertical, horizontal);
	}

	private boolean isInRectangle(Rectangle overlap, Line l, boolean horizontal) {
		if (horizontal) {
			return l.getAverageY() > overlap.getP1().y && l.getAverageY() < overlap.getP2().y;
		} else {
			return l.getAverageX() > overlap.getP1().x && l.getAverageX() < overlap.getP2().x;
		}
	}

	@SuppressWarnings("unused")
	private void debugTestRectangle(Rectangle overlap) {
		System.out.println(overlap);
		sP.getModel().add(new SelectionRectangle(new Point((int) overlap.getP1().x, (int) overlap.getP1().y),
				new Point((int) overlap.getP2().x, (int) overlap.getP2().y)));
		sP.getCanvas().repaint();
	}

	@SuppressWarnings("unused")
	private void debugWriteRectangles(List<Pair<Rectangle, LineListWrapper>> horizontalRectangles) {
		for (var x : horizontalRectangles) {
			System.out.println(x.t);
			sP.getModel().add(new SelectionRectangle(new Point((int) x.t.getP1().x, (int) x.t.getP1().y),
					new Point((int) x.t.getP2().x, (int) x.t.getP2().y)));
			sP.getCanvas().repaint();
		}
	}

	private List<Pair<Rectangle, LineListWrapper>> createRectangles(List<LineListWrapper> verticalGroups,
			boolean minX) {
		List<Pair<Rectangle, LineListWrapper>> rectangles = new ArrayList<>();
		for (LineListWrapper wrapper : verticalGroups) {
			rectangles.add(new Pair<Rectangle, LineListWrapper>(createRectangle(wrapper, minX), wrapper));
		}

		return rectangles;
	}

	private Rectangle createRectangle(LineListWrapper wrapper, boolean minX) {
		if (minX) {
			return new Rectangle(
					new PointDouble(wrapper.avgCoordinateValue - wrapper.avgLength / 2
							- LINES_MIN_X_DISTANCE_SCALE * wrapper.avgLength, 0),
					new PointDouble(wrapper.avgCoordinateValue + (0.5 + LINES_MIN_X_DISTANCE_SCALE) * wrapper.avgLength,
							Integer.MAX_VALUE));
		} else {
			return new Rectangle(
					new PointDouble(0,
							wrapper.avgCoordinateValue - wrapper.avgLength / 2
									- LINES_MIN_Y_DISTANCE_SCALE * wrapper.avgLength),
					new PointDouble(Integer.MAX_VALUE,
							wrapper.avgCoordinateValue + (0.5 + LINES_MIN_Y_DISTANCE_SCALE) * wrapper.avgLength));
		}
	}

	private List<LineListWrapper> groupLinesByXCoordinate(List<LineListWrapper> startGroups,
			LineCoordinateDistanceTester t1, LineSorter sorter, Boolean type) {
		return groupLinesByCoordinate(startGroups, t1, sorter, type);
	}

	private List<LineListWrapper> groupLinesByYCoordinate(List<LineListWrapper> startGroups,
			LineCoordinateDistanceTester t1, LineSorter sorter, Boolean type) {
		return groupLinesByCoordinate(startGroups, t1, sorter, type);
	}

	private List<LineListWrapper> groupLinesByCoordinate(List<LineListWrapper> startGroups,
			LineCoordinateDistanceTester t1, LineSorter sorter, Boolean type) {
		List<LineListWrapper> groups = new ArrayList<>();

		for (LineListWrapper wrapper : startGroups) {
			sorter.sort(wrapper.lines);

			t1.setAvgLineLength(wrapper.avgLength);
			for (LineListWrapper wp : groupLines(wrapper.lines, t1, type)) {
				if (wp.lines.size() > 2) {
					groups.add(wp);
				}
			}

			wrapper.lines.clear();
		}

		startGroups.clear();
		return groups;
	}

	private List<LineListWrapper> groupLinesByLength(List<Line> lines) {
		sortByDistance(lines);
		return groupLines(lines, new LineDistanceTester(), null);
	}

	private List<LineListWrapper> groupLines(List<Line> lines, Tester<Line> tester, Boolean type) {
		List<Line> activeLines = new LinkedList<>();
		List<LineListWrapper> groups = new ArrayList<>();
		Line l1, l2;

		for (int i = 0; i < lines.size(); i++) {
			if (activeLines.isEmpty()) {
				activeLines.add(lines.get(i));
			} else {
				l1 = lines.get(i);
				l2 = activeLines.get(0);

				if (tester.test(l1, l2)) {
					if (activeLines.size() > 2) {
						groups.add(new LineListWrapper(new ArrayList<>(activeLines), type));
					}
					activeLines.remove(0);

					while (!activeLines.isEmpty()) {
						l2 = activeLines.get(0);
						if (tester.test(l1, l2)) {
							activeLines.remove(0);
						} else {
							break;
						}
					}
				}

				activeLines.add(l1);
			}
		}

		if (activeLines.size() > 2) {
			groups.add(new LineListWrapper(new ArrayList<>(activeLines), type));
		}
		return groups;
	}

	private void sortByDistance(List<Line> lines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				return Double.compare(o1.length(), o2.length());
			}
		});
	}

	private void initLines(List<Line> horizontalLines, List<Line> verticalLines, List<BasicMovement> bms) {
		for (BasicMovement bm : bms) {
			List<Point> breakPoints = LineModel.calculateAcumulatedBreakPoints(bm.getPoints(),
					LineModel.calculateBreakPoints(bm.getPoints()));

			if (!breakPoints.isEmpty()) {
				if (breakPoints.size() > 3) {
					continue;
				}

				Pair<List<Line>, List<Line>> verHorLines = calculateVerticalAndHorizontalLinesFromMovementAndBreakPoints(
						bm, breakPoints);
				if (verHorLines == null) {
					continue;
				}

				List<Line> fLines = new ArrayList<>();

				for (Line l : verHorLines.t) {
					verticalLines.add(l);
					fLines.add(l);
				}

				for (Line l : verHorLines.k) {
					horizontalLines.add(l);
					fLines.add(l);
				}

				bm.setFractured(true);
				bm.setFracturedLines(fLines);

				continue;
			}

			Line l = LineModel.recognize(bm);
			if (l != null) {
				if (l.getType() == LineType.HORIZONTAL) {
					horizontalLines.add(l);
				} else if (l.getType() == LineType.VERTICAL) {
					verticalLines.add(l);
				}
			}
		}

	}

	private Pair<List<Line>, List<Line>> calculateVerticalAndHorizontalLinesFromMovementAndBreakPoints(BasicMovement bm,
			List<Point> breakPoints) {
		List<Line> lines = LineModel.linesInPoints(bm.getPoints(), breakPoints, bm);
		if (lines.size() != breakPoints.size() + 1) {
			return null;
		}

		List<Line> verticalLines = new ArrayList<>();
		List<Line> horizontalLines = new ArrayList<>();
		LineType last;

		if (lines.get(0).getType() == LineType.HORIZONTAL) {
			last = LineType.HORIZONTAL;
			horizontalLines.add(lines.get(0));
		} else if (lines.get(0).getType() == LineType.VERTICAL) {
			last = LineType.VERTICAL;
			verticalLines.add(lines.get(0));
		} else {
			return null;
		}

		for (int i = 1; i < lines.size(); i++) {
			if (last == LineType.HORIZONTAL) {
				if (lines.get(i).getType() != LineType.VERTICAL) {
					return null;
				}

				verticalLines.add(lines.get(i));
				last = LineType.VERTICAL;
			} else if (last == LineType.VERTICAL) {
				if (lines.get(i).getType() != LineType.HORIZONTAL) {
					return null;
				}

				horizontalLines.add(lines.get(i));
				last = LineType.HORIZONTAL;
			} else {
				return null;
			}
		}

		return new Pair<List<Line>, List<Line>>(verticalLines, horizontalLines);
	}

	private List<GraphicalObject> getObjectsInRectangle(Point a, Point b, DrawingModel model) {
		int minX = Math.min(a.x, b.x);
		int maxX = Math.max(a.x, b.x);
		int minY = Math.min(a.y, b.y);
		int maxY = Math.max(a.y, b.y);

		List<GraphicalObject> objects = model.getObjectsInRecti(minX, maxX, minY, maxY);
		return objects;
	}

	static boolean isPowerOfTwo(int n) {
		return (int) (Math.ceil((Math.log(n) / Math.log(2)))) == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
	}

	private static class LineListWrapper {
		public List<Line> lines;
		public double avgLength;
		public double avgCoordinateValue;

		public LineListWrapper(List<Line> lines, Boolean averageX) {
			this.lines = lines;
			avgLength = lines.stream().mapToDouble(l -> l.length()).average().getAsDouble();
			if (averageX != null) {
				if (averageX == true) {
					avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageX()).average().getAsDouble();
				} else {
					avgCoordinateValue = lines.stream().mapToDouble(l -> l.getAverageY()).average().getAsDouble();
				}
			}
		}
	}
}
