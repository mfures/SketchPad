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
import hr.fer.zemris.diprad.recognition.sorters.CoordinateMinXSorter;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateMinYSorter;
import hr.fer.zemris.diprad.recognition.testers.LineCoordinateDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LineDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesMinXDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesMinYDistanceTester;
import hr.fer.zemris.diprad.util.Pair;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;

public class KTableModel {
	public static final double LINES_MIN_X_DISTANCE_SCALE = 0.07;
	public static final double LINES_MIN_Y_DISTANCE_SCALE = 0.07;
	public static final double LINE_LENGTH_SCALE = 0.1;

	private SketchPad2 sP;

	public KTableModel(SketchPad2 sP) {
		this.sP = sP;
	}

	public void recognize2(Point a, Point b) {
		List<GraphicalObject> objects = getObjectsInRectangle(a, b, sP.getModel());
		List<BasicMovement> bms = handleGraphicalObjects(objects);

		KTable table = recognizeTable(bms);

		if (table == null) {
			return;
		}

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

	private KTable recognizeTable(List<BasicMovement> bms) {
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

		verticalGroups = groupLinesByStartingCoordinate(verticalGroups, new LinesMinYDistanceTester(),
				new CoordinateMinYSorter(), false);
		horisontalGroups = groupLinesByStartingCoordinate(horisontalGroups, new LinesMinXDistanceTester(),
				new CoordinateMinXSorter(), true);
		System.out.println("VERT GROUPS COUNT(dist+x):" + verticalGroups.size());
		System.out.println("HOR GROUPS COUNT(dist+x):" + horisontalGroups.size());

		List<Pair<LineListWrapper, LineListWrapper>> pairsVerHor = groupLinesInValidPairs(verticalGroups,
				horisontalGroups);
		if (pairsVerHor == null) {
			return null;// No grid found
		}

		System.out.println("Testiram");

		return null;
	}

	private List<Pair<LineListWrapper, LineListWrapper>> groupLinesInValidPairs(List<LineListWrapper> verticalGroups,
			List<LineListWrapper> horisontalGroups) {
		// TODO Commented for debuging MUST UNCOMMENT LATER
		// if (horisontalGroups.isEmpty() || verticalGroups.isEmpty()) {
		// return null;
		// }

		List<Rectangle> horisontalRectangles = createRectangles(verticalGroups, false);
		List<Rectangle> verticalRectangles = createRectangles(horisontalGroups, true);

		System.out.println("HOR rectangles found:" + horisontalRectangles.size());
		debugWriteRectangles(horisontalRectangles);// TODO remove
		System.out.println("VERT rectangles found:" + verticalRectangles.size());
		debugWriteRectangles(verticalRectangles);// TODO remove

		return null;
	}

	private void debugWriteRectangles(List<Rectangle> rectangles) {
		for (var x : rectangles) {
			System.out.println(x);
			sP.getModel().add(new SelectionRectangle(new Point((int) x.getP1().x, (int) x.getP1().y),
					new Point((int) x.getP2().x, (int) x.getP2().y)));
			sP.getCanvas().repaint();
		}
	}

	private List<Rectangle> createRectangles(List<LineListWrapper> verticalGroups, boolean minX) {
		List<Rectangle> rectangles = new ArrayList<>();

		for (LineListWrapper wrapper : verticalGroups) {
			rectangles.add(createRectangle(wrapper, minX));
		}

		return rectangles;
	}

	private Rectangle createRectangle(LineListWrapper wrapper, boolean minX) {
		if (minX) {
			return new Rectangle(
					new PointDouble(wrapper.avgCoordinateValue - LINES_MIN_X_DISTANCE_SCALE * wrapper.avgLength, 0),
					new PointDouble(wrapper.avgCoordinateValue + (1 + LINES_MIN_X_DISTANCE_SCALE) * wrapper.avgLength,
							Double.MAX_VALUE));
		} else {
			return new Rectangle(
					new PointDouble(0, wrapper.avgCoordinateValue - LINES_MIN_Y_DISTANCE_SCALE * wrapper.avgLength),
					new PointDouble(Double.MAX_VALUE,
							wrapper.avgCoordinateValue + (1 + LINES_MIN_Y_DISTANCE_SCALE) * wrapper.avgLength));
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

		public LineListWrapper(List<Line> lines, Boolean minX) {
			this.lines = lines;
			avgLength = lines.stream().mapToDouble(l -> l.length()).average().getAsDouble();
			if (minX != null) {
				if (minX == true) {
					avgCoordinateValue = lines.stream().mapToDouble(l -> l.getMinX()).average().getAsDouble();
				} else {
					avgCoordinateValue = lines.stream().mapToDouble(l -> l.getMinY()).average().getAsDouble();
				}
			}
		}
	}
}
