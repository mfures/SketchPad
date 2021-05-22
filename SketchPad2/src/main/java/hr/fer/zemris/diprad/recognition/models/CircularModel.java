package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.recognition.models.tokens.VectorOrientationType;
import hr.fer.zemris.diprad.recognition.objects.CircularObject;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.recognition.testers.WeakNegativeColinearityTester;
import hr.fer.zemris.diprad.util.MyVector;
import hr.fer.zemris.diprad.util.PointDouble;

public class CircularModel {
	public static CircularObject recognize(BasicMovementWrapper bmw) {
		return recognize(bmw.getBm().getPoints(), 0, bmw.getBm().getPoints().size() - 1, bmw);
	}

	public static CircularObject recognize(List<Point> points, int startIndex, int endIndex, BasicMovementWrapper bmw) {
		if(endIndex<=startIndex||endIndex==-1||startIndex==-1) {
			return null;
		}
		
		double totalNorm = calculateTotalNorm(points, startIndex, endIndex);
		int k = (endIndex - startIndex + 1);
		List<PointDouble> sampledPoints = samplePoints(points, startIndex, endIndex, totalNorm, k);
		return recognize(0, k - 1, bmw, totalNorm, k, sampledPoints);
	}

	public static CircularObject recognize(int startIndex, int endIndex, BasicMovementWrapper bmw, double totalNorm,
			int k, List<PointDouble> sampledPoints) {
		PointDouble avPointDouble = calculateAveragePoint(sampledPoints, startIndex, endIndex);
		PointDouble maxVector = new PointDouble(0, 0);
		double minMaxRatio = calculateMinMaxDistanceRatio(sampledPoints, avPointDouble, maxVector, startIndex,
				endIndex);

		if (!(minMaxRatio > 0.15)) {
			//System.out.println("Bad max/min ratio:" + minMaxRatio);
			return null;
		}

		double theta = calculateThetaOfOpening(sampledPoints, avPointDouble, startIndex, endIndex);
		Double totalAngle = totalAngle(sampledPoints, avPointDouble, startIndex, endIndex);
		if (totalAngle == null) {
			return null;
		}

		return new CircularObject(avPointDouble, minMaxRatio, theta, angle(maxVector), totalAngle > 330, totalAngle,
				totalNorm, bmw, startIndex, endIndex, sampledPoints);
	}

	@SuppressWarnings("unused")
	private static VectorOrientationType angleToOrientation(double theta) {
		if (theta < -157.5) {
			return VectorOrientationType.HORIZONTAL_MINUS;
		}
		if (theta < -112.5) {
			return VectorOrientationType.DIAGONAL_MINUS_MINUS;
		}
		if (theta < -67.5) {
			return VectorOrientationType.VERTICAL_MINUS;
		}
		if (theta < -22.5) {
			return VectorOrientationType.DIAGONAL_PLUS_MINUS;
		}
		if (theta < 22.5) {
			return VectorOrientationType.HORIZONTAL_PLUS;
		}
		if (theta < 67.5) {
			return VectorOrientationType.DIAGONAL_PLUS_PLUS;
		}
		if (theta < 112.5) {
			return VectorOrientationType.VERTICAL_PLUS;
		}
		if (theta < 157.5) {
			return VectorOrientationType.DIAGONAL_MINUS_PLUS;
		}

		return VectorOrientationType.HORIZONTAL_MINUS;
	}

	private static Double totalAngle(List<PointDouble> sampledPoints, PointDouble avPointDouble, int startIndex,
			int endIndex) {
		double s12, s22;
		double angle2;
		Double totalAngle = 0.0;
		double positive = 0, negative = 0;
		for (int i = startIndex; i < endIndex; i++) {
			s12 = calculateSlope(sampledPoints.get(i), avPointDouble);
			s22 = calculateSlope(avPointDouble, sampledPoints.get(i + 1));
			if (Double.isInfinite(s12)) {
				angle2 = Math.atan(1 / s22);
			} else if (Double.isInfinite(s22)) {
				angle2 = Math.atan(-1 / s12);
			} else {
				angle2 = Math.atan((s12 - s22) / (1 + s12 * s22));
			}

			totalAngle += angle2;
			if (angle2 > 0)
				positive++;
			else if (angle2 < 0) {
				negative++;
			}
		}

		double total = positive + negative;
		positive /= (total);
		negative /= (total);
		// System.out.println(positive);
		// System.out.println(negative);
		if (positive > 0.775) {
			return Math.abs(Math.toDegrees(totalAngle));
		}
		if (negative > 0.775) {
			return Math.abs(Math.toDegrees(totalAngle));
		}

		return null;
	}

	private static double calculateThetaOfOpening(List<PointDouble> sampledPoints, PointDouble avPointDouble,
			int startIndex, int endIndex) {
		PointDouble centerToStart = new PointDouble(sampledPoints.get(startIndex).x - avPointDouble.x,
				sampledPoints.get(startIndex).y - avPointDouble.y);
		PointDouble centerToEnd = new PointDouble(sampledPoints.get(endIndex).x - avPointDouble.x,
				sampledPoints.get(endIndex).y - avPointDouble.y);
		PointDouble averageCECS = PointDouble.mulPoint(PointDouble.addPoints(centerToStart, centerToEnd), 0.5);
		return angle(averageCECS);
	}

	private static double angle(PointDouble averageCECS) {
		PointDouble normalized = PointDouble.normalizedPoint(averageCECS);
		return Math.toDegrees(Math.atan2(normalized.y, normalized.x));
	}

	public static double calculateTotalNorm(List<Point> points, int startIndex, int endIndex) {
		double totalNorm = 0.0;
		for (int i = startIndex; i < endIndex; i++) {
			totalNorm += MyVector.norm(points.get(i), points.get(i + 1));
		}
		return totalNorm;
	}

	private static double calculateMinMaxDistanceRatio(List<PointDouble> sampledPoints, PointDouble avPointDouble,
			PointDouble maxVector, int startIndex, int endIndex) {
		double min2 = Integer.MAX_VALUE, max2 = 0;

		for (int i = startIndex; i <= endIndex; i++) {
			PointDouble p = sampledPoints.get(i);
			double dist = Math.sqrt(Math.pow(p.x - avPointDouble.x, 2) + Math.pow(p.y - avPointDouble.y, 2));
			if (min2 > dist) {
				min2 = dist;
			}
			if (max2 < dist) {
				max2 = dist;
				maxVector.set(p.x - avPointDouble.x, p.y - avPointDouble.y);
			}
		}
		double minMaxRatio = min2 / max2;
		return minMaxRatio;
	}

	private static PointDouble calculateAveragePoint(List<PointDouble> sampledPoints, int startIndex, int endIndex) {
		PointDouble avPointDouble = new PointDouble(0, 0);
		for (int i = startIndex; i <= endIndex; i++) {
			PointDouble p = sampledPoints.get(i);
			avPointDouble.x += p.x;
			avPointDouble.y += p.y;
		}
		avPointDouble.x /= (endIndex - startIndex + 1);
		avPointDouble.y /= (endIndex - startIndex + 1);
		return avPointDouble;
	}

	public static List<PointDouble> samplePoints(List<Point> points, int startIndex, int endIndex, double totalNorm,
			int k) {
		List<PointDouble> sampledPoints = new ArrayList<>();
		sampledPoints.add(new PointDouble(points.get(startIndex)));
		PointDouble lastPoint = sampledPoints.get(0);
		double segmentLength = totalNorm / (k - 1);
		for (int j = 1, i = startIndex + 1; j < k - 1; j++) {
			double currentLength = MyVector.norm(lastPoint, points.get(i));
			if (currentLength > segmentLength) {
				PointDouble active = PointDouble.addPoints(lastPoint, PointDouble
						.mulPoint(PointDouble.subPoints(points.get(i), lastPoint), segmentLength / currentLength));
				sampledPoints.add(active);
				lastPoint = active;
			} else {
				double totalLength = currentLength;
				while (totalLength < segmentLength) {
					i++;
					currentLength = MyVector.norm(points.get(i - 1), points.get(i));
					totalLength += currentLength;
				}
				totalLength -= currentLength;
				PointDouble active = PointDouble.addPoints(points.get(i - 1),
						PointDouble.mulPoint(PointDouble.subPoints(points.get(i), points.get(i - 1)),
								(segmentLength - totalLength) / currentLength));
				sampledPoints.add(active);
				lastPoint = active;
			}

		}
		sampledPoints.add(new PointDouble(points.get(endIndex)));
		return sampledPoints;
	}

	public static double calculateSlope(PointDouble p1, PointDouble p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}

	public static double calculateSlope(Point p1, Point p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}

	public static List<Integer> generateAcumulatedBreakPoints(BasicMovementWrapper bmw) {
		return generateAcumulatedBreakPoints(bmw.getBm().getPoints(), new WeakNegativeColinearityTester());
	}

	private static List<Integer> generateAcumulatedBreakPoints(List<Point> points, Tester<MyVector> t) {
		List<Integer> breakPoints = BreakPointsUtil.calculateBreakPoints(points, t);
		if (breakPoints.size() == 2) {
			return breakPoints;
		}
		double totalNorm = calculateTotalNorm(points, 0, points.size() - 1);
		double activeNorm = 0.0;

		List<Integer> trueBreakPoints = new ArrayList<>();
		trueBreakPoints.add(0);

		for (int i = 1; i < breakPoints.size() - 1; i++) {
			activeNorm = calculateTotalNorm(points, trueBreakPoints.get(trueBreakPoints.size() - 1),
					breakPoints.get(i));
			if (activeNorm > KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalNorm) {
				trueBreakPoints.add(breakPoints.get(i));
			}
		}

		if (trueBreakPoints.size() == 1) {
			trueBreakPoints.add(points.size() - 1);
			return trueBreakPoints;
		}
		if (trueBreakPoints.size() == 2) {
			activeNorm = calculateTotalNorm(points, 0, trueBreakPoints.get(1));
			if (activeNorm > (1 - KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE) * totalNorm) {
				trueBreakPoints.set(1, points.size() - 1);
				return trueBreakPoints;
			}
		}

		activeNorm = calculateTotalNorm(points, trueBreakPoints.get(trueBreakPoints.size() - 1), points.size() - 1);
		if (activeNorm > KTableModel.COEF_BREAK_POINT_SEGMENT_RELATIVE_MINIMUM_SIZE * totalNorm) {
			trueBreakPoints.add(points.size() - 1);
		} else {
			trueBreakPoints.set(trueBreakPoints.size() - 1, points.size() - 1);
		}
		return trueBreakPoints;
	}

	public static List<Integer> generateAcumulatedBreakPoints(BasicMovementWrapper bmw, double d) {
		return generateAcumulatedBreakPoints(bmw.getBm().getPoints(), new WeakNegativeColinearityTester(d));

	}

	public static int generateBestBreakPoint(BasicMovementWrapper bmw) {
		return BreakPointsUtil.calculateBestBreakPoint(bmw.getBm().getPoints());
	}
}
