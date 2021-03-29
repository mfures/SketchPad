package hr.fer.zemris.diprad.recognition.models;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.diprad.recognition.Tester;
import hr.fer.zemris.diprad.util.MyVector;

public class BreakPointsUtil {
	/**
	 * This function calculates break points using tester test function
	 * 
	 * @param points All points that define movement
	 * @param t      tester which test function is used for determining of break
	 *               point
	 * @return list of all break points containing first and last point in list of
	 *         points
	 */

	public static List<Integer> calculateBreakPoints(List<Point> points, Tester<MyVector> t) {
		if (points.size() < 3) {
			return new ArrayList<>();
		}

		List<MyVector> vectors = MyVector.listOfPointsToListOfVectors(points);
		List<Integer> breakPoints = new ArrayList<>();
		breakPoints.add(0);

		for (int i = 0; i < vectors.size() - 1; i++) {
			if (t.test(vectors.get(i), vectors.get(i + 1))) {
				breakPoints.add(vectors.get(i).i2);
			}
		}

		breakPoints.add(points.size() - 1);
		return breakPoints;
	}

	public static Integer calculateBestBreakPoint(List<Point> points) {
		if (points.size() < 3) {
			return -1;
		}

		List<MyVector> vectors = MyVector.listOfPointsToListOfVectors(points);
		List<Integer> breakPoints = new ArrayList<>();
		breakPoints.add(0);

		double cos, cosMin = 1;
		int min = -1;

		for (int i = 0; i < vectors.size() - 1; i++) {
			cos = MyVector.scalarProduct(vectors.get(i), vectors.get(i + 1))
					/ (vectors.get(i).norm() * vectors.get(i + 1).norm());

			//System.out.println("i:" + i + " cos:" + cos);
			if (cos < cosMin) {
				cosMin = cos;
				min = vectors.get(i).i2;
			}
		}

		//System.out.println(min);
		return min;
	}
}
