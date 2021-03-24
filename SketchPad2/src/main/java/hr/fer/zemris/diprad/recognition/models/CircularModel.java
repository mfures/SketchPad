package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hr.fer.zemris.diprad.recognition.models.tokens.VectorOrientationType;
import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;
import hr.fer.zemris.diprad.util.PointDouble;

public class CircularModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		return recognize(bmw.getBm().getPoints(), 0, bmw.getBm().getPoints().size() - 1, bmw);
	}

	public static boolean recognize(List<Point> points, int startIndex, int endIndex, BasicMovementWrapper bmw) {
		double totalNorm = calculateTotalNorm(points, startIndex, endIndex);
		int k = (endIndex - startIndex + 1);
		List<PointDouble> sampledPoints = samplePoints(points, startIndex, endIndex, totalNorm, k);
		PointDouble avPointDouble = calculateAveragePoint(sampledPoints);
		double minMaxRatio = calculateMinMaxDistanceRatio(sampledPoints, avPointDouble);

		if (!(minMaxRatio > 0.15)) {
			System.out.println("Bad max/min ratio:" + minMaxRatio);
			return false;
		}

		double theta = calculateThetaOfOpening(sampledPoints, avPointDouble);
		VectorOrientationType orientation = angleToOrientation(theta);
		System.out.println("Orientacija: " + orientation);
		System.out.println("Theta: " + theta);
		Double totalAngle = totalAngle(sampledPoints, avPointDouble);
		if (totalAngle == null) {
			return false;
		}

		return true;
	}

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

	private static Double totalAngle(List<PointDouble> sampledPoints, PointDouble avPointDouble) {
		double s12, s22;
		double angle2;
		Double totalAngle = 0.0;
		int checkSum2 = 0;
		for (int i = 0; i < sampledPoints.size() - 1; i++) {
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
				checkSum2++;
			else
				checkSum2--;
		}
		checkSum2 /= ((double) (sampledPoints.size() - 1));
		if (Math.abs(checkSum2) < 0.8) {
			return null;
		}

		return Math.toDegrees(totalAngle);
	}

	private static double calculateThetaOfOpening(List<PointDouble> sampledPoints, PointDouble avPointDouble) {
		PointDouble centerToStart = new PointDouble(sampledPoints.get(0).x - avPointDouble.x,
				sampledPoints.get(0).y - avPointDouble.y);
		PointDouble centerToEnd = new PointDouble(sampledPoints.get(sampledPoints.size() - 1).x - avPointDouble.x,
				sampledPoints.get(sampledPoints.size() - 1).y - avPointDouble.y);
		PointDouble averageCECS = PointDouble.mulPoint(PointDouble.addPoints(centerToStart, centerToEnd), 0.5);
		PointDouble normalized = PointDouble.normalizedPoint(averageCECS);

		// System.out.println("Center to start vector: " + centerToStart);
		// System.out.println("Center to end vector: " + centerToEnd);
		// System.out.println("Average csce: " + averageCECS);
		// System.out.println("Normalized:" + normalized);
		return Math.toDegrees(Math.atan2(normalized.y, normalized.x));
	}

	private static double calculateTotalNorm(List<Point> points, int startIndex, int endIndex) {
		double totalNorm = 0.0;
		for (int i = startIndex; i < endIndex; i++) {
			totalNorm += MyVector.norm(points.get(i), points.get(i + 1));
		}
		return totalNorm;
	}

	private static double calculateMinMaxDistanceRatio(List<PointDouble> sampledPoints, PointDouble avPointDouble) {
		double min2 = Integer.MAX_VALUE, max2 = 0;

		for (int i = 0; i < sampledPoints.size(); i++) {
			PointDouble p = sampledPoints.get(i);
			double dist = Math.sqrt(Math.pow(p.x - avPointDouble.x, 2) + Math.pow(p.y - avPointDouble.y, 2));
			if (min2 > dist) {
				min2 = dist;
			}
			if (max2 < dist) {
				max2 = dist;
			}
		}
		double minMaxRatio = min2 / max2;
		return minMaxRatio;
	}

	private static PointDouble calculateAveragePoint(List<PointDouble> sampledPoints) {
		PointDouble avPointDouble = new PointDouble(0, 0);
		for (PointDouble p : sampledPoints) {
			avPointDouble.x += p.x;
			avPointDouble.y += p.y;
		}
		avPointDouble.x /= sampledPoints.size();
		avPointDouble.y /= sampledPoints.size();
		return avPointDouble;
	}

	private static List<PointDouble> samplePoints(List<Point> points, int startIndex, int endIndex, double totalNorm,
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
}
