package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import hr.fer.zemris.diprad.SketchPad2;
import hr.fer.zemris.diprad.drawing.graphical.objects.KTable;
import hr.fer.zemris.diprad.drawing.graphical.objects.SelectionRectangle;
import hr.fer.zemris.diprad.drawing.model.DrawingModel;
import hr.fer.zemris.diprad.recognition.LineSorter;
import hr.fer.zemris.diprad.recognition.LineValueSupplier;
import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.letters.AModel;
import hr.fer.zemris.diprad.recognition.models.letters.BModel;
import hr.fer.zemris.diprad.recognition.models.letters.CModel;
import hr.fer.zemris.diprad.recognition.models.letters.DModel;
import hr.fer.zemris.diprad.recognition.models.letters.FModel;
import hr.fer.zemris.diprad.recognition.models.letters.GModel;
import hr.fer.zemris.diprad.recognition.models.letters.HModel;
import hr.fer.zemris.diprad.recognition.models.letters.WModel;
import hr.fer.zemris.diprad.recognition.models.letters.XModel;
import hr.fer.zemris.diprad.recognition.models.letters.YModel;
import hr.fer.zemris.diprad.recognition.models.letters.ZModel;
import hr.fer.zemris.diprad.recognition.models.numbers.OneModel;
import hr.fer.zemris.diprad.recognition.models.numbers.ThreeModel;
import hr.fer.zemris.diprad.recognition.models.numbers.TwoModel;
import hr.fer.zemris.diprad.recognition.models.numbers.ZeroModel;
import hr.fer.zemris.diprad.recognition.models.tokens.LineType;
import hr.fer.zemris.diprad.recognition.objects.Line;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.recognition.objects.wrappers.LineListWrapper;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageXSorter;
import hr.fer.zemris.diprad.recognition.sorters.CoordinateAverageYSorter;
import hr.fer.zemris.diprad.recognition.supliers.LineAverageXSupplier;
import hr.fer.zemris.diprad.recognition.supliers.LineAverageYSupplier;
import hr.fer.zemris.diprad.recognition.testers.LineDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LineLengthTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageXDistanceTester;
import hr.fer.zemris.diprad.recognition.testers.LinesAverageYDistanceTester;
import hr.fer.zemris.diprad.util.Pair;
import hr.fer.zemris.diprad.util.PointDouble;
import hr.fer.zemris.diprad.util.Rectangle;
import hr.fer.zemris.diprad.util.Rounding;

public class KTableModel {
	public static final double MIN_VECTOR_NORM = 5.1;
	public static final double COORDINATE_TOLERANCE = 0.05;
	public static final double LENGTH_TOLLERANCE = 0.05;
	public static final double DISTANCE_TOLERANCE = 0.25;
	public static final double COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE = 0.7;
	public static final double MAX_AVERAGE_SQUARE_ERROR = 350;

	public static final double COORDINATE_MIN = (1 - COORDINATE_TOLERANCE) / (1 + COORDINATE_TOLERANCE);
	public static final double COORDINATE_MAX = (1 + COORDINATE_TOLERANCE) / (1 - COORDINATE_TOLERANCE);
	public static final double LENGTH_MIN = (1 - LENGTH_TOLLERANCE) / (1 + LENGTH_TOLLERANCE);
	public static final double LENGTH_MAX = (1 + LENGTH_TOLLERANCE) / (1 - LENGTH_TOLLERANCE);
	public static final double DISTANCE_MIN = (1 - DISTANCE_TOLERANCE) / (1 + DISTANCE_TOLERANCE);
	public static final double DISTANCE_MAX = (1 + DISTANCE_TOLERANCE) / (1 - DISTANCE_TOLERANCE);

	private static SketchPad2 sP;

	public KTableModel(SketchPad2 sPad) {
		sP = sPad;
	}

	public void recognize(Point a, Point b) {
		List<BasicMovementWrapper> bmws = getObjectsInRectangle(a, b, sP.getModel());

		checkCharacterModels(bmws);

		List<KTable> tables = recognizeTables(bmws);
		if (tables.isEmpty()) {
			System.out.println("No tables found");
			return;
		}

		tables = checkForTableOverlaps(tables);
		if (tables == null) {
			return;
		}

		System.out.println("Našao sam ovoliko tablica:" + tables.size());
		for (var table : tables) {
			try {
				Line l = getLineInCorner(table, bmws);
				if (l != null) {
					List<List<VariableModel>> cornerVariables = handleCorner(l, table, bmws);
					List<List<VariableModel>> leftRightVariables = null;
					List<List<VariableModel>> topDownVariables = null;

					if (table.getR() == 2) {
						leftRightVariables = initLeftVariable(table, bmws);
						System.out.println("left variable: " + leftRightVariables.get(0).get(0).getVariable());
					}
					if (table.getS() == 2) {
						topDownVariables = initTopVariable(table, bmws);
						System.out.println("top variable: " + topDownVariables.get(0).get(0).getVariable());
					}

					if (table.getR() == 4) {
						leftRightVariables = initLeftRightVariables(table, bmws);
						System.out.println("left variable: " + leftRightVariables.get(0).get(0).getVariable());
						System.out.println("right variable: " + leftRightVariables.get(1).get(0).getVariable());
					}
					if (table.getS() == 4) {
						topDownVariables = initTopDownVariables(table, bmws);
						System.out.println("top variable: " + topDownVariables.get(0).get(0).getVariable());
						System.out.println("down variable: " + topDownVariables.get(1).get(0).getVariable());
					}

					getTableValuesAndSingleCellRoundings(table, bmws);
				}
			} catch (Exception e) {
				System.out.println("Dobio sam iznimku: " + e.getMessage());
				System.out.println("Crtam tablicu bez varijabli");
			}

			// debugDrawTable(table);
			// SketchPad2.debugDraw(new SelectionRectangle(table.getBoundingRectangle()));
			// SketchPad2.debugDraw(new
			// SelectionRectangle(table.getExpandedBoundingRectangle()));

			// clearBMsModel(table);
		}
	}

	private void getTableValuesAndSingleCellRoundings(KTable table, List<BasicMovementWrapper> bmws) {
		Rectangle bb = table.getBoundingRectangle();
		double minx = bb.getP1().x - 0.25 * table.getAvgWidth();
		double maxx = bb.getP1().x + 1.25 * table.getAvgWidth();
		double miny = bb.getP1().y - 0.25 * table.getAvgHeight();
		double maxy = bb.getP1().y + 1.25 * table.getAvgHeight();
		int[][] values = new int[table.getR()][table.getS()];
		List<Rounding> roundings = new ArrayList<>();

		handleSingleCell(table, bmws, minx, maxx, miny, maxy, values, roundings);
		handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 2, 1);
		handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 1, 2);
		handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 2, 2);
		if (table.getR() > 2) {
			handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 4, 1);
			handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 4, 2);
		}
		if (table.getS() > 2) {
			handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 1, 4);
			handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 2, 4);
		}
		if (table.getR() > 2 && table.getS() > 2) {
			handleMultiCell(table, bmws, minx, maxx, miny, maxy, values, roundings, 4, 4);
		}

		System.out.println("Vrijednosti");
		for (int i = 0; i < table.getR(); i++) {
			for (int j = 0; j < table.getS(); j++) {
				System.out.print(values[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("Zaokruženja");
		roundings.forEach(x -> System.out.println(x.getP1() + " " + x.getP2()));
	}

	private void handleMultiCell(KTable table, List<BasicMovementWrapper> bmws, double minx, double maxx, double miny,
			double maxy, int[][] values, List<Rounding> roundings, int rSize, int sSize) {
		maxx += (sSize - 1) * table.getAvgWidth();
		maxy += (rSize - 1) * table.getAvgHeight();
//		if (rSize == 1 && sSize == 2) {
//			debugDrawRectangle((int) minx, (int) maxx, (int) miny, (int) maxy);
//		}
		System.out.println("Ulaz: " + rSize + " " + sSize);

		for (int r = 0; r < table.getR() - rSize + 1; r++) {
			for (int s = 0; s < table.getS() - sSize + 1; s++) {
				List<BasicMovementWrapper> list = new ArrayList<>();

				for (BasicMovementWrapper bmw : bmws) {
					if (bmw.isUnused()) {
						if (!bmw.isDiscarded()) {
							if (bmw.getBm().isInRect((int) (minx + s * table.getAvgWidth()),
									(int) (maxx + s * table.getAvgWidth()), (int) (miny + r * table.getAvgHeight()),
									(int) (maxy + r * table.getAvgHeight()))) {
								list.add(bmw);
							}

						}
					}
				}

				List<CharacterModel> characters = checkMultiCellCharacters(list);
				if (characters.size() == 1) {
					roundings.add(new Rounding(new Point(r, s), new Point(rSize, sSize)));
				} else {
					characters.forEach(new Consumer<CharacterModel>() {
						@Override
						public void accept(CharacterModel t) {
							t.decBmsFragments();
							t.setBmsToDiscarded();
						}
					});
				}
			}
		}
	}

	private void handleSingleCell(KTable table, List<BasicMovementWrapper> bmws, double minx, double maxx, double miny,
			double maxy, int[][] values, List<Rounding> roundings) {
		for (int r = 0; r < table.getR(); r++) {
			for (int s = 0; s < table.getS(); s++) {
				List<BasicMovementWrapper> list = new ArrayList<>();

				for (BasicMovementWrapper bmw : bmws) {
					if (bmw.isUnused()) {
						if (!bmw.isDiscarded()) {
							if (bmw.getBm().isInRect((int) (minx + s * table.getAvgWidth()),
									(int) (maxx + s * table.getAvgWidth()), (int) (miny + r * table.getAvgHeight()),
									(int) (maxy + r * table.getAvgHeight()))) {
								list.add(bmw);
							}

						}
					}
				}

				List<CharacterModel> characters = checkSingleCellCharacters(list);
				Collections.sort(characters, new Comparator<CharacterModel>() {
					@Override
					public int compare(CharacterModel o1, CharacterModel o2) {
						return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
					}

				});
				if (characters.isEmpty()) {
					values[r][s] = -1;
				} else if (characters.size() == 1) {
					CharacterModel cm = characters.get(0);
					if (cm.getCharacter() == "0") {
						values[r][s] = 0;
					} else if (cm.getCharacter() == "1") {
						values[r][s] = 1;
					} else if (cm.getCharacter() == "X") {
						values[r][s] = 2;
					} else {
						cm.decBmsFragments();
						cm.setBmsToDiscarded();
						values[r][s] = -1;

					}
				} else if (characters.size() == 2) {
					Collections.sort(characters, new Comparator<CharacterModel>() {
						@Override
						public int compare(CharacterModel o1, CharacterModel o2) {
							return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
						}
					});

					CharacterModel cm = characters.get(0);
					CharacterModel cm2 = characters.get(1);
					if (cm.getCharacter() == "CO" || cm.getCharacter() == "0") {
						if (cm.getBoundingBox().getIp2().x > cm2.getBoundingBox().getIp2().x) {
							if (cm2.getCharacter() == "0") {
								values[r][s] = 0;
								roundings.add(new Rounding(new Point(r, s), new Point(1, 1)));
							} else if (cm2.getCharacter() == "1") {
								values[r][s] = 1;
								roundings.add(new Rounding(new Point(r, s), new Point(1, 1)));
							} else if (cm2.getCharacter() == "X") {
								values[r][s] = 2;
								roundings.add(new Rounding(new Point(r, s), new Point(1, 1)));
							} else {
								cm.decBmsFragments();
								cm.setBmsToDiscarded();
								cm2.decBmsFragments();
								cm2.setBmsToDiscarded();
								values[r][s] = -1;
							}
						} else {
							cm.decBmsFragments();
							cm.setBmsToDiscarded();
							cm2.decBmsFragments();
							cm2.setBmsToDiscarded();
							values[r][s] = -1;
						}

					} else {
						cm.decBmsFragments();
						cm.setBmsToDiscarded();
						cm2.decBmsFragments();
						cm2.setBmsToDiscarded();
					}
				} else {
					characters.forEach(new Consumer<CharacterModel>() {
						@Override
						public void accept(CharacterModel t) {
							t.decBmsFragments();
							t.setBmsToDiscarded();
						}
					});
				}
			}
		}
	}

	private List<List<VariableModel>> initTopVariable(KTable table, List<BasicMovementWrapper> bmws) {
		Rectangle bb = table.getBoundingRectangle();
		double minx3 = bb.getP1().x + 0.4 * table.getWidth();
		double maxx3 = bb.getP1().x + 1.1 * table.getWidth();
		double miny3 = bb.getP1().y - 0.7 * table.getAvgHeight();
		double maxy3 = bb.getP1().y + 0.1 * table.getAvgHeight();

		// debugDrawRectangle((int) minx3, (int) maxx3, (int) miny3, (int) maxy3);

		Line l2 = getHorizontalLineInRectangle(minx3, maxx3, miny3, maxy3, bmws);
		if (l2 != null) {
			return getVariablesFromHorizontalLine(l2, bmws);
		} else {
			throw new RuntimeException("Bad lines, l2 set but l3 not");
		}

	}

	private List<List<VariableModel>> initTopDownVariables(KTable table, List<BasicMovementWrapper> bmws) {
		Rectangle bb = table.getBoundingRectangle();
		double minx1 = bb.getP1().x + 0.15 * table.getWidth();
		double maxx1 = bb.getP1().x + 0.85 * table.getWidth();
		double miny1 = bb.getP1().y - 0.7 * table.getAvgHeight();
		double maxy1 = bb.getP1().y + 0.1 * table.getAvgHeight();

		double minx2 = bb.getP1().x + 0.15 * table.getWidth();
		double maxx2 = bb.getP1().x + 0.85 * table.getWidth();
		double miny2 = bb.getP2().y - 0.1 * table.getAvgHeight();
		double maxy2 = bb.getP2().y + 0.7 * table.getAvgHeight();

		double minx3 = bb.getP1().x + 0.4 * table.getWidth();
		double maxx3 = bb.getP1().x + 1.1 * table.getWidth();
		double miny3 = bb.getP1().y - 0.7 * table.getAvgHeight();
		double maxy3 = bb.getP1().y + 0.1 * table.getAvgHeight();

		double minx4 = bb.getP1().x + 0.4 * table.getWidth();
		double maxx4 = bb.getP1().x + 1.1 * table.getWidth();
		double miny4 = bb.getP2().y - 0.1 * table.getAvgHeight();
		double maxy4 = bb.getP2().y + 0.7 * table.getAvgHeight();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		// debugDrawRectangle((int) minx2, (int) maxx2, (int) miny2, (int) maxy2);
		// debugDrawRectangle((int) minx3, (int) maxx3, (int) miny3, (int) maxy3);
		// debugDrawRectangle((int) minx4, (int) maxx4, (int) miny4, (int) maxy4);
		// TODO Auto-generated method stub
		Line l1 = getHorizontalLineInRectangle(minx1, maxx1, miny1, maxy1, bmws);
		if (l1 != null) {
			Line l2 = getHorizontalLineInRectangle(minx4, maxx4, miny4, maxy4, bmws);
			if (l2 != null) {
				return getVariablesFromHorizontalLines(l1, l2, true, bmws);
			} else {
				throw new RuntimeException("Bad lines h, l1 set but l4 not");
			}
		} else {
			l1 = getHorizontalLineInRectangle(minx2, maxx2, miny2, maxy2, bmws);
			if (l1 != null) {
				Line l2 = getHorizontalLineInRectangle(minx3, maxx3, miny3, maxy3, bmws);
				if (l2 != null) {
					return getVariablesFromHorizontalLines(l2, l1, false, bmws);
				} else {
					throw new RuntimeException("Bad lines, l2 set but l3 not");
				}
			} else {
				throw new RuntimeException("No lines");
			}
		}
	}

	private List<List<VariableModel>> initLeftVariable(KTable table, List<BasicMovementWrapper> bmws) {
		Rectangle bb = table.getBoundingRectangle();

		double minx3 = bb.getP1().x - 0.7 * table.getAvgWidth();
		double maxx3 = bb.getP1().x + 0.1 * table.getAvgWidth();
		double miny3 = bb.getP1().y + 0.4 * table.getHeight();
		double maxy3 = bb.getP1().y + 1.1 * table.getHeight();

		// debugDrawRectangle((int) minx3, (int) maxx3, (int) miny3, (int) maxy3);

		Line l2 = getVerticalLineInRectangle(minx3, maxx3, miny3, maxy3, bmws);
		if (l2 != null) {
			return getVariablesFromVerticalLine(l2, bmws);
		} else {
			throw new RuntimeException("Bad lines, l2 not set");
		}

	}

	private List<List<VariableModel>> initLeftRightVariables(KTable table, List<BasicMovementWrapper> bmws) {
		Rectangle bb = table.getBoundingRectangle();
		double minx1 = bb.getP1().x - 0.7 * table.getAvgWidth();
		double maxx1 = bb.getP1().x + 0.1 * table.getAvgWidth();
		double miny1 = bb.getP1().y + 0.15 * table.getHeight();
		double maxy1 = bb.getP1().y + 0.85 * table.getHeight();

		double minx2 = bb.getP2().x - 0.1 * table.getAvgWidth();
		double maxx2 = bb.getP2().x + 0.7 * table.getAvgWidth();
		double miny2 = bb.getP1().y + 0.15 * table.getHeight();
		double maxy2 = bb.getP1().y + 0.85 * table.getHeight();

		double minx3 = bb.getP1().x - 0.7 * table.getAvgWidth();
		double maxx3 = bb.getP1().x + 0.1 * table.getAvgWidth();
		double miny3 = bb.getP1().y + 0.4 * table.getHeight();
		double maxy3 = bb.getP1().y + 1.1 * table.getHeight();

		double minx4 = bb.getP2().x - 0.1 * table.getAvgWidth();
		double maxx4 = bb.getP2().x + 0.7 * table.getAvgWidth();
		double miny4 = bb.getP1().y + 0.4 * table.getHeight();
		double maxy4 = bb.getP1().y + 1.1 * table.getHeight();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		// debugDrawRectangle((int) minx2, (int) maxx2, (int) miny2, (int) maxy2);
		// debugDrawRectangle((int) minx3, (int) maxx3, (int) miny3, (int) maxy3);
		// debugDrawRectangle((int) minx4, (int) maxx4, (int) miny4, (int) maxy4);

		Line l1 = getVerticalLineInRectangle(minx1, maxx1, miny1, maxy1, bmws);
		if (l1 != null) {
			Line l2 = getVerticalLineInRectangle(minx4, maxx4, miny4, maxy4, bmws);
			if (l2 != null) {
				return getVariablesFromVerticalLines(l1, l2, true, bmws);
			} else {
				throw new RuntimeException("Bad lines v, l1 set but l4 not");
			}
		} else {
			l1 = getVerticalLineInRectangle(minx2, maxx2, miny2, maxy2, bmws);
			if (l1 != null) {
				Line l2 = getVerticalLineInRectangle(minx3, maxx3, miny3, maxy3, bmws);
				if (l2 != null) {
					return getVariablesFromVerticalLines(l2, l1, false, bmws);
				} else {
					throw new RuntimeException("Bad lines, l2 set but l3 not");
				}
			} else {
				throw new RuntimeException("No lines");
			}
		}
	}

	private List<List<VariableModel>> getVariablesFromHorizontalLine(Line l1, List<BasicMovementWrapper> bmws) {
		double minx1 = l1.getAverageX() - 0.4 * l1.length();
		double maxx1 = l1.getAverageX() + 0.4 * l1.length();
		double miny1 = l1.getAverageY() - l1.length();
		double maxy1 = l1.getAverageY() + 0.1 * l1.length();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		List<VariableModel> leftVMs = getVariablesInRecti(minx1, maxx1, miny1, maxy1, bmws);
		if (leftVMs.size() != 1)
			throw new RuntimeException("Incorect number of variables found top: " + leftVMs.size());

		List<List<VariableModel>> leftRight = new ArrayList<>();
		leftRight.add(leftVMs);
		return leftRight;
	}

	private List<List<VariableModel>> getVariablesFromHorizontalLines(Line l1, Line l2, boolean leftCenter,
			List<BasicMovementWrapper> bmws) {
		double minx1 = l1.getAverageX() - 0.4 * l1.length();
		double maxx1 = l1.getAverageX() + 0.4 * l1.length();
		double miny1 = l1.getAverageY() - l1.length();
		double maxy1 = l1.getAverageY() + 0.1 * l1.length();

		double minx2 = l2.getAverageX() - 0.4 * l2.length();
		double maxx2 = l2.getAverageX() + 0.4 * l2.length();
		double miny2 = l2.getAverageY() - 0.1 * l2.length();
		double maxy2 = l2.getAverageY() + l2.length();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		// debugDrawRectangle((int) minx2, (int) maxx2, (int) miny2, (int) maxy2);
		List<VariableModel> leftVMs = getVariablesInRecti(minx1, maxx1, miny1, maxy1, bmws);
		List<VariableModel> rightVMs = getVariablesInRecti(minx2, maxx2, miny2, maxy2, bmws);
		if (leftVMs.size() != 1 && rightVMs.size() != 1)
			throw new RuntimeException(
					"Incorect number of variables found left: " + leftVMs.size() + " right: " + rightVMs.size());
		if (leftCenter) {
			leftVMs.get(0).setCenter(true);
		} else {
			rightVMs.get(0).setCenter(true);
		}

		List<List<VariableModel>> leftRight = new ArrayList<>();
		leftRight.add(leftVMs);
		leftRight.add(rightVMs);
		return leftRight;
	}

	private List<List<VariableModel>> getVariablesFromVerticalLine(Line l1, List<BasicMovementWrapper> bmws) {
		double minx1 = l1.getAverageX() - l1.length();
		double maxx1 = l1.getAverageX() + 0.1 * l1.length();
		double miny1 = l1.getAverageY() - 0.4 * l1.length();
		double maxy1 = l1.getAverageY() + 0.4 * l1.length();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		// debugDrawRectangle((int) minx2, (int) maxx2, (int) miny2, (int) maxy2);
		List<VariableModel> leftVMs = getVariablesInRecti(minx1, maxx1, miny1, maxy1, bmws);
		if (leftVMs.size() != 1)
			throw new RuntimeException("Incorect number of variables found left: " + leftVMs.size());

		List<List<VariableModel>> leftRight = new ArrayList<>();
		leftRight.add(leftVMs);
		return leftRight;
	}

	private List<List<VariableModel>> getVariablesFromVerticalLines(Line l1, Line l2, boolean leftCenter,
			List<BasicMovementWrapper> bmws) {
		double minx1 = l1.getAverageX() - l1.length();
		double maxx1 = l1.getAverageX() + 0.1 * l1.length();
		double miny1 = l1.getAverageY() - 0.4 * l1.length();
		double maxy1 = l1.getAverageY() + 0.4 * l1.length();

		double minx2 = l2.getAverageX() - 0.1 * l2.length();
		double maxx2 = l2.getAverageX() + l2.length();
		double miny2 = l2.getAverageY() - 0.4 * l2.length();
		double maxy2 = l2.getAverageY() + 0.4 * l2.length();

		// debugDrawRectangle((int) minx1, (int) maxx1, (int) miny1, (int) maxy1);
		// debugDrawRectangle((int) minx2, (int) maxx2, (int) miny2, (int) maxy2);
		List<VariableModel> leftVMs = getVariablesInRecti(minx1, maxx1, miny1, maxy1, bmws);
		List<VariableModel> rightVMs = getVariablesInRecti(minx2, maxx2, miny2, maxy2, bmws);
		if (leftVMs.size() != 1 && rightVMs.size() != 1)
			throw new RuntimeException(
					"Incorect number of variables found left: " + leftVMs.size() + " right: " + rightVMs.size());
		if (leftCenter) {
			leftVMs.get(0).setCenter(true);
		} else {
			rightVMs.get(0).setCenter(true);
		}

		List<List<VariableModel>> leftRight = new ArrayList<>();
		leftRight.add(leftVMs);
		leftRight.add(rightVMs);
		return leftRight;
	}

	private List<VariableModel> getVariablesInRecti(double minx, double maxx, double miny, double maxy,
			List<BasicMovementWrapper> bmws) {
		List<BasicMovementWrapper> usedBmws = new ArrayList<>();

		for (BasicMovementWrapper bmw : bmws) {
			if (bmw.isUnused()) {
				if (bmw.getBm().isInRect((int) minx, (int) maxx, (int) miny, (int) maxy)) {
					usedBmws.add(bmw);
				}
			}
		}

		List<CharacterModel> cms = checkCharacterModels(usedBmws);
		Collections.sort(cms, new Comparator<CharacterModel>() {
			@Override
			public int compare(CharacterModel o1, CharacterModel o2) {
				return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
			}

		});

		List<VariableModel> vms = constructVariables(cms);
		VariableModel.validateVariableListOrFail(vms);
		vms.forEach(x -> x.setBmwsToUsed());
		return vms;
	}

	private List<List<VariableModel>> handleCorner(Line l, KTable table, List<BasicMovementWrapper> bmws) {
		double yDiff = l.getMaxY() - l.getMinY();

		double minX = l.getMinX();
		double maxX = l.getMaxX() + table.getWidth() * 0.35;
		double minX2 = l.getMinX() - table.getHeight() * 0.35;
		double maxX2 = l.getMaxX();
		double minX3 = l.getMinX() - table.getWidth() * 0.35;
		double maxX3 = l.getMinX() + table.getWidth() * 0.35;

		double minY = l.getMinY() - 0.15 * yDiff;
		double maxY = l.getMaxY() - 0.1 * yDiff;
		double minY2 = l.getMinY() - 1.6 * yDiff;// Both should be -
		double maxY2 = l.getMaxY() - 1 * yDiff;
		List<BasicMovementWrapper> leftBmws = new ArrayList<>();
		List<BasicMovementWrapper> rightBmws = new ArrayList<>();
		List<BasicMovementWrapper> topBmws = new ArrayList<>();

		// debugDrawRectangle((int) minX, (int) maxX, (int) minY, (int) maxY);
		// debugDrawRectangle((int) minX2, (int) maxX2, (int) minY, (int) maxY);
		// debugDrawRectangle((int) minX3, (int) maxX3, (int) minY2, (int) maxY2);

		for (BasicMovementWrapper bmw : bmws) {
			if (bmw.isUnused()) {
				Rectangle bb = bmw.getBm().getBoundingBox();
				if (bmw.getBm().isInRect((int) minX3, (int) maxX3, (int) minY2, (int) maxY2)) {
					topBmws.add(bmw);
				} else {
					if ((l.forX(bb.getP1().x) < bb.getP1().y) && (l.forX(bb.getP2().x) < bb.getP2().y)) {
						if (bmw.getBm().isInRect((int) minX2, (int) maxX2, (int) minY, (int) maxY)) {
							leftBmws.add(bmw);
						}
					} else if ((l.forX(bb.getP1().x) > bb.getP1().y) && (l.forX(bb.getP2().x) > bb.getP2().y)) {
						if (bmw.getBm().isInRect((int) minX, (int) maxX, (int) minY, (int) maxY)) {
							rightBmws.add(bmw);
						}
					}
				}
			}
		}

		List<CharacterModel> leftCMs = checkCharacterModels(leftBmws);
		Collections.sort(leftCMs, new Comparator<CharacterModel>() {
			@Override
			public int compare(CharacterModel o1, CharacterModel o2) {
				return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
			}

		});

		List<CharacterModel> rightCMs = checkCharacterModels(rightBmws);
		Collections.sort(rightCMs, new Comparator<CharacterModel>() {
			@Override
			public int compare(CharacterModel o1, CharacterModel o2) {
				return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
			}

		});

		List<CharacterModel> topCMs = checkCharacterModels(topBmws);
		Collections.sort(topCMs, new Comparator<CharacterModel>() {
			@Override
			public int compare(CharacterModel o1, CharacterModel o2) {
				return Integer.compare(o1.getBoundingBox().getIp1().x, o2.getBoundingBox().getIp1().x);
			}

		});

		System.out.println("Lijevo: " + leftCMs.size());
		leftCMs.forEach((x) -> System.out.print(x.getCharacter() + " "));
		System.out.println("\nDesno: " + rightCMs.size());
		rightCMs.forEach((x) -> System.out.print(x.getCharacter() + " "));
		System.out.println("\nGore: " + topCMs.size());
		topCMs.forEach((x) -> System.out.print(x.getCharacter() + " "));

		List<VariableModel> leftVMs = constructVariables(leftCMs);
		VariableModel.validateVariableListOrFail(leftVMs);
		System.out.println("\nLijevo varijable: " + leftVMs.size());
		leftVMs.forEach((x) -> System.out.print(x.getVariable() + " "));

		List<VariableModel> rightVMs = constructVariables(rightCMs);
		VariableModel.validateVariableListOrFail(rightVMs);
		System.out.println("\nDesno varijable: " + rightVMs.size());
		rightVMs.forEach((x) -> System.out.print(x.getVariable() + " "));

		List<VariableModel> topVMs = constructVariables(topCMs);
		VariableModel.validateVariableListOrFail(topVMs);
		System.out.println("\nGore varijable: " + topVMs.size());
		topVMs.forEach((x) -> System.out.print(x.getVariable() + " "));
		System.out.println();

		leftVMs.forEach((x) -> x.setBmwsToUsed());
		rightVMs.forEach((x) -> x.setBmwsToUsed());
		topVMs.forEach((x) -> x.setBmwsToUsed());

		List<List<VariableModel>> variables = new ArrayList<List<VariableModel>>();
		variables.add(leftVMs);
		variables.add(rightVMs);
		variables.add(topVMs);
		return variables;
	}

	private List<VariableModel> constructVariables(List<CharacterModel> cms) {
		List<VariableModel> variables = new ArrayList<>();

		for (int i = 0; i < cms.size(); i++) {
			if (Character.isDigit(cms.get(i).getCharacter().charAt(0))) {
				throw new RuntimeException("Bad digit placement(i): " + i + " size: " + cms.size());
			}

			if (i != cms.size() - 1) {
				if (Character.isDigit(cms.get(i + 1).getCharacter().charAt(0))) {
					variables.add(new VariableModel(cms.get(i), cms.get(i + 1)));
					i++;
				}
			} else {
				variables.add(new VariableModel(cms.get(i)));
			}

		}

		return variables;
	}

	private Line getLineTypeInRectangle(double minX, double maxX, double minY, double maxY,
			List<BasicMovementWrapper> bmws, LineType lt, double width) {
		for (BasicMovementWrapper bmw : bmws) {
			if (bmw.getBm().isInRect((int) minX, (int) maxX, (int) minY, (int) maxY)) {
				Line l = LinearModel.recognize(bmw);
				if (l != null) {
					if (l.getType() == lt && l.length() > width * 0.45) {
						bmw.incTotalHandeledFragments();
						return l;
					}
				}
			}
		}

		return null;
	}

	private Line getHorizontalLineInRectangle(double minX, double maxX, double minY, double maxY,
			List<BasicMovementWrapper> bmws) {
		return getLineTypeInRectangle(minX, maxX, minY, maxY, bmws, LineType.HORIZONTAL, (maxX - minX));
	}

	private Line getVerticalLineInRectangle(double minX, double maxX, double minY, double maxY,
			List<BasicMovementWrapper> bmws) {
		return getLineTypeInRectangle(minX, maxX, minY, maxY, bmws, LineType.VERTICAL, (maxY - minY));
	}

	private Line getLineInCorner(KTable table, List<BasicMovementWrapper> bmws) {
		double minX = table.getP().x - table.getAvgWidth() * 2;
		double maxX = table.getP().x + table.getAvgWidth() / 2;
		double minY = table.getP().y - table.getAvgWidth() * 2;
		double maxY = table.getP().y + table.getAvgWidth() / 2;

		for (BasicMovementWrapper bmw : bmws) {
			if (bmw.getBm().isInRect((int) minX, (int) maxX, (int) minY, (int) maxY)) {
				Line l = LinearModel.recognize(bmw);
				if (l != null) {
					if (l.getSlope() <= 15 && l.getSlope() >= (1.0 / 15)) {
						bmw.incTotalHandeledFragments();
						return l;
					}
				}
			}
		}

		return null;
	}

	private List<CharacterModel> checkMultiCellCharacters(List<BasicMovementWrapper> bmws) {
		CharacterModel cm = null;
		List<CharacterModel> cms = new ArrayList<>();

		for (int i = 0; i < bmws.size(); i++) {
			if (bmws.get(i).isUnused()) {
				if (!bmws.get(i).isDiscarded()) {
					cm = CircularModel.recognizeC(bmws.get(i));
					if (null != cm) {
						if (cm.getCo().isFullCircle()) {
							cms.add(cm);
							bmws.get(i).incTotalHandeledFragments();
						}

						continue;
					}
				}
			}

			bmws.get(i).setDiscarded(true);
		}

		return cms;
	}

	private List<CharacterModel> checkSingleCellCharacters(List<BasicMovementWrapper> bmws) {
		CharacterModel cm = null;
		List<CharacterModel> cms = new ArrayList<>();

		for (int i = 0; i < bmws.size(); i++) {
			if (bmws.get(i).isUnused()) {
				if (i != bmws.size() - 1) {
					if (bmws.get(i).getIndex() + 1 == bmws.get(i + 1).getIndex() && bmws.get(i + 1).isUnused()) {
						cm = XModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							bmws.get(i).incTotalHandeledFragments();
							i++;
							bmws.get(i).incTotalHandeledFragments();
							continue;
						}
					}
				}
				cm = ZeroModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					bmws.get(i).incTotalHandeledFragments();
					continue;
				}
				cm = OneModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					bmws.get(i).incTotalHandeledFragments();
					continue;
				}
				cm = CircularModel.recognizeC(bmws.get(i));
				if (null != cm) {
					if (cm.getCo().isFullCircle()) {
						cms.add(cm);
						bmws.get(i).incTotalHandeledFragments();
					}

					continue;
				}
			}

			bmws.get(i).setDiscarded(true);
		}

		return cms;
	}

	private List<CharacterModel> checkCharacterModels(List<BasicMovementWrapper> bmws) {
		CharacterModel cm = null;
		List<CharacterModel> cms = new ArrayList<>();

		// System.out.println("Ulaz sa ovoliko elemenata: " + bmws.size());

		for (int i = 0; i < bmws.size(); i++) {
			if (bmws.get(i).isUnused()) {
				if (i != bmws.size() - 1) {
					if (bmws.get(i).getIndex() + 1 == bmws.get(i + 1).getIndex() && bmws.get(i + 1).isUnused()) {
						cm = XModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
						cm = YModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
						cm = AModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
						cm = DModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
						cm = BModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
						cm = FModel.recognize(bmws.get(i), bmws.get(i + 1));
						if (null != cm) {
							cms.add(cm);
							i++;
							continue;
						}
					}
				}
				cm = ZModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = TwoModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = ZeroModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = OneModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = ThreeModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = CModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}

				cm = WModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = HModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
				cm = GModel.recognize(bmws.get(i));
				if (null != cm) {
					cms.add(cm);
					continue;
				}
			}
		}

		return cms;
	}

	@SuppressWarnings("unused")
	private void clearBMsModel(KTable table) {
		DrawingModel model = sP.getModel();
		for (BasicMovementWrapper bmw : table.getBmws()) {
			model.remove(bmw.getBm());
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

	private List<KTable> recognizeTables(List<BasicMovementWrapper> bmws) {
		List<Line> horizontalLines = new ArrayList<>();
		List<Line> verticalLines = new ArrayList<>();
		initLines(horizontalLines, verticalLines, bmws);
		// System.out.println("NUM VERT:" + verticalLines.size());
		// System.out.println("NUM HOR:" + horizontalLines.size());

		List<LineListWrapper> verticalGroups = groupLinesByLength(verticalLines);
		List<LineListWrapper> horizontalGroups = groupLinesByLength(horizontalLines);
		// System.out.println("VERT GROUPS COUNT(dist):" + verticalGroups.size());
		// verticalGroups.forEach((l) -> System.out.println(l.lines.size()));
		// System.out.println("HOR GROUPS COUNT(dist):" + horizontalGroups.size());
		// horizontalGroups.forEach((l) -> System.out.println(l.lines.size()));

		verticalGroups = groupLinesByYCoordinate(verticalGroups, new LinesAverageYDistanceTester(),
				new CoordinateAverageYSorter(), false);
		// System.out.println("VERT GROUPS COUNT(dist+x):" + verticalGroups.size());
		// verticalGroups.forEach((l) -> System.out.println(l.lines.size()));

		horizontalGroups = groupLinesByXCoordinate(horizontalGroups, new LinesAverageXDistanceTester(),
				new CoordinateAverageXSorter(), true);
		// System.out.println("HOR GROUPS COUNT(dist+x):" + horizontalGroups.size());
		// horizontalGroups.forEach((l) -> System.out.println(l.lines.size()));

		List<Pair<LineListWrapper, LineListWrapper>> pairsVerHor = groupLinesValidPairs(verticalGroups,
				horizontalGroups);

		if (pairsVerHor == null) {
			return null;// No grid found
		}
		// System.out.println("Number of paired groups of lines:" + pairsVerHor.size());

		List<KTable> tables = new ArrayList<>();
		for (Pair<LineListWrapper, LineListWrapper> pairVerHor : pairsVerHor) {
			KTable table = checkForFragmentsAndCreateTable(pairVerHor.t, pairVerHor.k);
			if (table != null) {
				tables.add(table);
			} else {
				System.out.println("Invalid table");
			}
		}

		return tables;
	}

	private boolean groupLinesByDistance(List<Line> lines, LineSorter sorter, LineValueSupplier supplier,
			double minLength, double maxLength) {
		if (!areInputDimensionsForKTableValid(lines)) {
			return false;
		}

		sorter.sort(lines);

		double dCurrent = supplier.getValue(lines.get(1)) - supplier.getValue(lines.get(0));
		double dMin = dCurrent, dMax = dCurrent;

		for (int j = 1; j < lines.size() - 1; j++) {
			dCurrent = supplier.getValue(lines.get(j + 1)) - supplier.getValue(lines.get(j));

			if (dCurrent < dMax * DISTANCE_MIN) {
				return false;
			} else if (dCurrent > dMin * DISTANCE_MAX) {
				return false;
			}

			if (dCurrent > dMax) {
				dMax = dCurrent;
			} else if (dCurrent < dMin) {
				dMin = dCurrent;
			}
		}

		double minAverageLength = minLength / (lines.size() - 1);
		double maxAverageLength = maxLength / (lines.size() - 1);

		if (minAverageLength > dMax * DISTANCE_MIN && maxAverageLength < dMin * DISTANCE_MAX) {
			return true;
		}

		return false;
	}

	private KTable checkForFragmentsAndCreateTable(LineListWrapper verticalLinesWrap,
			LineListWrapper horizontalLinesWrap) {
		for (Line l : verticalLinesWrap.lines) {
			l.getBmw().incTotalHandeledFragments();
		}
		for (Line l : horizontalLinesWrap.lines) {
			l.getBmw().incTotalHandeledFragments();
		}

		for (Line l : verticalLinesWrap.lines) {
			if (l.getBmw().getTotalFragments() != l.getBmw().getTotalHandeledFragments()) {
				resetAll(verticalLinesWrap, horizontalLinesWrap);
				return null;
			}
		}
		for (Line l : horizontalLinesWrap.lines) {
			if (l.getBmw().getTotalFragments() != l.getBmw().getTotalHandeledFragments()) {
				resetAll(verticalLinesWrap, horizontalLinesWrap);
				return null;
			}
		}

		List<BasicMovementWrapper> bmws = new ArrayList<>();
		addAllBmwsToList(verticalLinesWrap, bmws);
		addAllBmwsToList(horizontalLinesWrap, bmws);

		double height = horizontalLinesWrap.lines.get(horizontalLinesWrap.lines.size() - 1).getAverageY()
				- horizontalLinesWrap.lines.get(0).getAverageY();
		double width = verticalLinesWrap.lines.get(verticalLinesWrap.lines.size() - 1).getAverageX()
				- verticalLinesWrap.lines.get(0).getAverageX();

		KTable table = new KTable(
				new Point((int) verticalLinesWrap.lines.get(0).getAverageX(),
						(int) horizontalLinesWrap.lines.get(0).getAverageY()),
				verticalLinesWrap.lines.size(), horizontalLinesWrap.lines.size(), (int) width, (int) height);

		table.setBms(bmws);
		return table;
	}

	private void addAllBmwsToList(LineListWrapper verticalLinesWrap, List<BasicMovementWrapper> bmws) {
		for (Line l : verticalLinesWrap.lines) {
			bmws.add(l.getBmw());
		}
	}

	private void resetAll(LineListWrapper verticalLinesWrap, LineListWrapper horizontalLinesWrap) {
		for (Line l : verticalLinesWrap.lines) {
			l.getBmw().resetTotalHandeledFragments();
		}
		for (Line l : horizontalLinesWrap.lines) {
			l.getBmw().resetTotalHandeledFragments();
		}
	}

	@SuppressWarnings("unused")
	public void debugDrawRectangle(int minx, int maxx, int miny, int maxy) {
		sP.getModel().add(new SelectionRectangle(new Point(minx, miny), new Point(maxx, maxy)));
		sP.getCanvas().repaint();
	}

	@SuppressWarnings("unused")
	public static void debugDrawRectangleStatic(Rectangle r) {
		sP.getModel().add(
				new SelectionRectangle(new Point(r.getIp1().x, r.getIp1().y), new Point(r.getIp2().x, r.getIp2().y)));
		sP.getCanvas().repaint();
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
				horizontal.minLength, horizontal.maxLength)) {
			return null;
		}

		LineListWrapper vertical = new LineListWrapper(verticalLines, false);
		if (!groupLinesByDistance(horizontalLines, new CoordinateAverageYSorter(), new LineAverageYSupplier(),
				vertical.minLength, vertical.minLength)) {
			return null;
		}

		return new Pair<LineListWrapper, LineListWrapper>(vertical, horizontal);
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
		// System.out.println(overlap);
		sP.getModel().add(new SelectionRectangle(new Point((int) overlap.getP1().x, (int) overlap.getP1().y),
				new Point((int) overlap.getP2().x, (int) overlap.getP2().y)));
		sP.getCanvas().repaint();
	}

	@SuppressWarnings("unused")
	private void debugWriteRectangles(List<Pair<Rectangle, LineListWrapper>> horizontalRectangles) {
		for (var x : horizontalRectangles) {
			// System.out.println(x.t);
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
					new PointDouble(wrapper.avgCoordinateValue - (wrapper.maxLength) / 2
							- (COORDINATE_MAX - 1) * (wrapper.maxLength), 0),
					new PointDouble(wrapper.avgCoordinateValue + (COORDINATE_MAX - 0.5) * (wrapper.maxLength),
							Integer.MAX_VALUE));
		} else {
			return new Rectangle(
					new PointDouble(0,
							wrapper.avgCoordinateValue - (wrapper.maxLength) / 2
									- (COORDINATE_MAX - 1) * (wrapper.maxLength)),
					new PointDouble(Integer.MAX_VALUE,
							wrapper.avgCoordinateValue + (COORDINATE_MAX - 0.5) * (wrapper.maxLength)));
		}
	}

	private List<LineListWrapper> groupLinesByXCoordinate(List<LineListWrapper> startGroups, LineDistanceTester t1,
			LineSorter sorter, Boolean type) {
		return groupLinesByCoordinate(startGroups, t1, sorter, type);
	}

	private List<LineListWrapper> groupLinesByYCoordinate(List<LineListWrapper> startGroups, LineDistanceTester t1,
			LineSorter sorter, Boolean type) {
		return groupLinesByCoordinate(startGroups, t1, sorter, type);
	}

	private List<LineListWrapper> groupLinesByCoordinate(List<LineListWrapper> startGroups, LineDistanceTester t1,
			LineSorter sorter, Boolean type) {
		List<LineListWrapper> groups = new ArrayList<>();

		for (LineListWrapper wrapper : startGroups) {
			sorter.sort(wrapper.lines);

			t1.setLengths(wrapper.minLength, wrapper.maxLength);
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
		sortByLength(lines);
		return groupLines(lines, new LineLengthTester(), null);
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

	private void sortByLength(List<Line> lines) {
		Collections.sort(lines, new Comparator<Line>() {
			@Override
			public int compare(Line o1, Line o2) {
				return Double.compare(o1.length(), o2.length());
			}
		});
	}

	private void initLines(List<Line> horizontalLines, List<Line> verticalLines, List<BasicMovementWrapper> bmws) {
		for (BasicMovementWrapper bmw : bmws) {
			List<Integer> breakPoints = LinearModel.acumulateBreakPointsWhichAreClose(bmw.getBm().getPoints());
			if (breakPoints.size() != 2) {
				// System.out.println(breakPoints.size());
				if (breakPoints.size() > 5) {
					continue;
				}

				Pair<List<Line>, List<Line>> verHorLines = calculateVerticalAndHorizontalLinesFromMovementAndBreakPoints(
						bmw, breakPoints);
				if (verHorLines == null) {
					// System.out.println("Ver hor null");
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

				bmw.setTotalFragments(verHorLines.t.size() + verHorLines.k.size());

				continue;
			}

			Line l = LinearModel.recognize(bmw);
			if (l != null) {
				if (l.getType() == LineType.HORIZONTAL) {
					horizontalLines.add(l);
				} else if (l.getType() == LineType.VERTICAL) {
					verticalLines.add(l);
				}
			}
		}

	}

	private Pair<List<Line>, List<Line>> calculateVerticalAndHorizontalLinesFromMovementAndBreakPoints(
			BasicMovementWrapper bmw, List<Integer> breakPoints) {
		List<Line> lines = LinearModel.linesInPoints(bmw.getBm().getPoints(), breakPoints, bmw);
		// System.out.println(lines.size());
		// System.out.println(breakPoints.size() - 1);
		if (lines.size() != breakPoints.size() - 1) {
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

	private List<BasicMovementWrapper> getObjectsInRectangle(Point a, Point b, DrawingModel model) {
		int minX = Math.min(a.x, b.x);
		int maxX = Math.max(a.x, b.x);
		int minY = Math.min(a.y, b.y);
		int maxY = Math.max(a.y, b.y);

		List<BasicMovementWrapper> objects = model.getObjectsInRecti(minX, maxX, minY, maxY);
		return objects;
	}

	static boolean isPowerOfTwo(int n) {
		return (int) (Math.ceil((Math.log(n) / Math.log(2)))) == (int) (Math.floor(((Math.log(n) / Math.log(2)))));
	}
}
