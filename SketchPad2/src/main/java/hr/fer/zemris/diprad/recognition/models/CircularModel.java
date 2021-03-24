package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.wrappers.BasicMovementWrapper;
import hr.fer.zemris.diprad.util.MyVector;
import hr.fer.zemris.diprad.util.PointDouble;

public class CircularModel {
	public static boolean recognize(BasicMovementWrapper bmw) {
		return recognize(bmw.getBm().getPoints(), 0, bmw.getBm().getPoints().size() - 1, bmw);
	}

	public static boolean recognize(List<Point> points, int startIndex, int endIndex, BasicMovementWrapper bmw) {
		List<MyVector> vectors = MyVector.initVectorList(points, startIndex, endIndex);
		double totalNorm = 0;
		for (int i = 0; i < vectors.size(); i++) {
			totalNorm += vectors.get(i).norm();
		}
		int k = (endIndex - startIndex + 1);
		// k /= 2;
		// System.out.println("K:" + k);
		List<PointDouble> sampledPoints = new ArrayList<>();
		sampledPoints.add(new PointDouble(points.get(startIndex)));
		PointDouble lastPoint = sampledPoints.get(0);
		double segmentLength = totalNorm / (k - 1);
		// System.out.println("Target segment length: " + segmentLength);
		for (int j = 1, i = startIndex + 1; j < k - 1; j++) {
			double currentLength = MyVector.norm(lastPoint, points.get(i));
			// System.out.println("Currentt length initial:" + currentLength);
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
					// System.out.println("Total length: " + totalLength);
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

		// System.out.println("Total points: " + (endIndex - startIndex + 1));
		// System.out.println("Calculated points: " + sampledPoints.size());
		// sampledPoints.forEach((x) -> System.out.print(x + " "));
		// System.out.println();

		Point averagePoint = new Point(0, 0);
		for (int i = startIndex; i <= endIndex; i++) {
			Point p = points.get(i);
			averagePoint.x += p.x;
			averagePoint.y += p.y;
		}

		PointDouble avPointDouble = new PointDouble(0, 0);
		for (PointDouble p : sampledPoints) {
			avPointDouble.x += p.x;
			avPointDouble.y += p.y;
		}
		avPointDouble.x /= sampledPoints.size();
		avPointDouble.y /= sampledPoints.size();

		averagePoint.x = (int) Math.round((averagePoint.x) / (endIndex - startIndex + 1.0));
		averagePoint.y = (int) Math.round((averagePoint.y) / (endIndex - startIndex + 1.0));

		double min = Integer.MAX_VALUE, max = 0;
		int minIndex = 0, maxIndex = 0;

		for (int i = startIndex; i <= endIndex; i++) {
			Point p = points.get(i);
			double dist = Math.sqrt(Math.pow(p.x - averagePoint.x, 2) + Math.pow(p.y - averagePoint.y, 2));
			if (min > dist) {
				minIndex = i;
				min = dist;
			}
			if (max < dist) {
				maxIndex = i;
				max = dist;
			}
		}

		double min2 = Integer.MAX_VALUE, max2 = 0;
		int minIndex2 = 0, maxIndex2 = 0;

		for (int i = 0; i < sampledPoints.size(); i++) {
			PointDouble p = sampledPoints.get(i);
			double dist = Math.sqrt(Math.pow(p.x - avPointDouble.x, 2) + Math.pow(p.y - avPointDouble.y, 2));
			if (min2 > dist) {
				minIndex2 = i;
				min2 = dist;
			}
			if (max2 < dist) {
				maxIndex2 = i;
				max2 = dist;
			}
		}

		System.out.println("Min max ratio: " + min / max);
		if (!(min / max > 0.1)) {
			System.out.println("Bad max/min ratio:" + min / max);
			// return false;
		}

		System.out.println("Average point double: " + avPointDouble);
		System.out.println("Min point:            " + sampledPoints.get(minIndex2));
		PointDouble minToCenter2 = new PointDouble(avPointDouble.x - sampledPoints.get(minIndex2).x,
				avPointDouble.y - sampledPoints.get(minIndex2).y);
		System.out.println("Min to center vector: " + minToCenter2);
		double s12, s22;
		double angle2;
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

			if (angle2 > 0)
				checkSum2++;
			else
				checkSum2--;
		}
		System.out.println("Cheksum:" + (checkSum2 / ((double) (sampledPoints.size() - 1))));

		System.out.println("Average point:        " + averagePoint);
		System.out.println("Min point:            " + points.get(minIndex));
		Point minToCenter = new Point(averagePoint.x - points.get(minIndex).x, averagePoint.y - points.get(minIndex).y);
		System.out.println("Min to center vector: " + minToCenter);

		double s1, s2;
		double angle;
		int checkSum = 0;
		for (int i = startIndex; i < endIndex; i++) {
			s1 = calculateSlope(points.get(i), averagePoint);
			s2 = calculateSlope(averagePoint, points.get(i + 1));
			if (Double.isInfinite(s1)) {
				angle = Math.atan(1 / s2);
			} else if (Double.isInfinite(s2)) {
				angle = Math.atan(-1 / s1);
			} else {
				angle = Math.atan((s1 - s2) / (1 + s1 * s2));
			}

			if (angle > 0)
				checkSum++;
			else
				checkSum--;
		}

		// (endIndex - startIndex + 1.0)
		System.out.println("Cheksum:" + (checkSum / ((double) (endIndex - startIndex))));
		if (Math.abs((checkSum / ((double) (endIndex - startIndex)))) > 0.) {
			return true;
		}

		System.out.println("Bad check sum");
		return false;
	}

	public static double calculateSlope(PointDouble p1, PointDouble p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}

	public static double calculateSlope(Point p1, Point p2) {
		return (p2.y - p1.y) / (p2.x * 1.0 - p1.x);
	}
}
