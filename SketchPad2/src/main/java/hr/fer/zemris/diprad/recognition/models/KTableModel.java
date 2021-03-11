package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.graphical.GraphicalObject;
import hr.fer.zemris.diprad.drawing.graphical.objects.BasicMovement;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable.Position;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.recognition.LineSorter;
import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.One;
import hr.fer.zemris.diprad.recognition.objects.Zero;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageXSorter;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageYSorter;
import hr.fer.zemris.diprad.recognition.sorters.SemiStaticValueSorter;
import hr.fer.zemris.diprad.recognition.testers.LineCoordinateDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LineDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageXDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageYDistanceTester;
import hr.fer.zemris.diprad.util.ListPair;
import hr.fer.zemris.diprad.util.Pair;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class KTableModel {
	public static final double LINES_MIN_X_DISTANCE_SCALE = 0.15;
	public static final double LINES_MIN_Y_DISTANCE_SCALE = 0.15;
	public static final double LINE_LENGTH_SCALE = 0.15;
	public static final double TABLE_WIDTH_TOLERANCE = 0.35;
	public static final double TABLE_HEIGHT_TOLERANCE = 0.35;
	private static final double TABLE_HORISONTAL_LINE_LENTGTH_TOLERANCE = 0.7;
	private static final double TABLE_VERTICAL_LINE_LENTGTH_TOLERANCE = 0.7;
	public static final double MIN_VECTOR_NORM = 4.2;

	private SketchPad2 sP;

	public KTableModel(SketchPad2 sP) {
		this.sP = sP;
	}

	public void recognize2(Point a, Point b) {
		List<GraphicalObject> objects = getObjectsInRectangle(a, b, sP.getModel());
		List<BasicMovement> bms = handleGraphicalObjects(objects);

		List<KTable> tables = recognizeTables(bms);

		if (tables.isEmpty()) {
			return;
		}

		System.out.println("Našao sam ovoliko tablica:" + tables.size());
		// TODO remove
		// for (var table : tables) {
		// debugDrawTable(table);
		// SketchPad2.debugDraw(new SelectionRectangle(table.getBoundingRectangle()));
		// SketchPad2.debugDraw(new
		// SelectionRectangle(table.getExpandedBoundingRectangle()));

		// }
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
		List<Line> horisontalLines = new ArrayList<>();
		List<Line> verticalLines = new ArrayList<>();

		initLines(horisontalLines, verticalLines, bms);
		sortByDistance(verticalLines);
		sortByDistance(horisontalLines);
		System.out.println("NUM VERT:" + verticalLines.size());
		System.out.println("NUM HOR:" + horisontalLines.size());

		// horisontalLines.forEach((x) -> System.out.println(x.length()));
		List<LineListWrapper> verticalGroups = groupLinesByDistance(verticalLines);
		List<LineListWrapper> horisontalGroups = groupLinesByDistance(horisontalLines);
		System.out.println("VERT GROUPS COUNT(dist):" + verticalGroups.size());
		System.out.println("HOR GROUPS COUNT(dist):" + horisontalGroups.size());

		verticalGroups = groupLinesByStartingCoordinate(verticalGroups, new LinesAverageYDistanceTester(),
				new CoordinateAverageYSorter(), false);
		horisontalGroups = groupLinesByStartingCoordinate(horisontalGroups, new LinesAverageXDistanceTester(),
				new CoordinateAverageXSorter(), true);
		System.out.println("VERT GROUPS COUNT(dist+x):" + verticalGroups.size());
		System.out.println("HOR GROUPS COUNT(dist+x):" + horisontalGroups.size());

		List<Pair<LineListWrapper, LineListWrapper>> pairsVerHor = groupLinesInValidPairs(verticalGroups,
				horisontalGroups);
		if (pairsVerHor == null) {
			return null;// No grid found
		}
		System.out.println("Number of paired groups of lines:" + pairsVerHor.size());

		List<KTable> tables = new ArrayList<>();
		for (Pair<LineListWrapper, LineListWrapper> pairVerHor : pairsVerHor) {
			KTable table = createTableFromVerHorPair(pairVerHor.t, pairVerHor.k);
			if (table != null) {
				tables.add(table);
			}
		}

		return tables;
	}

	private KTable createTableFromVerHorPair(LineListWrapper verticalLinesWrap, LineListWrapper horisontalLinesWrap) {
		if (!areInputDimensionsForKTableValid(verticalLinesWrap, horisontalLinesWrap)) {
			return null;
		}

		sortInputLinesBySemiStaticValue(verticalLinesWrap, horisontalLinesWrap);

		double height = horisontalLinesWrap.lines.get(horisontalLinesWrap.lines.size() - 1).getSemiStaticValue()
				- horisontalLinesWrap.lines.get(0).getSemiStaticValue();
		double width = verticalLinesWrap.lines.get(verticalLinesWrap.lines.size() - 1).getSemiStaticValue()
				- verticalLinesWrap.lines.get(0).getSemiStaticValue();

		double avgHeight = height / (horisontalLinesWrap.lines.size() - 1);
		double maxHeight = (1 + TABLE_HEIGHT_TOLERANCE) * avgHeight;
		double minHeight = (1 - TABLE_HEIGHT_TOLERANCE) * avgHeight;

		double avgWidth = width / (verticalLinesWrap.lines.size() - 1);
		double maxWidth = (1 + TABLE_WIDTH_TOLERANCE) * avgWidth;
		double minWidth = (1 - TABLE_WIDTH_TOLERANCE) * avgWidth;

		double tmp;

		if (horisontalLinesWrap.avgLength < TABLE_HORISONTAL_LINE_LENTGTH_TOLERANCE * width) {
			return null;
		}
		if (verticalLinesWrap.avgLength < TABLE_VERTICAL_LINE_LENTGTH_TOLERANCE * height) {
			return null;
		}

		for (int i = 0; i < verticalLinesWrap.lines.size() - 1; i++) {
			tmp = verticalLinesWrap.lines.get(i + 1).getSemiStaticValue()
					- verticalLinesWrap.lines.get(i).getSemiStaticValue();
			if (tmp > maxWidth || tmp < minWidth) {
				return null;
			}
		}

		for (int i = 0; i < horisontalLinesWrap.lines.size() - 1; i++) {
			tmp = horisontalLinesWrap.lines.get(i + 1).getSemiStaticValue()
					- horisontalLinesWrap.lines.get(i).getSemiStaticValue();
			if (tmp > maxHeight || tmp < minHeight) {
				return null;
			}
		}

		for (Line l : verticalLinesWrap.lines) {// No need to check horisontal lines
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
							for (Line hl : horisontalLinesWrap.lines) {
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
				new Point(verticalLinesWrap.lines.get(0).getSemiStaticValue().intValue(),
						horisontalLinesWrap.lines.get(0).getSemiStaticValue().intValue()),
				verticalLinesWrap.lines.size(), horisontalLinesWrap.lines.size(), (int) width, (int) height);

		return table;
	}

	@SuppressWarnings("unused")
	private void debugDrawTable(KTable table) {
		sP.getModel().add(table);
		sP.getCanvas().repaint();
	}

	private void sortInputLinesBySemiStaticValue(LineListWrapper verticalLinesWrap,
			LineListWrapper horisontalLinesWrap) {
		LineSorter sorter = new SemiStaticValueSorter();
		sorter.sort(horisontalLinesWrap.lines);
		sorter.sort(verticalLinesWrap.lines);
	}

	private boolean areInputDimensionsForKTableValid(LineListWrapper verticalLinesWrap,
			LineListWrapper horisontalLinesWrap) {
		if (!(verticalLinesWrap.lines.size() > 1 && horisontalLinesWrap.lines.size() > 1)) {// TODO should be 2

			return false;
		}

		if (!(isPowerOfTwo(verticalLinesWrap.lines.size() - 1) && isPowerOfTwo(horisontalLinesWrap.lines.size() - 1))) {
			return false;
		}

		return true;
	}

	private List<Pair<LineListWrapper, LineListWrapper>> groupLinesInValidPairs(List<LineListWrapper> verticalGroups,
			List<LineListWrapper> horisontalGroups) {
		// TODO Commented for debuging MUST UNCOMMENT LATER
		// if (horisontalGroups.isEmpty() || verticalGroups.isEmpty()) {
		// return null;
		// }

		List<Pair<Rectangle, LineListWrapper>> horisontalRectangles = createRectangles(verticalGroups, false);
		List<Pair<Rectangle, LineListWrapper>> verticalRectangles = createRectangles(horisontalGroups, true);

		System.out.println("HOR rectangles found:" + horisontalRectangles.size());
		debugWriteRectangles(horisontalRectangles);// TODO remove
		System.out.println("VERT rectangles found:" + verticalRectangles.size());
		debugWriteRectangles(verticalRectangles);// TODO remove

		List<Pair<LineListWrapper, LineListWrapper>> pairs = new ArrayList<Pair<LineListWrapper, LineListWrapper>>();
		List<ListPair> lPairs = new ArrayList<>();

		for (Pair<Rectangle, LineListWrapper> ph : horisontalRectangles) {
			for (Pair<Rectangle, LineListWrapper> pv : verticalRectangles) {
				Pair<LineListWrapper, LineListWrapper> pair = rectangleOverlapLines(ph, pv);
				if (pair != null) {
					ListPair p = new ListPair(pair.t.lines, pair.k.lines);
					boolean flag = true;
					for (ListPair pp : lPairs) {
						if (myEquals(pp, p)) {
							flag = false;
							break;
						}
					}

					if (flag) {
						pairs.add(pair);
						lPairs.add(p);
					}
				}
			}
		}

		return pairs;
	}

	private boolean myEquals(ListPair pp, ListPair p) {
		if (pp.l1.size() != p.l1.size()) {
			System.out.println("First list different size:" + pp.l1.size() + " " + p.l1.size());
			return false;
		}

		if (pp.l2.size() != p.l2.size()) {
			System.out.println("Second list different size:" + pp.l2.size() + " " + p.l2.size());
			return false;
		}

		System.out.println("Iste veličine");
		// TODO Auto-generated method stub
		return false;
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

		if (verticalLines.size() < 2) {// TODO should be 3
			return null;
		}

		List<Line> horisontalLines = new ArrayList<>();
		for (Line l : pv.k.lines) {
			if (isInRectangle(overlap, l, true)) {
				horisontalLines.add(l);
			}
		}

		if (horisontalLines.size() < 2) {// TODO should be 3
			return null;
		}

		return new Pair<KTableModel.LineListWrapper, KTableModel.LineListWrapper>(
				new LineListWrapper(verticalLines, false), new LineListWrapper(horisontalLines, true));
	}

	private boolean isInRectangle(Rectangle overlap, Line l, boolean horisontal) {
		if (horisontal) {
			return l.getSemiStaticValue() > overlap.getP1().y && l.getSemiStaticValue() < overlap.getP2().y;
		} else {
			return l.getSemiStaticValue() > overlap.getP1().x && l.getSemiStaticValue() < overlap.getP2().x;
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
	private void debugWriteRectangles(List<Pair<Rectangle, LineListWrapper>> horisontalRectangles) {
		for (var x : horisontalRectangles) {
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
		LineSorter sorter = new SemiStaticValueSorter();
		sorter.sort(wrapper.lines);
		double heightOrWidth = wrapper.lines.get(wrapper.lines.size() - 1).getSemiStaticValue()
				- wrapper.lines.get(0).getSemiStaticValue();

		if (minX) {
			return new Rectangle(
					new PointDouble(
							wrapper.avgCoordinateValue - wrapper.avgLength / 2
									- LINES_MIN_X_DISTANCE_SCALE * wrapper.avgLength,
							wrapper.lines.get(0).getSemiStaticValue() - 0.2 * heightOrWidth),
					new PointDouble(wrapper.avgCoordinateValue + (0.5 + LINES_MIN_X_DISTANCE_SCALE) * wrapper.avgLength,
							wrapper.lines.get(wrapper.lines.size() - 1).getSemiStaticValue() + 0.2 * heightOrWidth));
		} else {
			return new Rectangle(
					new PointDouble(wrapper.lines.get(0).getSemiStaticValue() - 0.2 * heightOrWidth,
							wrapper.avgCoordinateValue - wrapper.avgLength / 2
									- LINES_MIN_Y_DISTANCE_SCALE * wrapper.avgLength),
					new PointDouble(
							wrapper.lines.get(wrapper.lines.size() - 1).getSemiStaticValue() + 0.2 * heightOrWidth,
							wrapper.avgCoordinateValue + (0.5 + LINES_MIN_Y_DISTANCE_SCALE) * wrapper.avgLength));
		}
	}

	private List<LineListWrapper> groupLinesByStartingCoordinate(List<LineListWrapper> startGroups,
			LineCoordinateDistanceTester t1, LineSorter sorter, Boolean type) {
		List<LineListWrapper> groups = new ArrayList<>();

		for (LineListWrapper wrapper : startGroups) {
			sorter.sort(wrapper.lines);

			if (wrapper.lines.size() < 2) {// Should never happen
				wrapper.lines.clear();
				continue;
			}

			t1.setAvgLineLength(wrapper.avgLength);
			for (LineListWrapper wp : groupLines(wrapper.lines, t1, type)) {
				if (wp.lines.size() > 1) {
					groups.add(wp);
				}
			}

			wrapper.lines.clear();
		}

		startGroups.clear();
		return groups;
	}

	private List<LineListWrapper> groupLinesByDistance(List<Line> lines) {
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
					if (activeLines.size() > 1) {
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

		if (activeLines.size() > 1) {
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

	private void initLines(List<Line> horisontalLines, List<Line> verticalLines, List<BasicMovement> bms) {
		for (BasicMovement bm : bms) {
			List<Point> breakPoints = LineModel.calculateAcumulatedBreakPoints(bm.getPoints(),
					LineModel.calculateBreakPoints(bm.getPoints()));

			if (!breakPoints.isEmpty()) {
				if (breakPoints.size() > 3) {
					continue;
				}

				Pair<List<Line>, List<Line>> verHorLines = calculateVerticalAndHorisontalLinesFromMovementAndBreakPoints(
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
					horisontalLines.add(l);
					fLines.add(l);
				}

				bm.setFractured(true);
				bm.setFracturedLines(fLines);

				continue;
			}

			Line l = LineModel.recognize(bm);
			if (l != null) {
				if (l.getType() == LineType.HORISONTAL) {
					horisontalLines.add(l);
				} else if (l.getType() == LineType.VERTICAL) {
					verticalLines.add(l);
				}
			}
		}

	}

	private Pair<List<Line>, List<Line>> calculateVerticalAndHorisontalLinesFromMovementAndBreakPoints(BasicMovement bm,
			List<Point> breakPoints) {
		List<Line> lines = LineModel.linesInPoints(bm.getPoints(), breakPoints, bm);
		if (lines.size() != breakPoints.size() + 1) {
			return null;
		}

		List<Line> verticalLines = new ArrayList<>();
		List<Line> horistonalLines = new ArrayList<>();
		LineType last;

		if (lines.get(0).getType() == LineType.HORISONTAL) {
			last = LineType.HORISONTAL;
			horistonalLines.add(lines.get(0));
		} else if (lines.get(0).getType() == LineType.VERTICAL) {
			last = LineType.VERTICAL;
			verticalLines.add(lines.get(0));
		} else {
			return null;
		}

		for (int i = 1; i < lines.size(); i++) {
			if (last == LineType.HORISONTAL) {
				if (lines.get(i).getType() != LineType.VERTICAL) {
					return null;
				}

				verticalLines.add(lines.get(i));
				last = LineType.VERTICAL;
			} else if (last == LineType.VERTICAL) {
				if (lines.get(i).getType() != LineType.HORISONTAL) {
					return null;
				}

				horistonalLines.add(lines.get(i));
				last = LineType.HORISONTAL;
			} else {
				return null;
			}
		}

		return new Pair<List<Line>, List<Line>>(verticalLines, horistonalLines);
	}

	public void recognize(Point a, Point b) {
		recognize2(a, b);
		if (1 == 1) {
			return;
		}

		List<Line> horistontalLines = new ArrayList<>();
		List<Line> verticalLines = new ArrayList<>();
		OneModel oneModel = new OneModel();
		ZeroModel zeroModel = new ZeroModel();
		DrawingModel model = sP.getModel();

		List<GraphicalObject> objects = getObjectsInRectangle(a, b, model);
		List<GraphicalObject> extraObjects = new ArrayList<GraphicalObject>();
		List<Line> extraLines = new ArrayList<Line>();
		List<GraphicalObject> tmpList = new ArrayList<GraphicalObject>();
		List<GraphicalObject> circles = new ArrayList<GraphicalObject>();
		List<GraphicalObject> ones = new ArrayList<GraphicalObject>();

		for (GraphicalObject o : objects) {
			One o1 = oneModel.recognize((BasicMovement) o);
			if (o1 != null) {
				ones.add(o);
			}
		}

		for (GraphicalObject o : ones) {
			objects.remove(o);
		}

		for (GraphicalObject o : objects) {
			Zero z1 = zeroModel.recognize((BasicMovement) o);
			if (z1 != null) {
				circles.add(o);
			}
		}

		for (GraphicalObject o : circles) {
			objects.remove(o);
		}

		Line l = null;
		for (GraphicalObject o : objects) {
			l = LineModel.recognize((BasicMovement) o);
			if (l == null) {
				extraObjects.add(o);
			} else if (l.getTan() > 0.2) {
				extraLines.add(l);
				tmpList.add(o);
			} else if (l.getType() == LineType.HORISONTAL) {
				horistontalLines.add(l);
			} else {
				verticalLines.add(l);
			}
		}

		for (GraphicalObject o : extraObjects) {
			objects.remove(o);
		}
		for (GraphicalObject o : tmpList) {
			objects.remove(o);
		}

		Collections.sort(horistontalLines);
		Collections.sort(verticalLines);

		if (verticalLines.size() > 1 && horistontalLines.size() > 1) {
			double height = horistontalLines.get(horistontalLines.size() - 1).getSemiStaticValue()
					- horistontalLines.get(0).getSemiStaticValue();
			double width = verticalLines.get(verticalLines.size() - 1).getSemiStaticValue()
					- verticalLines.get(0).getSemiStaticValue();

			double avgHeight = height / (horistontalLines.size() - 1);

			double maxHeight = 1.25 * avgHeight;
			double minHeight = 0.75 * avgHeight;

			double avgWidth = width / (verticalLines.size() - 1);
			double maxWidth = 1.25 * avgWidth;
			double minWidth = 0.75 * avgWidth;

			boolean checkFlag = true;
			double tmp;

			for (Line l2 : horistontalLines) {
				if (Math.abs(l2.getP1().x - l2.getP2().x) < 0.7 * width) {
					checkFlag = false;
					break;
				}
			}

			for (Line l2 : verticalLines) {
				if (Math.abs(l2.getP1().y - l2.getP2().y) < 0.7 * height) {
					checkFlag = false;
					break;
				}
			}

			for (int i = 0; i < verticalLines.size() - 1; i++) {
				tmp = verticalLines.get(i + 1).getSemiStaticValue() - verticalLines.get(i).getSemiStaticValue();
				if (tmp > maxWidth || tmp < minWidth) {
					checkFlag = false;
					break;
				}
			}

			for (int i = 0; i < horistontalLines.size() - 1; i++) {
				tmp = horistontalLines.get(i + 1).getSemiStaticValue() - horistontalLines.get(i).getSemiStaticValue();
				if (tmp > maxHeight || tmp < minHeight) {
					checkFlag = false;
					break;
				}
			}

			if (checkFlag) {
				for (int i = 0; i < verticalLines.size() - 1; i++) {
					tmp = verticalLines.get(i + 1).getSemiStaticValue() - verticalLines.get(i).getSemiStaticValue();
					if (tmp > maxWidth || tmp < minWidth) {
						checkFlag = false;
						break;
					}
				}

				if (checkFlag) {
					for (GraphicalObject o : objects) {
						model.remove(o);
					}

					if (isPowerOfTwo(verticalLines.size() - 1) && isPowerOfTwo(horistontalLines.size() - 1)) {
						KTable table = new KTable(
								new Point(verticalLines.get(0).getSemiStaticValue().intValue(),
										horistontalLines.get(0).getSemiStaticValue().intValue()),
								verticalLines.size(), horistontalLines.size(), (int) width, (int) height);

						model.add(table);

						Zero z1 = null;
						Map<Position, List<Pairt>> map = new HashMap<>();
						for (GraphicalObject o : circles) {
							z1 = zeroModel.recognize((BasicMovement) o);
							if (z1 != null) {
								if (table.youInterested(z1.getCenter())) {
									if (z1.getRadius() < avgHeight && z1.getRadius() < avgWidth) {
										Position p = table.getPosition(z1.getCenter());
										if (map.containsKey(p)) {
											map.get(p).add(new Pairt(0, o));
										} else {
											List<Pairt> listP = new ArrayList<Pairt>();
											listP.add(new Pairt(0, o));
											map.put(p, listP);
										}
									}
								}
							}
						}

						One one1 = null;
						for (GraphicalObject o : ones) {
							one1 = oneModel.recognize((BasicMovement) o);
							if (one1 != null) {
								if (table.youInterested(one1.getCenter())) {
									if (one1.getRadius() < avgHeight && one1.getRadius() < avgWidth) {
										Position p = table.getPosition(one1.getCenter());
										if (map.containsKey(p)) {
											map.get(p).add(new Pairt(1, o));
										} else {
											List<Pairt> listP = new ArrayList<Pairt>();
											listP.add(new Pairt(1, o));
											map.put(p, listP);
										}
									}
								}
							}
						}

						Map<Position, List<PairLine>> mapLine = new HashMap<>();
						for (GraphicalObject o : tmpList) {
							l = LineModel.recognize((BasicMovement) o);
							Point averagePoint = new Point();
							averagePoint.x = (l.getP1().x + l.getP2().x) / 2;
							averagePoint.y = (l.getP1().y + l.getP2().y) / 2;

							if (table.youInterested(averagePoint)) {
								if (l.length() / 2 < avgHeight && l.length() / 2 < avgWidth) {
									Position p = table.getPosition(averagePoint);
									if (mapLine.containsKey(p)) {
										mapLine.get(p).add(new PairLine(l, o));
									} else {
										List<PairLine> listP = new ArrayList<>();
										listP.add(new PairLine(l, o));
										mapLine.put(p, listP);
									}
								}
							}
						}

						for (Position x : mapLine.keySet()) {
							List<PairLine> l1 = mapLine.get(x);
							if (l1.size() == 2) {
								Line ln1 = l1.get(0).l;
								Line ln2 = l1.get(1).l;
								if ((ln1.getSlope() / ln2.getSlope()) < 0) {
									double y1 = ln1.forX(ln2.getP1().x);
									double y2 = ln1.forX(ln2.getP2().x);
									double y3 = ln2.forX(ln1.getP1().x);
									double y4 = ln2.forX(ln1.getP2().x);

									if ((y1 < ln2.getP1().y && y2 > ln2.getP2().y)
											|| (y1 > ln2.getP1().y && y2 < ln2.getP2().y)) {
										if ((y3 < ln1.getP1().y && y4 > ln1.getP2().y)
												|| (y3 > ln1.getP1().y && y4 < ln1.getP2().y)) {
											if (!map.containsKey(x)) {
												List<Pairt> listP = new ArrayList<Pairt>();
												listP.add(new Pairt(2, l1.get(0).o));
												map.put(x, listP);
												model.remove(l1.get(1).o);
											}
										}
									}
								}
							}
						}

						for (Position x : map.keySet()) {
							List<Pairt> l1 = map.get(x);
							if (l1.size() == 1) {
								model.remove(l1.get(0).o);
								table.setValueAt(x, l1.get(0).v);
							}
						}

					}
				}
			}

		}

		sP.getCanvas().repaint();
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

	private static class Pairt {
		public int v;
		public GraphicalObject o;

		public Pairt(int v, GraphicalObject o) {
			this.v = v;
			this.o = o;
		}
	}

	private static class PairLine {
		public Line l;
		public GraphicalObject o;

		public PairLine(Line l, GraphicalObject o) {
			this.l = l;
			this.o = o;
		}
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lines == null) ? 0 : lines.hashCode());
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
			LineListWrapper other = (LineListWrapper) obj;
			if (lines == null) {
				if (other.lines != null)
					return false;
			} else if (!lines.equals(other.lines))
				return false;
			return true;
		}

	}
}
